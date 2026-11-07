import { useEffect, useState } from 'react';
import HTTPClient from '../HTTPClient.js';
import Icon from '../components/Icon.jsx';
import Modal from '../components/Modal.jsx';
import { EmptyState, initials } from '../components/Layout.jsx';
import { StatusBadge } from '../components/Badges.jsx';
import useDialog from '../hooks/useDialog.js';
import { formatDate, formatMoney, sponsorCode } from '../utils/format.js';

/**
 * Giao diện SponsorDetail: cửa sổ nổi trên SponsorManagement (không có route riêng).
 * Nhận sponsor (đã "lưu vào session" ở SponsorManagement), gọi getSponsorInfo để lấy hợp đồng đã ký.
 */
export default function SponsorDetail({ sponsor: selected, onClose }) {
  const dialog = useDialog();
  const [sponsor, setSponsor] = useState(selected);

  useEffect(() => {
    HTTPClient.fetch('GET', `/sponsors/${selected.id}`)
      .then(setSponsor)
      .catch((e) => dialog.alert(e.message, { type: 'error' }));
  }, [selected.id]); // eslint-disable-line react-hooks/exhaustive-deps

  const contracts = sponsor?.listContract;

  return (
    <Modal
      size="lg"
      title="Thông tin nhà tài trợ"
      subtitle={<>{sponsor.name} · <span className="chip">{sponsorCode(sponsor.id)}</span></>}
      onClose={onClose}
      footer={<button id="sponsorDetail-btnCancel" name="btnCancel" type="button" className="btn" onClick={onClose}>
        <Icon name="back" />Quay lại
      </button>}
    >
      <div className="profile" style={{ marginBottom: 18, justifyContent: 'space-between' }}>
        <div className="profile">
          <span className="avatar">{initials(sponsor.name)}</span>
          <div>
            <h2>{sponsor.name}</h2>
            <span className="muted">{sponsor.field}</span>
          </div>
        </div>
        <StatusBadge status={sponsor.status} />
      </div>
      <dl id="sponsorDetail" className="dl">
        <div><dt>Tên nhà tài trợ</dt><dd>{sponsor.name}</dd></div>
        <div><dt>Mã số thuế</dt><dd className="mono">{sponsor.taxCode}</dd></div>
        <div><dt>Lĩnh vực</dt><dd>{sponsor.field}</dd></div>
        <div><dt>Địa chỉ</dt><dd>{sponsor.address}</dd></div>
        <div><dt>Email</dt><dd>{sponsor.email}</dd></div>
        <div><dt>Số điện thoại</dt><dd>{sponsor.tel}</dd></div>
      </dl>

      <div className="section-title" style={{ margin: '22px -24px 0' }}>
        <h3><Icon name="contract" />Danh sách hợp đồng đã ký</h3>
        {contracts && <span className="count">{contracts.length} hợp đồng</span>}
      </div>
      <div className="table-wrap" style={{ margin: '0 -24px -22px' }}>
        <table id="tblContract">
          <thead>
            <tr><th className="tt">TT</th><th>Mã hợp đồng</th><th>Mùa giải</th><th className="num">Giá trị</th><th>Ngày ký</th><th>Hiệu lực đến</th></tr>
          </thead>
          <tbody>
            {contracts == null && <tr><td colSpan={6} className="empty">Đang tải...</td></tr>}
            {contracts?.length === 0 && (
              <tr><td colSpan={6}><EmptyState icon="contract" title="Chưa có">Nhà tài trợ chưa có hợp đồng nào đã ký</EmptyState></td></tr>
            )}
            {contracts?.map((c, i) => (
              <tr key={c.id}>
                <td className="tt">{i + 1}</td>
                <td><span className="chip">{c.barcode}</span></td>
                <td className="cell-title">{c.season?.name}</td>
                <td className="num">{formatMoney(c.value)}</td>
                <td>{formatDate(c.signedDate)}</td>
                <td>{formatDate(c.effectiveTo)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Modal>
  );
}
