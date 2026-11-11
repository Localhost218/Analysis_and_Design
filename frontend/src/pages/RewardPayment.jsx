import { useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import HTTPClient from '../HTTPClient.js';
import SuggestInput from '../components/SuggestInput.jsx';
import Icon from '../components/Icon.jsx';
import { EmptyState, PageHeader } from '../components/Layout.jsx';
import { RankBadge, StatusBadge } from '../components/Badges.jsx';
import useSession, { session } from '../hooks/useSession.js';
import useDialog from '../hooks/useDialog.js';
import Invoice from './Invoice.jsx';
import { duration, formatDate, formatMoney, isRaceEnded, raceCode, seasonRaceLabel } from '../utils/format.js';

export const METHOD_ONLINE = 'Chuyển khoản online';
export const METHOD_DIRECT = 'Trả thưởng trực tiếp';

/** Hóa đơn đang soạn: listRegisteredReceipt là dữ liệu của bảng tblPayment. */
const emptyInvoice = (method = METHOD_ONLINE) => ({ paymentMethod: method, listRegisteredReceipt: [] });

export default function RewardPayment() {
  const navigate = useNavigate();
  const dialog = useDialog();
  const [organizer] = useSession('organizer');
  const [season, setSeason] = useSession('season');
  const [race, setRace] = useSession('race');
  const [stored, setInvoice] = useSession('invoice', () => emptyInvoice(METHOD_ONLINE));
  // Hóa đơn đã thanh toán (có id) hiện bằng cửa sổ Invoice; bảng thanh toán khi đó bắt đầu lại từ đầu
  const paidInvoice = stored?.id ? stored : null;
  const invoice = paidInvoice ? emptyInvoice(stored?.paymentMethod || METHOD_ONLINE) : stored;

  const [paymentMethod, setPaymentMethod] = useState(invoice.paymentMethod || METHOD_ONLINE);
  const [txtSeasonName, setTxtSeasonName] = useState(season?.name || '');
  const [txtContent, setTxtContent] = useState('');
  const [loadingDriver, setLoadingDriver] = useState(null);
  const [paying, setPaying] = useState(false);

  const listRace = (season?.listRace || []).filter(isRaceEnded);
  const tblPayment = invoice.listRegisteredReceipt;
  const lblTotalAmount = tblPayment.reduce((sum, r) => sum + r.reward, 0);
  const inPayment = (id) => tblPayment.some((r) => r.id === id);
  const results = race?.listRegisteredReceipt || [];

  const handleMethodChange = (newMethod) => {
    setPaymentMethod(newMethod);
    setInvoice((inv) => ({ ...inv, paymentMethod: newMethod }));
  };

  const searchSeason = useCallback((k) => HTTPClient.fetch('GET', `/seasons?name=${encodeURIComponent(k)}`), []);

  const selectSeason = async (s) => {
    setTxtSeasonName(s.name);
    setRace(null);
    try {
      const info = await HTTPClient.fetch('GET', `/seasons/${s.id}`);
      setSeason(info);
      if (!(info.listRace || []).some(isRaceEnded)) {
        await dialog.alert(`Chưa có chặng đua đủ điều kiện trả thưởng (Mùa giải ${info.name} chưa có chặng đua nào kết thúc).`, { title: 'Chưa có chặng đua đủ điều kiện trả thưởng' });
      }
    } catch (e) {
      await dialog.alert(e.message, { type: 'error' });
    }
  };

  const selectRace = async (raceId) => {
    if (!raceId) {
      setRace(null);
      return;
    }
    try {
      const info = await HTTPClient.fetch('GET', `/races/${raceId}`);
      if (!info.basePrize || !info.listRegisteredReceipt?.length) {
        setRace({ ...info, listRegisteredReceipt: [] });
        await dialog.alert(`Chặng ${info.name} còn thiếu dữ liệu kết quả hoặc mức thưởng cơ sở.`, { type: 'error', title: 'Chặng đua thiếu dữ liệu' });
        return;
      }
      setRace(info);
    } catch (e) {
      await dialog.alert(e.message, { type: 'error' });
    }
  };

  const toggleReceipt = async (receipt, checked) => {
    if (!checked) {
      setInvoice((inv) => ({ ...inv, listRegisteredReceipt: inv.listRegisteredReceipt.filter((r) => r.id !== receipt.id) }));
      return;
    }
    const driver = receipt.registeredDriver.driver;

    if (paymentMethod === METHOD_DIRECT) {
      // Trả thưởng trực tiếp: không bắt buộc tài khoản ngân hàng
      const row = {
        id: receipt.id,
        reward: receipt.reward,
        race: { id: race.id, name: race.name, season: { id: season.id, name: season.name, year: season.year } },
        registeredDriver: receipt.registeredDriver,
        bankAccount: null,
      };
      setInvoice((inv) => ({ ...inv, listRegisteredReceipt: [...inv.listRegisteredReceipt, row] }));
      return;
    }

    setLoadingDriver(receipt.id);
    try {
      const info = await HTTPClient.fetch('GET', `/drivers/${driver.id}`);
      const bankAccount = (info.listBankAccount || []).find((a) => a.isDefault && a.status === 'Hoạt động');
      if (!bankAccount) {
        await dialog.alert(`Thiếu thông tin nhận thưởng: ${driver.fullName}\nTay đua chưa có tài khoản nhận thưởng mặc định.`, { type: 'error', title: 'Không thể thêm vào bảng thanh toán' });
        return;
      }
      const row = {
        id: receipt.id,
        reward: receipt.reward,
        race: { id: race.id, name: race.name, season: { id: season.id, name: season.name, year: season.year } },
        registeredDriver: receipt.registeredDriver,
        bankAccount,
      };
      setInvoice((inv) => ({ ...inv, listRegisteredReceipt: [...inv.listRegisteredReceipt, row] }));
    } catch (e) {
      await dialog.alert(e.message, { type: 'error' });
    } finally {
      setLoadingDriver(null);
    }
  };

  const btnPay = async () => {
    if (!tblPayment.length) return;
    const isDirect = paymentMethod === METHOD_DIRECT;
    const confirmMsg = isDirect
      ? `Xác nhận trả thưởng trực tiếp ${formatMoney(lblTotalAmount)} cho ${tblPayment.length} khoản thưởng của các tay đua?`
      : `Thanh toán ${formatMoney(lblTotalAmount)} cho ${tblPayment.length} khoản thưởng qua ${paymentMethod.toLowerCase()}?`;

    if (!await dialog.confirm(confirmMsg, {
      title: 'Bạn xác nhận thanh toán?',
      confirmText: isDirect ? 'Xác nhận trả thưởng' : 'Thanh toán'
    })) return;

    setPaying(true);
    try {
      const saved = await HTTPClient.fetch('POST', '/invoices', {
        organizer: { id: organizer.id },
        paymentMethod: paymentMethod,
        listRegisteredReceipt: tblPayment.map((r) => ({ id: r.id })),
      });

      if (isDirect) {
        // Trả thưởng trực tiếp: không cần hiện hóa đơn
        await dialog.alert(`Đã xác nhận trả thưởng trực tiếp thành công ${formatMoney(lblTotalAmount)} cho ${tblPayment.length} khoản thưởng.`, { type: 'success', title: 'Trả thưởng thành công' });
        setInvoice(emptyInvoice(METHOD_DIRECT));
      } else {
        await dialog.alert(`Hóa đơn ${saved.barcode} đã được lập và gửi đến email của từng tay đua.`, { type: 'success', title: 'Thanh toán thành công' });
        setInvoice(saved); // Hiện cửa sổ Invoice
      }
      // Nạp lại kết quả chặng đang xem để cập nhật trạng thái "Đã trả"
      if (race?.id) selectRace(race.id);
    } catch (e) {
      await dialog.alert(`${e.message}\n\nBấm "Thanh toán" để thử lại hoặc "Hủy" để kết thúc.`, { type: 'error', title: 'Thanh toán không thành công' });
    } finally {
      setPaying(false);
    }
  };

  const btnCancel = async () => {
    if (tblPayment.length && !await dialog.confirm('Xóa bảng thanh toán và quay về trang chủ?', { confirmText: 'Hủy thanh toán' })) return;
    session.remove('invoice', 'season', 'race');
    navigate('/');
  };

  return (
    <main className="main wide">
      <PageHeader
        crumbs={[{ label: 'Trang chủ', to: '/' }, { label: 'Trả thưởng cho tay đua' }]}
        title="Trả thưởng cho tay đua"
        subtitle="Chọn kết quả của các chặng đã kết thúc; hỗ trợ chuyển khoản online hoặc trả thưởng trực tiếp."
      />

      <section className="card">
        <div className="card-head"><h2><span className="step">1</span>Chọn mùa giải và chặng đua</h2></div>
        <div className="card-body grid cols-2">
          <div className="field">
            <label htmlFor="txtSeasonName">Mùa giải</label>
            <SuggestInput
              id="txtSeasonName" value={txtSeasonName} onChange={setTxtSeasonName}
              search={searchSeason} placeholder="Nhập tên, chọn từ danh sách gợi ý"
              notFoundText="Không tìm thấy mùa giải"
              renderItem={(s) => <><span>{s.name}</span><small>{formatDate(s.startDate)} – {formatDate(s.endDate)}</small></>}
              onSelect={selectSeason}
            />
            {season && <span className="hint"><Icon name="calendar" size={13} /> {formatDate(season.startDate)} – {formatDate(season.endDate)}</span>}
          </div>
          <div className="field">
            <label htmlFor="listRace">Chặng đua</label>
            <select id="listRace" value={race?.id || ''} disabled={!season || listRace.length === 0}
              onChange={(e) => selectRace(e.target.value)}>
              <option value="">{season && listRace.length === 0 ? 'Chưa có chặng đua đủ điều kiện trả thưởng' : 'Chọn chặng đua đã kết thúc'}</option>
              {listRace.map((r) => <option key={r.id} value={r.id}>{r.name} · {formatDate(r.startTime)}</option>)}
            </select>
            {race?.basePrize ? <span className="hint">Mức thưởng cơ sở {raceCode(race.name)}: <b>{formatMoney(race.basePrize)}</b> · {race.location}</span> : null}
          </div>
        </div>
      </section>

      <section className="card">
        <div className="card-head">
          <h2><span className="step">2</span>Kết quả chặng đua</h2>
          {race && <span className="count">{race.name}</span>}
        </div>
        <div className="card-body flush table-wrap">
          <table id="tblResult">
            <thead>
              <tr>
                <th className="tt">TT</th><th>Số xe</th><th>Tay đua</th><th>Hạng</th><th className="num">Vòng</th>
                <th className="num">Thời gian</th><th className="num">Điểm</th><th className="num">Hệ số</th>
                <th className="num">Tiền thưởng</th><th>Trạng thái</th><th className="center">Chọn</th>
              </tr>
            </thead>
            <tbody>
              {results.length === 0 && (
                <tr><td colSpan={11}><EmptyState icon="flag" title="Chưa có dữ liệu">Chọn mùa giải và chặng đua để xem kết quả</EmptyState></td></tr>
              )}
              {results.map((r, i) => {
                const paid = r.paymentStatus === 'Đã trả';
                const locked = paid || !r.reward;
                const checked = inPayment(r.id);
                return (
                  <tr key={r.id} className={`${locked ? 'dim' : ''} ${checked ? 'selected' : ''}`}>
                    <td className="tt">{i + 1}</td>
                    <td><span className="racing-no">#{r.registeredDriver.driver.racingNumber}</span></td>
                    <td>
                      <div className="cell-title nowrap">{r.registeredDriver.driver.fullName}</div>
                      <div className="cell-sub">{r.registeredDriver.team?.name}</div>
                    </td>
                    <td><RankBadge rank={r.rank} completedStatus={r.completedStatus} /></td>
                    <td className="num">{r.completedLap}</td>
                    <td className="num mono">{duration(race.startTime, r.finishedTime)}</td>
                    <td className="num">{r.score}</td>
                    <td className="num">{race.basePrize ? (r.reward / race.basePrize).toFixed(2) : '—'}</td>
                    <td className="num cell-title">{formatMoney(r.reward)}</td>
                    <td><StatusBadge status={r.paymentStatus} /></td>
                    <td className="center">
                      <input type="checkbox" aria-label={`Chọn ${r.registeredDriver.driver.fullName}`}
                        disabled={locked || loadingDriver === r.id}
                        checked={checked}
                        onChange={(e) => toggleReceipt(r, e.target.checked)} />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>

      <section className="card">
        <div className="card-head" style={{ justifyContent: 'space-between', flexWrap: 'wrap', gap: 12 }}>
          <h2><span className="step">3</span>Bảng thanh toán</h2>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <span style={{ fontSize: 13, fontWeight: 500 }}>Hình thức:</span>
            <div style={{ display: 'inline-flex', borderRadius: 8, background: '#f0f2f5', padding: 3, gap: 4 }}>
              <button
                type="button"
                className={`btn btn-sm ${paymentMethod === METHOD_ONLINE ? 'btn-primary' : 'btn-ghost'}`}
                style={{ borderRadius: 6, fontSize: 12, padding: '4px 10px' }}
                onClick={() => handleMethodChange(METHOD_ONLINE)}
              >
                <Icon name="bank" size={13} /> {METHOD_ONLINE}
              </button>
              <button
                type="button"
                className={`btn btn-sm ${paymentMethod === METHOD_DIRECT ? 'btn-primary' : 'btn-ghost'}`}
                style={{ borderRadius: 6, fontSize: 12, padding: '4px 10px' }}
                onClick={() => handleMethodChange(METHOD_DIRECT)}
              >
                <Icon name="wallet" size={13} /> {METHOD_DIRECT}
              </button>
            </div>
            <span className="count">{tblPayment.length} khoản</span>
          </div>
        </div>
        <div className="card-body flush table-wrap">
          <table id="tblPayment">
            <thead>
              <tr>
                <th className="tt">TT</th>
                <th>Mùa giải – Chặng</th>
                <th>Tay đua</th>
                <th>Hình thức / Ngân hàng</th>
                <th>Chủ tài khoản</th>
                <th>Số tài khoản</th>
                <th className="num">Tiền thưởng</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {tblPayment.length === 0 && (
                <tr><td colSpan={8}><EmptyState icon="wallet" title="Chưa chọn tay đua nào">Tích chọn ở bảng kết quả để thêm vào bảng thanh toán</EmptyState></td></tr>
              )}
              {tblPayment.map((r, i) => (
                <tr key={r.id}>
                  <td className="tt">{i + 1}</td>
                  <td><span className="chip">{seasonRaceLabel(r.race)}</span></td>
                  <td className="cell-title nowrap">{r.registeredDriver.driver.fullName} <span className="racing-no">#{r.registeredDriver.driver.racingNumber}</span></td>
                  <td>{r.bankAccount ? r.bankAccount.bank.name : <span className="chip chip-success">Trả thưởng trực tiếp</span>}</td>
                  <td>{r.bankAccount ? r.bankAccount.accountHolder : 'Trao giải trực tiếp'}</td>
                  <td className="mono">{r.bankAccount ? r.bankAccount.accountNumber : '—'}</td>
                  <td className="num cell-title">{formatMoney(r.reward)}</td>
                  <td className="num"><button type="button" className="btn-link" onClick={() => toggleReceipt(r, false)}><Icon name="trash" size={16} />Bỏ</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <div className="checkout-bar" role="region" aria-label="Thanh toán">
        <div className="checkout-total">
          <small>Tổng tiền · {tblPayment.length} khoản</small>
          <b id="lblTotalAmount">{formatMoney(lblTotalAmount)}</b>
        </div>
        <dl className="checkout-meta">
          <div><dt>Phương thức</dt><dd><b>{paymentMethod}</b></dd></div>
          <div><dt>Người thực hiện</dt><dd>{organizer?.fullName}</dd></div>
        </dl>
        <div className="checkout-content">
          <label htmlFor="txtContent">Nội dung</label>
          <input id="txtContent" value={txtContent} placeholder="vd: Trả thưởng chặng đua F1"
            onChange={(e) => setTxtContent(e.target.value)} />
        </div>
        <div className="checkout-actions">
          <button id="btnCancel" type="button" className="btn" onClick={btnCancel}>Hủy</button>
          <button id="btnPay" type="button" className="btn btn-primary btn-lg" onClick={btnPay} disabled={!tblPayment.length || paying}>
            <Icon name="wallet" />{paying ? 'Đang xử lý...' : (paymentMethod === METHOD_DIRECT ? 'Xác nhận trả thưởng' : 'Thanh toán online')}
          </button>
        </div>
      </div>
      {paidInvoice && (
        <Invoice
          invoice={paidInvoice}
          onClose={() => setInvoice(emptyInvoice(paymentMethod))}
          onBack={() => {
            session.remove('invoice', 'season', 'race');
            navigate('/');
          }}
        />
      )}
    </main>
  );
}
