import { useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import HTTPClient from '../HTTPClient.js';
import SuggestInput from '../components/SuggestInput.jsx';
import Icon from '../components/Icon.jsx';
import { EmptyState, PageHeader } from '../components/Layout.jsx';
import { StatusBadge } from '../components/Badges.jsx';
import useSession from '../hooks/useSession.js';
import useContractDraft from '../hooks/useContractDraft.js';
import useDialog from '../hooks/useDialog.js';
import { formatDate, formatMoney } from '../utils/format.js';
import NewSponsor from './NewSponsor.jsx';
import NewSeason from './NewSeason.jsx';
import NewTerm from './NewTerm.jsx';
import NewBenefit from './NewBenefit.jsx';

export default function ContractSigning() {
  const navigate = useNavigate();
  const dialog = useDialog();
  const [organizer] = useSession('organizer');
  const { contract, patchContract: setContract, resetContract, discardContract } = useContractDraft();
  const [txtSponsorName, setTxtSponsorName] = useState(contract.sponsor?.name || '');
  const [txtSeasonName, setTxtSeasonName] = useState(contract.season?.name || '');
  const [txtTermTitle, setTxtTermTitle] = useState('');
  const [txtBenefitTitle, setTxtBenefitTitle] = useState('');
  const [busy, setBusy] = useState(false);
  // NewSponsor / NewSeason / NewTerm / NewBenefit hiện dạng cửa sổ nổi, không có route riêng
  const [openForm, setOpenForm] = useState(null);
  const closeForm = useCallback(() => setOpenForm(null), []);

  const saved = !!contract.id;
  const signed = contract.status === 'Đã ký';
  const locked = saved;

  const searchSponsor = useCallback((k) => HTTPClient.fetch('GET', `/sponsors?name=${encodeURIComponent(k)}`), []);
  const searchSeason = useCallback((k) => HTTPClient.fetch('GET', `/seasons?name=${encodeURIComponent(k)}`), []);
  const searchTerm = useCallback((k) => HTTPClient.fetch('GET', `/terms?title=${encodeURIComponent(k)}`), []);
  const searchBenefit = useCallback((k) => HTTPClient.fetch('GET', `/benefits?title=${encodeURIComponent(k)}`), []);

  const addTerm = (term) => {
    setTxtTermTitle('');
    if (contract.listContractTerm.some((ct) => ct.term.id === term.id)) {
      dialog.alert(`Điều khoản "${term.title}" đã có trong hợp đồng.`);
      return;
    }
    setContract((c) => ({ ...c, listContractTerm: [...c.listContractTerm, { term, notes: '' }] }));
  };

  const addBenefit = (benefit) => {
    setTxtBenefitTitle('');
    if (contract.listContractBenefit.some((cb) => cb.benefit.id === benefit.id)) {
      dialog.alert(`Quyền lợi "${benefit.title}" đã có trong hợp đồng.`);
      return;
    }
    setContract((c) => ({
      ...c, listContractBenefit: [...c.listContractBenefit, { benefit, value: '', unit: '', notes: '' }],
    }));
  };

  const updateRow = (listName, index, field, value) => {
    setContract((c) => ({
      ...c,
      [listName]: c[listName].map((row, i) => (i === index ? { ...row, [field]: value } : row)),
    }));
  };

  const removeRow = (listName, index) => {
    setContract((c) => ({ ...c, [listName]: c[listName].filter((_, i) => i !== index) }));
  };

  const validate = () => {
    const errors = [];
    if (!contract.sponsor) errors.push('Chưa chọn nhà tài trợ');
    if (!contract.season) errors.push('Chưa chọn mùa giải');
    const value = Number(contract.value);
    if (!Number.isInteger(value) || value <= 0) errors.push('Giá trị hợp đồng phải là số nguyên dương');
    if (value > 2147483647) errors.push('Giá trị hợp đồng vượt quá giới hạn');
    if (!contract.effectiveFrom || !contract.effectiveTo) errors.push('Chưa nhập thời gian hiệu lực');
    else if (contract.effectiveFrom >= contract.effectiveTo) errors.push('Ngày hết hiệu lực phải sau ngày bắt đầu');
    if (!contract.sponsorRepresentative.trim()) errors.push('Chưa nhập người đại diện nhà tài trợ');
    if (!contract.organizerRepresentative.trim()) errors.push('Chưa nhập người đại diện Ban tổ chức');
    contract.listContractBenefit.forEach((cb) => {
      if (cb.value !== '' && (!Number.isInteger(Number(cb.value)) || Number(cb.value) < 0)) {
        errors.push(`Giá trị quyền lợi "${cb.benefit.title}" không hợp lệ`);
      }
    });
    return errors;
  };

  const btnSave = async () => {
    const errors = validate();
    if (errors.length) {
      await dialog.alert(errors.map((e) => `• ${e}`).join('\n'), { type: 'error', title: 'Thông tin không hợp lệ' });
      return;
    }
    setBusy(true);
    try {
      const result = await HTTPClient.fetch('POST', '/contracts', {
        sponsor: { id: contract.sponsor.id },
        season: { id: contract.season.id },
        organizer: { id: organizer.id },
        value: Number(contract.value),
        effectiveFrom: contract.effectiveFrom,
        effectiveTo: contract.effectiveTo,
        sponsorRepresentative: contract.sponsorRepresentative,
        organizerRepresentative: contract.organizerRepresentative,
        listContractTerm: contract.listContractTerm.map((ct) => ({ term: { id: ct.term.id }, notes: ct.notes })),
        listContractBenefit: contract.listContractBenefit.map((cb) => ({
          benefit: { id: cb.benefit.id },
          value: cb.value === '' ? null : Number(cb.value),
          unit: cb.unit,
          notes: cb.notes,
        })),
      });
      setContract({ id: result.id, barcode: result.barcode, status: result.status, createdDate: result.createdDate });
      await dialog.alert(`Hợp đồng ${result.barcode} đã được lưu ở trạng thái "Chờ ký". Hệ thống sẽ mở hộp thoại in.`,
        { type: 'success', title: 'Đã lưu hợp đồng' });
      setTimeout(() => window.print(), 50);
    } catch (e) {
      await dialog.alert(e.message, { type: 'error' });
    } finally {
      setBusy(false);
    }
  };

  const btnConfirmSigned = async () => {
    if (!saved) {
      await dialog.alert('Hợp đồng chưa được lưu. Hãy bấm "Lưu và in hợp đồng" trước.', { title: 'Chưa lưu hợp đồng' });
      return;
    }
    if (!await dialog.confirm(`Xác nhận Ban tổ chức và nhà tài trợ đã ký hợp đồng ${contract.barcode}?`,
      { confirmText: 'Xác nhận đã ký' })) return;
    setBusy(true);
    try {
      const result = await HTTPClient.fetch('PUT', `/contracts/${contract.id}/signed`);
      setContract({ status: result.status, signedDate: result.signedDate });
      await dialog.alert(`Hợp đồng ${result.barcode} chuyển sang "Đã ký" (ngày ký ${formatDate(result.signedDate)}).`,
        { type: 'success', title: 'Đã cập nhật trạng thái hợp đồng' });
    } catch (e) {
      await dialog.alert(e.message, { type: 'error' });
    } finally {
      setBusy(false);
    }
  };

  const btnCancel = async () => {
    const dirty = !saved && (contract.sponsor || contract.season || contract.listContractTerm.length || contract.listContractBenefit.length);
    if (dirty && !await dialog.confirm('Hợp đồng đang soạn chưa được lưu. Hủy và quay về trang chủ?', { confirmText: 'Hủy hợp đồng' })) return;
    discardContract();
    navigate('/');
  };

  const newContract = () => {
    resetContract();
    setTxtSponsorName('');
    setTxtSeasonName('');
  };

  return (
    <main className="main">
      <div className="no-print">
        <PageHeader
          crumbs={[{ label: 'Trang chủ', to: '/' }, { label: 'Ký hợp đồng tài trợ' }]}
          title={<>Ký hợp đồng tài trợ <StatusBadge status={contract.status} />{contract.barcode && <span className="chip">{contract.barcode}</span>}</>}
          subtitle={saved ? 'Hợp đồng đã lưu, nội dung không thể chỉnh sửa.' : 'Điền thông tin đã thỏa thuận với nhà tài trợ, sau đó lưu và in hợp đồng.'}
        />

        <div className="layout-aside">
          <div>
            <section className="card">
              <div className="card-head"><h2><span className="step">1</span>Thông tin chung</h2></div>
              <div className="card-body grid cols-2">
                <div className="field">
                  <label htmlFor="txtSponsorName">Nhà tài trợ</label>
                  <div className="field-row">
                    <SuggestInput
                      id="txtSponsorName" value={txtSponsorName} onChange={setTxtSponsorName} disabled={locked}
                      search={searchSponsor} placeholder="Nhập tên, chọn từ danh sách gợi ý"
                      notFoundText={`Không tồn tại nhà tài trợ chứa từ khóa "${txtSponsorName.trim()}"`}
                      renderItem={(s) => <><span>{s.name}</span><small>MST {s.taxCode}</small></>}
                      onSelect={(s) => { setTxtSponsorName(s.name); setContract({ sponsor: s }); }}
                    />
                    <button id="btnNewSponsor" type="button" className="btn" disabled={locked} title="Thêm nhà tài trợ" onClick={() => setOpenForm('sponsor')}>
                      <Icon name="plus" /><span className="sr-only">Thêm nhà tài trợ</span>
                    </button>
                  </div>
                </div>
                <div className="field">
                  <label htmlFor="txtSeasonName">Mùa giải</label>
                  <div className="field-row">
                    <SuggestInput
                      id="txtSeasonName" value={txtSeasonName} onChange={setTxtSeasonName} disabled={locked}
                      search={searchSeason} placeholder="Nhập tên, chọn từ danh sách gợi ý"
                      notFoundText="Không tìm thấy mùa giải"
                      renderItem={(s) => <><span>{s.name}</span><small>{formatDate(s.startDate)} – {formatDate(s.endDate)}</small></>}
                      onSelect={(s) => { setTxtSeasonName(s.name); setContract({ season: s }); }}
                    />
                    <button id="btnNewSeason" type="button" className="btn" disabled={locked} title="Thêm mùa giải" onClick={() => setOpenForm('season')}>
                      <Icon name="plus" /><span className="sr-only">Thêm mùa giải</span>
                    </button>
                  </div>
                </div>
                <div className="field">
                  <label htmlFor="txtContractValue">Giá trị hợp đồng</label>
                  <div className="input-affix">
                    <input id="txtContractValue" type="number" min="1" step="1" value={contract.value} disabled={locked}
                      placeholder="vd: 1000000000" onChange={(e) => setContract({ value: e.target.value })} />
                    <span>VNĐ</span>
                  </div>
                  {contract.value && <span className="hint">{formatMoney(contract.value)}</span>}
                </div>
                <div className="field">
                  <span className="field-label">Ngày lập</span>
                  <time id="lblCreatedDate" dateTime={contract.createdDate} className="input-static">{formatDate(contract.createdDate)}</time>
                </div>
                <div className="field">
                  <label htmlFor="txtEffectiveFrom">Hiệu lực từ</label>
                  <input id="txtEffectiveFrom" type="date" value={contract.effectiveFrom} disabled={locked}
                    onChange={(e) => setContract({ effectiveFrom: e.target.value })} />
                </div>
                <div className="field">
                  <label htmlFor="txtEffectiveTo">Đến</label>
                  <input id="txtEffectiveTo" type="date" value={contract.effectiveTo} disabled={locked}
                    onChange={(e) => setContract({ effectiveTo: e.target.value })} />
                </div>
                <div className="field">
                  <label htmlFor="txtSponsorRepresentative">Đại diện nhà tài trợ</label>
                  <input id="txtSponsorRepresentative" value={contract.sponsorRepresentative} disabled={locked}
                    placeholder="Họ tên người đại diện" onChange={(e) => setContract({ sponsorRepresentative: e.target.value })} />
                </div>
                <div className="field">
                  <label htmlFor="txtOrganizerRepresentative">Đại diện Ban tổ chức</label>
                  <input id="txtOrganizerRepresentative" value={contract.organizerRepresentative} disabled={locked}
                    onChange={(e) => setContract({ organizerRepresentative: e.target.value })} />
                </div>
              </div>
            </section>

            <section className="card">
              <div className="card-head">
                <h2><span className="step">2</span>Điều khoản</h2>
                <span className="count">{contract.listContractTerm.length} điều khoản</span>
              </div>
              <div className="card-body">
                <div className="field-row">
                  <label htmlFor="txtTermTitle" className="sr-only">Tiêu đề điều khoản</label>
                  <SuggestInput
                    id="txtTermTitle" value={txtTermTitle} onChange={setTxtTermTitle} disabled={locked}
                    search={searchTerm} placeholder="Nhập tiêu đề điều khoản, chọn từ gợi ý (vd: theo chặng)"
                    notFoundText="Không tìm thấy điều khoản phù hợp"
                    renderItem={(t) => <><span>{t.title}</span><small>{t.type}</small></>}
                    onSelect={addTerm}
                  />
                  <button id="btnNewTerm" type="button" className="btn" disabled={locked} onClick={() => setOpenForm('term')}><Icon name="plus" />Thêm điều khoản mới</button>
                </div>
              </div>
              <div className="card-body flush table-wrap">
                <table id="tblContractTerm">
                  <thead><tr><th className="tt">TT</th><th>Tiêu đề</th><th>Nội dung</th><th>Ghi chú</th><th /></tr></thead>
                  <tbody>
                    {contract.listContractTerm.length === 0 && (
                      <tr><td colSpan={5}><EmptyState icon="contract">Chưa có điều khoản — tìm và chọn ở ô phía trên</EmptyState></td></tr>
                    )}
                    {contract.listContractTerm.map((ct, i) => (
                      <tr key={ct.term.id}>
                        <td className="tt">{i + 1}</td>
                        <td className="cell-title">{ct.term.title}</td>
                        <td className="cell-sub">{ct.term.details}</td>
                        <td style={{ minWidth: 220 }}><input value={ct.notes || ''} placeholder="Ghi chú áp dụng riêng" disabled={locked}
                          onChange={(e) => updateRow('listContractTerm', i, 'notes', e.target.value)} /></td>
                        <td className="num"><button type="button" className="btn-link" disabled={locked}
                          onClick={() => removeRow('listContractTerm', i)}><Icon name="trash" size={16} />Xóa</button></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>

            <section className="card">
              <div className="card-head">
                <h2><span className="step">3</span>Quyền lợi</h2>
                <span className="count">{contract.listContractBenefit.length} quyền lợi</span>
              </div>
              <div className="card-body">
                <div className="field-row">
                  <label htmlFor="txtBenefitTitle" className="sr-only">Tiêu đề quyền lợi</label>
                  <SuggestInput
                    id="txtBenefitTitle" value={txtBenefitTitle} onChange={setTxtBenefitTitle} disabled={locked}
                    search={searchBenefit} placeholder="Nhập tiêu đề quyền lợi, chọn từ gợi ý (vd: VIP)"
                    notFoundText="Không tìm thấy quyền lợi phù hợp"
                    renderItem={(b) => <><span>{b.title}</span><small>{b.type}</small></>}
                    onSelect={addBenefit}
                  />
                  <button id="btnNewBenefit" type="button" className="btn" disabled={locked} onClick={() => setOpenForm('benefit')}><Icon name="plus" />Thêm quyền lợi mới</button>
                </div>
              </div>
              <div className="card-body flush table-wrap">
                <table id="tblContractBenefit">
                  <thead><tr><th className="tt">TT</th><th>Tiêu đề</th><th>Giá trị</th><th>Đơn vị</th><th>Ghi chú</th><th /></tr></thead>
                  <tbody>
                    {contract.listContractBenefit.length === 0 && (
                      <tr><td colSpan={6}><EmptyState icon="flag">Chưa có quyền lợi — tìm và chọn ở ô phía trên</EmptyState></td></tr>
                    )}
                    {contract.listContractBenefit.map((cb, i) => (
                      <tr key={cb.benefit.id}>
                        <td className="tt">{i + 1}</td>
                        <td className="cell-title">{cb.benefit.title}</td>
                        <td><input className="small" type="number" min="0" value={cb.value ?? ''} placeholder="Giá trị" disabled={locked}
                          onChange={(e) => updateRow('listContractBenefit', i, 'value', e.target.value)} /></td>
                        <td><input className="small" value={cb.unit || ''} placeholder="Đơn vị" disabled={locked}
                          onChange={(e) => updateRow('listContractBenefit', i, 'unit', e.target.value)} /></td>
                        <td style={{ minWidth: 180 }}><input value={cb.notes || ''} placeholder="Ghi chú" disabled={locked}
                          onChange={(e) => updateRow('listContractBenefit', i, 'notes', e.target.value)} /></td>
                        <td className="num"><button type="button" className="btn-link" disabled={locked}
                          onClick={() => removeRow('listContractBenefit', i)}><Icon name="trash" size={16} />Xóa</button></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
          </div>

          <aside className="aside">
            <section className="card">
              <div className="card-head"><h2>Tóm tắt hợp đồng</h2></div>
              <div className="card-body">
                <div className="total-box">
                  <small>Giá trị hợp đồng</small>
                  <b>{contract.value ? formatMoney(contract.value) : '—'}</b>
                </div>
                <dl className="summary">
                  <div><dt>Nhà tài trợ</dt><dd>{contract.sponsor?.name || '—'}</dd></div>
                  <div><dt>Mùa giải</dt><dd>{contract.season?.name || '—'}</dd></div>
                  <div><dt>Hiệu lực</dt><dd>{contract.effectiveFrom && contract.effectiveTo ? `${formatDate(contract.effectiveFrom)} – ${formatDate(contract.effectiveTo)}` : '—'}</dd></div>
                  <div><dt>Điều khoản / quyền lợi</dt><dd>{contract.listContractTerm.length} / {contract.listContractBenefit.length}</dd></div>
                  {contract.signedDate && <div><dt>Ngày ký</dt><dd>{formatDate(contract.signedDate)}</dd></div>}
                </dl>
                <div className="stack" style={{ marginTop: 18 }}>
                  <button id="btnSave" type="button" className="btn btn-primary btn-lg" onClick={btnSave} disabled={saved || busy}>
                    <Icon name="print" />Lưu và in hợp đồng
                  </button>
                  <button id="btnConfirmSigned" type="button" className="btn btn-success btn-lg" onClick={btnConfirmSigned} disabled={signed || busy}>
                    <Icon name="pen" />Xác nhận đã ký
                  </button>
                  {saved && <button type="button" className="btn" onClick={() => window.print()}><Icon name="print" />In lại</button>}
                  {signed && <button type="button" className="btn" onClick={newContract}><Icon name="plus" />Soạn hợp đồng mới</button>}
                  <button id="btnCancel" type="button" className="btn btn-ghost" onClick={btnCancel}>Hủy</button>
                </div>
              </div>
            </section>
          </aside>
        </div>
      </div>

      <ContractPrint contract={contract} />

      {openForm === 'sponsor' && (
        <NewSponsor onClose={closeForm} onCreated={(sponsor) => {
          setTxtSponsorName(sponsor.name);
          setContract({ sponsor });
          closeForm();
        }} />
      )}
      {openForm === 'season' && (
        <NewSeason onClose={closeForm} onCreated={(season) => {
          setTxtSeasonName(season.name);
          setContract({ season });
          closeForm();
        }} />
      )}
      {openForm === 'term' && (
        <NewTerm onClose={closeForm} onCreated={(term) => { addTerm(term); closeForm(); }} />
      )}
      {openForm === 'benefit' && (
        <NewBenefit onClose={closeForm} onCreated={(benefit) => { addBenefit(benefit); closeForm(); }} />
      )}
    </main>
  );
}

/** Bản in hợp đồng (chỉ hiện khi in). */
function ContractPrint({ contract }) {
  if (!contract.id) return null;
  return (
    <article className="print-only contract-print">
      <h1>HỢP ĐỒNG TÀI TRỢ</h1>
      <p className="center">Số: {contract.barcode} — Ngày lập: {formatDate(contract.createdDate)}</p>
      <p><b>Bên tài trợ:</b> {contract.sponsor?.name} (MST {contract.sponsor?.taxCode}), {contract.sponsor?.address}</p>
      <p><b>Mùa giải được tài trợ:</b> {contract.season?.name}</p>
      <p><b>Giá trị hợp đồng:</b> {formatMoney(contract.value)}</p>
      <p><b>Thời gian hiệu lực:</b> {formatDate(contract.effectiveFrom)} – {formatDate(contract.effectiveTo)}</p>
      <h2>Các điều khoản hợp đồng</h2>
      <ol>
        {contract.listContractTerm.map((ct) => (
          <li key={ct.term.id}>{ct.term.title}{ct.notes ? `; ghi chú: ${ct.notes}` : ''}. <i>{ct.term.details}</i></li>
        ))}
      </ol>
      <h2>Các quyền lợi của nhà tài trợ</h2>
      <ol>
        {contract.listContractBenefit.map((cb) => (
          <li key={cb.benefit.id}>
            {cb.benefit.title}{cb.value !== '' && cb.value != null ? `, ${cb.value} ${cb.unit || ''}` : ''}{cb.notes ? `; ${cb.notes}` : ''}
          </li>
        ))}
      </ol>
      <div className="signatures">
        <div><b>ĐẠI DIỆN BAN TỔ CHỨC</b><br /><br /><br /><br />{contract.organizerRepresentative}</div>
        <div><b>ĐẠI DIỆN NHÀ TÀI TRỢ</b><br /><br /><br /><br />{contract.sponsorRepresentative}</div>
      </div>
    </article>
  );
}
