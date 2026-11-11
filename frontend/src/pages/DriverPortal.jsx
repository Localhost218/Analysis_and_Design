import { useEffect, useState } from 'react';
import HTTPClient from '../HTTPClient.js';
import Icon from '../components/Icon.jsx';
import { EmptyState, PageHeader, initials } from '../components/Layout.jsx';
import { RankBadge, StatusBadge } from '../components/Badges.jsx';
import useSession from '../hooks/useSession.js';
import useDialog from '../hooks/useDialog.js';
import Invoice from './Invoice.jsx';
import EditDriver from './EditDriver.jsx';
import BankAccountForm from './BankAccountForm.jsx';
import { formatDate, formatMoney } from '../utils/format.js';

export default function DriverPortal() {
  const dialog = useDialog();
  const [user, setUser] = useSession('user');
  const [driver, setDriver] = useSession('driver', () => user);
  const [driverInfo, setDriverInfo] = useState(driver);
  const [rewards, setRewards] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewInvoice, setViewInvoice] = useState(null);
  const [loadingInvoice, setLoadingInvoice] = useState(false);
  const [editingProfile, setEditingProfile] = useState(false);
  const [editingAccount, setEditingAccount] = useState(null); // 'new' | BankAccount | null

  const driverId = driver?.id || user?.id;

  useEffect(() => {
    if (!driverId) return;
    setLoading(true);

    // Lấy thông tin chi tiết tay đua kèm tài khoản ngân hàng
    HTTPClient.fetch('GET', `/drivers/${driverId}`)
      .then((info) => setDriverInfo(info))
      .catch((e) => console.error(e));

    // Lấy danh sách kết quả & tiền thưởng
    HTTPClient.fetch('GET', `/drivers/${driverId}/rewards`)
      .then((list) => setRewards(list || []))
      .catch((e) => dialog.alert(e.message, { type: 'error' }))
      .finally(() => setLoading(false));
  }, [driverId]); // eslint-disable-line react-hooks/exhaustive-deps

  const openInvoice = async (invoiceId) => {
    if (!invoiceId) return;
    setLoadingInvoice(true);
    try {
      const inv = await HTTPClient.fetch('GET', `/invoices/${invoiceId}`);
      setViewInvoice(inv);
    } catch (e) {
      await dialog.alert(e.message, { type: 'error', title: 'Không thể mở hóa đơn' });
    } finally {
      setLoadingInvoice(false);
    }
  };

  const listBankAccount = driverInfo?.listBankAccount || [];
  const defaultAccount = listBankAccount.find((a) => a.isDefault);

  const totalPaid = rewards
    .filter((r) => r.paymentStatus === 'Đã trả')
    .reduce((sum, r) => sum + (r.reward || 0), 0);

  const totalPending = rewards
    .filter((r) => r.paymentStatus === 'Chưa trả' && r.reward > 0)
    .reduce((sum, r) => sum + (r.reward || 0), 0);

  const paidCount = rewards.filter((r) => r.paymentStatus === 'Đã trả').length;
  const pendingCount = rewards.filter((r) => r.paymentStatus === 'Chưa trả' && r.reward > 0).length;

  // Cập nhật tay đua (kèm listBankAccount) sau khi sửa thông tin / tài khoản, đồng bộ session để header đổi tên
  const applyDriver = (updated) => {
    setDriverInfo(updated);
    setDriver((prev) => ({ ...prev, ...updated }));
    setUser((prev) => ({ ...prev, ...updated }));
  };

  const setAsDefault = async (a) => {
    try {
      applyDriver(await HTTPClient.fetch('PUT', `/drivers/${driverId}/bank-accounts/${a.id}`,
        { bank: { id: a.bank.id }, accountNumber: a.accountNumber, accountHolder: a.accountHolder, isDefault: true }));
    } catch (e) {
      await dialog.alert(e.message, { type: 'error' });
    }
  };

  const deleteAccount = async (a) => {
    if (!(await dialog.confirm(`Xóa tài khoản ${a.accountNumber} (${a.bank?.name})?`, { title: 'Xóa tài khoản', confirmText: 'Xóa' }))) return;
    try {
      applyDriver(await HTTPClient.fetch('DELETE', `/drivers/${driverId}/bank-accounts/${a.id}`));
    } catch (e) {
      await dialog.alert(e.message, { type: 'error' });
    }
  };

  return (
    <main className="main wide">
      <PageHeader
        title="Trang chủ Tay đua"
        subtitle="Theo dõi kết quả thi đấu, tiền thưởng từng chặng, hóa đơn chi trả và tài khoản nhận thưởng."
      />

      <div className="layout-portal">
        {/* Cột trái: hồ sơ tay đua + tài khoản nhận thưởng mặc định */}
        <aside className="portal-side">
          <section className="card profile-card">
            <div className="profile-top">
              <span className="avatar-xl">{initials(driverInfo?.fullName)}</span>
              <h2>{driverInfo?.fullName}</h2>
              <div className="profile-tags">
                <span className="racing-no">#{driverInfo?.racingNumber}</span>
                <span className="chip">{driverInfo?.username}</span>
              </div>
            </div>
            <div className="card-body">
              <dl className="dl cols-1">
                <div><dt>Ngày sinh</dt><dd><Icon name="calendar" size={16} />{driverInfo?.dateOfBirth ? formatDate(driverInfo.dateOfBirth) : '—'}</dd></div>
                <div><dt>Quốc tịch</dt><dd><Icon name="flag" size={16} />{driverInfo?.nationality || '—'}</dd></div>
                <div><dt>Số xe</dt><dd><Icon name="star" size={16} />{driverInfo?.racingNumber ?? '—'}</dd></div>
              </dl>
            </div>
            <div className="card-foot">
              <button id="btnEditDriver" type="button" className="btn" onClick={() => setEditingProfile(true)} disabled={!driverInfo}>
                <Icon name="pen" />Chỉnh sửa thông tin
              </button>
            </div>
          </section>

          {defaultAccount ? (
            <div className="default-account">
              <small><Icon name="wallet" size={14} /> Tài khoản nhận thưởng mặc định</small>
              <b>{defaultAccount.accountNumber}</b>
              <span>{defaultAccount.bank?.name} · {defaultAccount.accountHolder}</span>
            </div>
          ) : (
            <div className="default-account missing">
              <small><Icon name="alert" size={14} /> Chưa có tài khoản nhận thưởng mặc định</small>
              <span>Thêm tài khoản ngân hàng để nhận thưởng chuyển khoản online.</span>
            </div>
          )}
        </aside>

        <div className="portal-content">
          {/* Thống kê tiền thưởng */}
          <div className="grid cols-4">
            <div className="card stat-card">
              <span className="stat-icon"><Icon name="flag" /></span>
              <div className="stat-text"><small>Tổng chặng đã đua</small><b>{rewards.length}</b></div>
            </div>
            <div className="card stat-card success">
              <span className="stat-icon"><Icon name="checkCircle" /></span>
              <div className="stat-text"><small>Đã nhận · {paidCount} chặng</small><b>{formatMoney(totalPaid)}</b></div>
            </div>
            <div className="card stat-card warning">
              <span className="stat-icon"><Icon name="season" /></span>
              <div className="stat-text"><small>Chờ chi trả · {pendingCount} chặng</small><b>{formatMoney(totalPending)}</b></div>
            </div>
            <div className="card stat-card brand">
              <span className="stat-icon"><Icon name="trophy" /></span>
              <div className="stat-text"><small>Tổng tiền thưởng</small><b>{formatMoney(totalPaid + totalPending)}</b></div>
            </div>
          </div>

          {/* Bảng tiền thưởng và kết quả thi đấu */}
          <section className="card">
            <div className="card-head" style={{ justifyContent: 'space-between' }}>
              <h2><Icon name="trophy" />Bảng tiền thưởng và trạng thái chi trả</h2>
              <span className="count">{rewards.length} chặng đua</span>
            </div>
            <div className="card-body flush table-wrap">
              <table id="tblDriverReward">
                <thead>
                  <tr>
                    <th className="tt">TT</th>
                    <th>Mùa giải</th>
                    <th>Chặng đua</th>
                    <th>Ngày thi đấu</th>
                    <th>Thứ hạng</th>
                    <th className="num">Vòng</th>
                    <th className="num">Điểm</th>
                    <th className="num">Tiền thưởng</th>
                    <th>Trạng thái</th>
                    <th className="num">Hóa đơn</th>
                  </tr>
                </thead>
                <tbody>
                  {loading && (
                    <tr><td colSpan={10} className="empty">Đang tải bảng tiền thưởng...</td></tr>
                  )}
                  {!loading && rewards.length === 0 && (
                    <tr>
                      <td colSpan={10}>
                        <EmptyState icon="trophy" title="Chưa có kết quả thi đấu">
                          Tay đua chưa có kết quả thi đấu nào trong hệ thống.
                        </EmptyState>
                      </td>
                    </tr>
                  )}
                  {!loading && rewards.map((r, i) => {
                    const isPaid = r.paymentStatus === 'Đã trả';
                    const invoiceId = r.rewardInvoice?.id;
                    const invoiceCode = r.rewardInvoice?.barcode || (invoiceId ? `HD${String(invoiceId).padStart(3, '0')}` : null);
    
                    return (
                      <tr key={r.id}>
                        <td className="tt">{i + 1}</td>
                        <td><span className="chip">{r.race?.season?.name}</span></td>
                        <td className="cell-title">
                          <div>{r.race?.name}</div>
                          <small className="muted">{r.race?.location}</small>
                        </td>
                        <td>{r.race?.startTime ? formatDate(r.race.startTime) : '—'}</td>
                        <td><RankBadge rank={r.rank} completedStatus={r.completedStatus} /></td>
                        <td className="num">{r.completedLap != null ? r.completedLap : '—'}</td>
                        <td className="num">{r.score != null ? r.score : 0}</td>
                        <td className="num cell-title">{formatMoney(r.reward)}</td>
                        <td><StatusBadge status={r.paymentStatus} /></td>
                        <td className="num">
                          {isPaid && invoiceId ? (
                            <button
                              type="button"
                              className="btn btn-sm btn-primary"
                              disabled={loadingInvoice}
                              onClick={() => openInvoice(invoiceId)}
                              title="Xem hóa đơn trả thưởng"
                            >
                              <Icon name="invoice" size={13} />
                              {invoiceCode ? `Xem ${invoiceCode}` : 'Xem hóa đơn'}
                            </button>
                          ) : (
                            <span className="muted" style={{ fontSize: 12 }}>Chưa có hóa đơn</span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </section>


          {/* Bảng tài khoản ngân hàng: tay đua tự thêm / sửa / xóa, luôn có đúng 1 tài khoản mặc định */}
          <section className="card">
            <div className="card-head" style={{ justifyContent: 'space-between' }}>
              <h2><Icon name="bank" />Tài khoản ngân hàng</h2>
              <button id="btnNewBankAccount" type="button" className="btn btn-sm btn-primary" onClick={() => setEditingAccount('new')} disabled={!driverInfo}>
                <Icon name="plus" size={14} />Thêm tài khoản
              </button>
            </div>
            <div className="card-body flush table-wrap">
              <table id="tblBankAccount">
                <thead>
                  <tr>
                    <th className="tt">TT</th>
                    <th>Ngân hàng</th>
                    <th>Số tài khoản</th>
                    <th>Chủ tài khoản</th>
                    <th>Trạng thái</th>
                    <th className="num">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {listBankAccount.length === 0 && (
                    <tr>
                      <td colSpan={6}>
                        <EmptyState icon="bank" title="Chưa có tài khoản ngân hàng">
                          Bấm "Thêm tài khoản" để khai báo tài khoản nhận thưởng.
                        </EmptyState>
                      </td>
                    </tr>
                  )}
                  {listBankAccount.map((a, i) => (
                    <tr key={a.id}>
                      <td className="tt">{i + 1}</td>
                      <td className="cell-title">{a.bank?.name}</td>
                      <td><span className="chip">{a.accountNumber}</span></td>
                      <td>{a.accountHolder}</td>
                      <td>
                        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                          {a.isDefault && <StatusBadge status="Mặc định" />}
                          <StatusBadge status={a.status} />
                        </div>
                      </td>
                      <td className="num">
                        <div className="row-actions">
                          {!a.isDefault && (
                            <button type="button" className="btn btn-sm" onClick={() => setAsDefault(a)}>
                              <Icon name="star" size={14} />Đặt mặc định
                            </button>
                          )}
                          <button type="button" className="btn btn-sm" onClick={() => setEditingAccount(a)}>
                            <Icon name="pen" size={14} />Sửa
                          </button>
                          <button type="button" className="btn btn-sm btn-ghost" onClick={() => deleteAccount(a)}
                            disabled={a.isDefault} title={a.isDefault ? 'Không thể xóa tài khoản mặc định' : 'Xóa tài khoản'}>
                            <Icon name="trash" size={14} />Xóa
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </div>
      </div>

      {editingProfile && (
        <EditDriver
          driver={driverInfo}
          onClose={() => setEditingProfile(false)}
          onUpdated={(d) => { applyDriver(d); setEditingProfile(false); }}
        />
      )}

      {editingAccount && (
        <BankAccountForm
          driverId={driverId}
          account={editingAccount === 'new' ? null : editingAccount}
          forceDefault={!defaultAccount}
          onClose={() => setEditingAccount(null)}
          onSaved={(d) => { applyDriver(d); setEditingAccount(null); }}
        />
      )}

      {/* Cửa sổ nổi hiển thị Hóa đơn trả thưởng */}
      {viewInvoice && (
        <Invoice
          invoice={viewInvoice}
          readOnly
          onClose={() => setViewInvoice(null)}
          onBack={() => setViewInvoice(null)}
        />
      )}
    </main>
  );
}
