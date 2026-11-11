import { useEffect, useState } from 'react';
import HTTPClient from '../HTTPClient.js';
import Icon from '../components/Icon.jsx';
import Modal from '../components/Modal.jsx';
import { EmptyState, PageHeader, initials } from '../components/Layout.jsx';
import { StatusBadge } from '../components/Badges.jsx';
import useSession from '../hooks/useSession.js';
import useDialog from '../hooks/useDialog.js';
import { formatDate, formatMoney, sponsorCode } from '../utils/format.js';
import EditSponsor from './EditSponsor.jsx';

export default function SponsorPortal() {
  const dialog = useDialog();
  const [user, setUser] = useSession('user');
  const [sponsor, setSponsor] = useSession('sponsor', () => user);
  const [contracts, setContracts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('ALL'); // 'ALL', 'SIGNED', 'WAITING'
  const [selectedContract, setSelectedContract] = useState(null);
  const [editingProfile, setEditingProfile] = useState(false);

  const sponsorId = sponsor?.id || user?.id;

  const loadContracts = async () => {
    if (!sponsorId) return;
    setLoading(true);
    try {
      const list = await HTTPClient.fetch('GET', `/sponsors/${sponsorId}/contracts`);
      setContracts(list || []);
    } catch (e) {
      await dialog.alert(e.message, { type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContracts();
  }, [sponsorId]); // eslint-disable-line react-hooks/exhaustive-deps

  const filteredContracts = contracts.filter((c) => {
    if (tab === 'SIGNED') return c.status === 'Đã ký';
    if (tab === 'WAITING') return c.status === 'Chờ ký';
    return true;
  });

  const totalValue = contracts.reduce((sum, c) => sum + (c.value || 0), 0);
  const signedCount = contracts.filter((c) => c.status === 'Đã ký').length;
  const waitingCount = contracts.filter((c) => c.status === 'Chờ ký').length;

  const handleProfileUpdated = (updated) => {
    setSponsor((prev) => ({ ...prev, ...updated }));
    setUser((prev) => ({ ...prev, ...updated }));
    setEditingProfile(false);
  };

  const tabs = [
    ['ALL', 'Tất cả', contracts.length],
    ['SIGNED', 'Đã ký', signedCount],
    ['WAITING', 'Chờ ký', waitingCount],
  ];

  return (
    <main className="main wide">
      <PageHeader
        title="Trang chủ Nhà tài trợ"
        subtitle="Thông tin doanh nghiệp và các hợp đồng tài trợ (cả chờ ký và đã ký)."
      />

      <div className="layout-portal">
        {/* Cột trái: hồ sơ nhà tài trợ */}
        <aside className="portal-side">
          <section className="card profile-card">
            <div className="profile-top">
              <span className="avatar-xl">{initials(sponsor?.name)}</span>
              <h2>{sponsor?.name}</h2>
              <div className="profile-tags">
                <span className="chip">{sponsorCode(sponsor?.id)}</span>
                <StatusBadge status={sponsor?.status || 'Đang hợp tác'} />
              </div>
            </div>
            <div className="card-body">
              <dl className="dl cols-1">
                <div><dt>Mã số thuế</dt><dd className="mono"><Icon name="invoice" size={16} />{sponsor?.taxCode}</dd></div>
                <div><dt>Lĩnh vực</dt><dd><Icon name="sponsor" size={16} />{sponsor?.field}</dd></div>
                <div><dt>Email</dt><dd><Icon name="mail" size={16} />{sponsor?.email}</dd></div>
                <div><dt>Số điện thoại</dt><dd><Icon name="info" size={16} />{sponsor?.tel}</dd></div>
                <div><dt>Địa chỉ</dt><dd><Icon name="home" size={16} />{sponsor?.address}</dd></div>
              </dl>
            </div>
            <div className="card-foot">
              <button type="button" className="btn" onClick={() => setEditingProfile(true)}>
                <Icon name="pen" />Cập nhật thông tin liên hệ
              </button>
            </div>
          </section>
        </aside>

        <div className="portal-content">
          {/* Thống kê hợp đồng */}
          <div className="grid cols-4">
            <div className="card stat-card">
              <span className="stat-icon"><Icon name="contract" /></span>
              <div className="stat-text"><small>Tổng số hợp đồng</small><b>{contracts.length}</b></div>
            </div>
            <div className="card stat-card success">
              <span className="stat-icon"><Icon name="checkCircle" /></span>
              <div className="stat-text"><small>Hợp đồng đã ký</small><b>{signedCount}</b></div>
            </div>
            <div className="card stat-card warning">
              <span className="stat-icon"><Icon name="pen" /></span>
              <div className="stat-text"><small>Hợp đồng chờ ký</small><b>{waitingCount}</b></div>
            </div>
            <div className="card stat-card brand">
              <span className="stat-icon"><Icon name="wallet" /></span>
              <div className="stat-text"><small>Tổng giá trị tài trợ</small><b>{formatMoney(totalValue)}</b></div>
            </div>
          </div>

          {/* Danh sách hợp đồng tài trợ */}
          <section className="card">
            <div className="card-head" style={{ justifyContent: 'space-between', flexWrap: 'wrap', gap: 12 }}>
              <h2><Icon name="contract" />Danh sách hợp đồng tài trợ</h2>
              <div className="segmented" role="tablist">
                {tabs.map(([key, label, n]) => (
                  <button key={key} type="button" role="tab" aria-selected={tab === key}
                    className={tab === key ? 'active' : ''} onClick={() => setTab(key)}>
                    {label} ({n})
                  </button>
                ))}
              </div>
            </div>

            <div className="card-body flush table-wrap">
              <table>
                <thead>
                  <tr>
                    <th className="tt">TT</th>
                    <th>Mã hợp đồng</th>
                    <th>Mùa giải</th>
                    <th className="num">Giá trị</th>
                    <th>Ngày lập</th>
                    <th>Ngày ký</th>
                    <th>Thời hạn hiệu lực</th>
                    <th>Trạng thái</th>
                    <th className="num">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {loading && (
                    <tr><td colSpan={9} className="empty">Đang tải danh sách hợp đồng...</td></tr>
                  )}
                  {!loading && filteredContracts.length === 0 && (
                    <tr>
                      <td colSpan={9}>
                        <EmptyState icon="contract" title="Không có hợp đồng nào">
                          {tab === 'WAITING'
                            ? 'Không có hợp đồng nào đang chờ ký.'
                            : tab === 'SIGNED'
                            ? 'Chưa có hợp đồng nào đã ký.'
                            : 'Nhà tài trợ chưa có hợp đồng nào trong hệ thống.'}
                        </EmptyState>
                      </td>
                    </tr>
                  )}
                  {!loading && filteredContracts.map((c, i) => (
                    <tr key={c.id}>
                      <td className="tt">{i + 1}</td>
                      <td><span className="chip">{c.barcode}</span></td>
                      <td className="cell-title nowrap">{c.season?.name}</td>
                      <td className="num cell-title nowrap">{formatMoney(c.value)}</td>
                      <td>{formatDate(c.createdDate)}</td>
                      <td>{c.signedDate ? formatDate(c.signedDate) : <span className="muted">Chưa ký</span>}</td>
                      <td className="nowrap">{formatDate(c.effectiveFrom)} – {formatDate(c.effectiveTo)}</td>
                      <td>
                        <StatusBadge status={c.status} />
                      </td>
                      <td className="num">
                        <button
                          type="button"
                          className="btn btn-sm"
                          onClick={() => setSelectedContract(c)}
                        >
                          <Icon name="eye" size={14} />Xem chi tiết
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </div>
      </div>

      {/* Modal chi tiết hợp đồng */}
      {selectedContract && (
        <Modal
          size="lg"
          title={`Hợp đồng tài trợ ${selectedContract.barcode}`}
          subtitle={<>{selectedContract.season?.name} · <StatusBadge status={selectedContract.status} /></>}
          onClose={() => setSelectedContract(null)}
          footer={
            <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
              <button type="button" className="btn" onClick={() => window.print()}>
                <Icon name="print" />In hợp đồng
              </button>
              <button type="button" className="btn btn-dark" onClick={() => setSelectedContract(null)}>
                Đóng
              </button>
            </div>
          }
        >
          <dl className="summary dl cols-2" style={{ marginBottom: 18 }}>
            <div><dt>Mã hợp đồng</dt><dd className="mono">{selectedContract.barcode}</dd></div>
            <div><dt>Trạng thái</dt><dd><StatusBadge status={selectedContract.status} /></dd></div>
            <div><dt>Mùa giải</dt><dd><b>{selectedContract.season?.name}</b></dd></div>
            <div><dt>Giá trị hợp đồng</dt><dd className="cell-title">{formatMoney(selectedContract.value)}</dd></div>
            <div><dt>Thời gian hiệu lực</dt><dd>{formatDate(selectedContract.effectiveFrom)} đến {formatDate(selectedContract.effectiveTo)}</dd></div>
            <div><dt>Ngày lập / Ngày ký</dt><dd>{formatDate(selectedContract.createdDate)} / {selectedContract.signedDate ? formatDate(selectedContract.signedDate) : 'Chưa ký'}</dd></div>
            <div><dt>Đại diện Ban tổ chức</dt><dd>{selectedContract.organizerRepresentative}</dd></div>
            <div><dt>Đại diện Nhà tài trợ</dt><dd>{selectedContract.sponsorRepresentative}</dd></div>
          </dl>

          <div className="section-title" style={{ margin: '18px -24px 0' }}>
            <h3><Icon name="document" />Các điều khoản hợp đồng</h3>
            <span className="count">{selectedContract.listContractTerm?.length || 0} điều khoản</span>
          </div>
          <div className="table-wrap" style={{ margin: '0 -24px 18px' }}>
            <table>
              <thead>
                <tr>
                  <th className="tt">TT</th>
                  <th>Tiêu đề điều khoản</th>
                  <th>Nội dung chi tiết</th>
                  <th>Xử lý vi phạm</th>
                  <th>Ghi chú áp dụng</th>
                </tr>
              </thead>
              <tbody>
                {(!selectedContract.listContractTerm || selectedContract.listContractTerm.length === 0) && (
                  <tr><td colSpan={5} className="empty">Không có điều khoản riêng</td></tr>
                )}
                {selectedContract.listContractTerm?.map((ct, idx) => (
                  <tr key={ct.id || idx}>
                    <td className="tt">{idx + 1}</td>
                    <td className="cell-title">{ct.term?.title}</td>
                    <td>{ct.term?.details}</td>
                    <td><span className="text-danger">{ct.term?.punishment || 'Theo quy định'}</span></td>
                    <td className="muted">{ct.notes || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="section-title" style={{ margin: '18px -24px 0' }}>
            <h3><Icon name="trophy" />Các quyền lợi của nhà tài trợ</h3>
            <span className="count">{selectedContract.listContractBenefit?.length || 0} quyền lợi</span>
          </div>
          <div className="table-wrap" style={{ margin: '0 -24px -22px' }}>
            <table>
              <thead>
                <tr>
                  <th className="tt">TT</th>
                  <th>Tiêu đề quyền lợi</th>
                  <th>Mô tả quyền lợi</th>
                  <th className="num">Số lượng / Giá trị</th>
                  <th>Ghi chú</th>
                </tr>
              </thead>
              <tbody>
                {(!selectedContract.listContractBenefit || selectedContract.listContractBenefit.length === 0) && (
                  <tr><td colSpan={5} className="empty">Không có quyền lợi riêng</td></tr>
                )}
                {selectedContract.listContractBenefit?.map((cb, idx) => (
                  <tr key={cb.id || idx}>
                    <td className="tt">{idx + 1}</td>
                    <td className="cell-title">{cb.benefit?.title}</td>
                    <td>{cb.benefit?.description}</td>
                    <td className="num cell-title">
                      {cb.value != null ? `${cb.value} ${cb.unit || ''}` : '—'}
                    </td>
                    <td className="muted">{cb.notes || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Modal>
      )}

      {editingProfile && (
        <EditSponsor
          sponsor={sponsor}
          onClose={() => setEditingProfile(false)}
          onUpdated={handleProfileUpdated}
        />
      )}
    </main>
  );
}
