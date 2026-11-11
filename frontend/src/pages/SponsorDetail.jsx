import { useEffect, useState } from 'react';
import HTTPClient from '../HTTPClient.js';
import Icon from '../components/Icon.jsx';
import Modal from '../components/Modal.jsx';
import { EmptyState, initials } from '../components/Layout.jsx';
import { StatusBadge } from '../components/Badges.jsx';
import useDialog from '../hooks/useDialog.js';
import { formatDate, formatMoney, sponsorCode } from '../utils/format.js';
import EditSponsor from './EditSponsor.jsx';

/**
 * Giao diện SponsorDetail: cửa sổ nổi trên SponsorManagement (không có route riêng).
 * Nhận sponsor (đã "lưu vào session" ở SponsorManagement), gọi getSponsorInfo để lấy hợp đồng đã ký.
 * Hỗ trợ cập nhật (subUpdate) và xóa (subDelete) nếu không ở chế độ readOnly.
 */
export default function SponsorDetail({ sponsor: selected, onClose, onUpdated, onDeleted, readOnly = false }) {
  const dialog = useDialog();
  const [sponsor, setSponsor] = useState(selected);
  const [editing, setEditing] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    HTTPClient.fetch('GET', `/sponsors/${selected.id}`)
      .then(setSponsor)
      .catch((e) => dialog.alert(e.message, { type: 'error' }));
  }, [selected.id]); // eslint-disable-line react-hooks/exhaustive-deps

  const contracts = sponsor?.listContract;

  const handleUpdate = (updated) => {
    setSponsor((prev) => ({ ...prev, ...updated }));
    setEditing(false);
    if (onUpdated) onUpdated(updated);
  };

  const handleDelete = async () => {
    const ok = await dialog.confirm(
      `Bạn có chắc chắn muốn xóa nhà tài trợ "${sponsor.name}" khỏi hệ thống?`,
      { title: 'Xác nhận xóa nhà tài trợ', confirmText: 'Xóa nhà tài trợ' }
    );
    if (!ok) return;

    setDeleting(true);
    try {
      await HTTPClient.fetch('DELETE', `/sponsors/${sponsor.id}`);
      await dialog.alert(`Đã xóa nhà tài trợ "${sponsor.name}" khỏi hệ thống.`, { type: 'success', title: 'Xóa thành công' });
      if (onDeleted) onDeleted(sponsor.id);
      onClose();
    } catch (err) {
      await dialog.alert(err.message, { type: 'error', title: 'Không thể xóa nhà tài trợ' });
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <Modal
        size="lg"
        title="Thông tin nhà tài trợ"
        subtitle={<>{sponsor.name} · <span className="chip">{sponsorCode(sponsor.id)}</span></>}
        onClose={onClose}
        footer={
          <div style={{ display: 'flex', width: '100%', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              {!readOnly && (
                <button
                  id="sponsorDetail-btnDelete"
                  name="subDelete"
                  type="button"
                  className="btn btn-danger"
                  onClick={handleDelete}
                  disabled={deleting}
                >
                  <Icon name="trash" />Xóa nhà tài trợ
                </button>
              )}
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              {!readOnly && (
                <button
                  id="sponsorDetail-btnUpdate"
                  name="subUpdate"
                  type="button"
                  className="btn btn-primary"
                  onClick={() => setEditing(true)}
                >
                  <Icon name="pencil" />Sửa thông tin
                </button>
              )}
              <button id="sponsorDetail-btnCancel" name="btnCancel" type="button" className="btn" onClick={onClose}>
                <Icon name="back" />Quay lại
              </button>
            </div>
          </div>
        }
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

      {editing && (
        <EditSponsor
          sponsor={sponsor}
          onClose={() => setEditing(false)}
          onUpdated={handleUpdate}
        />
      )}
    </>
  );
}
