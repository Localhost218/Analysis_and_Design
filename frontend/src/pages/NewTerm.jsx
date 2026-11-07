import { useState } from 'react';
import HTTPClient from '../HTTPClient.js';
import FormModal, { Field } from '../components/FormModal.jsx';
import useDialog from '../hooks/useDialog.js';
import { TERM_TYPES } from '../utils/options.js';

const P = 'newTerm';

/** Giao diện NewTerm: cửa sổ nổi trên ContractSigning. onCreated(term) khi thêm thành công. */
export default function NewTerm({ onClose, onCreated }) {
  const dialog = useDialog();
  const [form, setForm] = useState({ title: '', details: '', punishment: '', type: '' });
  const [submitting, setSubmitting] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const btnAdd = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const term = await HTTPClient.fetch('POST', '/terms', form);
      await dialog.alert(`Đã thêm điều khoản "${term.title}" vào hợp đồng.`, { type: 'success', title: 'Thêm thành công' });
      onCreated(term);
    } catch (err) {
      await dialog.alert(err.message, { type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <FormModal prefix={P} icon="contract" title="Thêm điều khoản"
      subtitle="Điều khoản mới được lưu vào danh mục và thêm vào hợp đồng đang soạn."
      onClose={onClose} onSubmit={btnAdd} submitting={submitting}>
      <Field prefix={P} name="txtTitle" label="Tiêu đề" wide>
        {(a) => <input {...a} value={form.title} onChange={set('title')} required />}
      </Field>
      <Field prefix={P} name="txtDetails" label="Nội dung" wide>
        {(a) => <input {...a} value={form.details} onChange={set('details')} required />}
      </Field>
      <Field prefix={P} name="txtPunishment" label="Xử lý vi phạm">
        {(a) => <input {...a} value={form.punishment} onChange={set('punishment')} />}
      </Field>
      <Field prefix={P} name="txtType" label="Loại">
        {(a) => (
          <select {...a} value={form.type} onChange={set('type')} required>
            <option value="">Chọn loại điều khoản</option>
            {TERM_TYPES.map((t) => <option key={t}>{t}</option>)}
          </select>
        )}
      </Field>
    </FormModal>
  );
}
