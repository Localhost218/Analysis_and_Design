import Icon from '../components/Icon.jsx';
import Modal from '../components/Modal.jsx';
import { StatusBadge } from '../components/Badges.jsx';
import useDialog from '../hooks/useDialog.js';
import { formatDateTime, formatMoney, seasonRaceLabel } from '../utils/format.js';

/**
 * Giao diện Invoice: cửa sổ nổi trên RewardPayment sau khi thanh toán thành công (không có route riêng).
 * Không gọi backend, hiển thị từ invoice (RewardInvoice đã lưu, "lấy từ session").
 * onClose: đóng hóa đơn, ở lại trang trả thưởng; onBack (btnBack): quay lại OrganizerHome.
 */
export default function Invoice({ invoice, onClose, onBack }) {
  const dialog = useDialog();

  const handleResendEmail = async () => {
    await dialog.alert(`Đã gửi lại thông tin hóa đơn ${invoice.barcode} đến email của từng tay đua.`,
      { type: 'success', title: 'Gửi email thành công' });
  };

  return (
    <Modal
      size="lg"
      icon="checkCircle"
      title="Hóa đơn trả thưởng"
      subtitle={<>Mã hóa đơn <span className="chip">{invoice.barcode}</span> · {formatDateTime(invoice.paidAt)}</>}
      onClose={onClose}
      footer={<>
        <button type="button" className="btn" onClick={() => window.print()}><Icon name="print" />In hóa đơn</button>
        <button type="button" className="btn" onClick={handleResendEmail}><Icon name="mail" />Gửi lại email</button>
        <button type="button" className="btn" onClick={onClose}>Tiếp tục trả thưởng</button>
        <button id="btnBack" type="button" className="btn btn-dark" onClick={onBack}><Icon name="home" />Quay lại trang chủ</button>
      </>}
    >
      <div className="receipt">
        <div className="receipt-head" style={{ paddingTop: 4 }}>
          <StatusBadge status={invoice.status} />
          <div className="receipt-amount">{formatMoney(invoice.amount)}</div>
          <div className="muted">{invoice.listRegisteredReceipt.length} khoản trả thưởng</div>
        </div>

        <dl className="summary receipt-meta" style={{ margin: '18px 0' }}>
          <div><dt>Mã hóa đơn</dt><dd>{invoice.barcode}</dd></div>
          <div><dt>Thời điểm thanh toán</dt><dd>{formatDateTime(invoice.paidAt)}</dd></div>
          <div><dt>Phương thức</dt><dd>{invoice.paymentMethod}</dd></div>
          <div><dt>Người thực hiện</dt><dd>{invoice.organizer?.fullName}</dd></div>
          <div><dt>Trạng thái</dt><dd>{invoice.status}</dd></div>
        </dl>
      </div>

      <div className="section-title" style={{ margin: '0 -24px' }}>
        <h3>Các khoản trả thưởng</h3>
      </div>
      <div className="table-wrap" style={{ margin: '0 -24px' }}>
        <table id="tblReceipt">
          <thead><tr><th className="tt">TT</th><th>Mùa giải – Chặng</th><th>Tay đua</th><th className="num">Tiền thưởng</th></tr></thead>
          <tbody>
            {invoice.listRegisteredReceipt.map((r, i) => (
              <tr key={r.id}>
                <td className="tt">{i + 1}</td>
                <td><span className="chip">{seasonRaceLabel(r.race)}</span> <span className="cell-sub">{r.race.name}</span></td>
                <td className="cell-title">{r.registeredDriver.driver.fullName}</td>
                <td className="num">{formatMoney(r.reward)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="receipt-total" style={{ margin: '0 -24px -22px' }}><span>Tổng tiền</span><b>{formatMoney(invoice.amount)}</b></div>
    </Modal>
  );
}
