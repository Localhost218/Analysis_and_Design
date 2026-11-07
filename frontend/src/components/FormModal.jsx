import Icon from './Icon.jsx';
import Modal from './Modal.jsx';

/**
 * Khung chung cho 4 giao diện NewSponsor, NewSeason, NewTerm, NewBenefit:
 * hiện dạng cửa sổ nổi trên ContractSigning, có nút Hủy (btnCancel) và Thêm (btnAdd).
 */
export default function FormModal({ prefix, title, subtitle, icon, onClose, onSubmit, submitting, children }) {
  return (
    <Modal
      title={title}
      subtitle={subtitle}
      icon={icon}
      onClose={onClose}
      onSubmit={onSubmit}
      footer={<>
        <button id={`${prefix}-btnCancel`} name="btnCancel" type="button" className="btn" onClick={onClose}>Hủy</button>
        <button id={`${prefix}-btnAdd`} name="btnAdd" type="submit" className="btn btn-primary" disabled={submitting}>
          <Icon name="plus" />{submitting ? 'Đang lưu...' : 'Thêm'}
        </button>
      </>}
    >
      <div className="grid cols-2">{children}</div>
    </Modal>
  );
}

/** Ô nhập trong form: tên theo báo cáo ở `name`, id có tiền tố để không trùng với trang ContractSigning. */
export function Field({ prefix, name, label, hint, wide, children }) {
  return (
    <div className="field" style={wide ? { gridColumn: '1 / -1' } : undefined}>
      <label htmlFor={`${prefix}-${name}`}>{label}</label>
      {children({ id: `${prefix}-${name}`, name })}
      {hint && <span className="hint">{hint}</span>}
    </div>
  );
}
