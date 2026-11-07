import { useState } from 'react';
import HTTPClient from '../HTTPClient.js';
import FormModal, { Field } from '../components/FormModal.jsx';
import useDialog from '../hooks/useDialog.js';
import { BENEFIT_TYPES } from '../utils/options.js';

const P = 'newBenefit';

/** Giao diện NewBenefit: cửa sổ nổi trên ContractSigning. onCreated(benefit) khi thêm thành công. */
export default function NewBenefit({ onClose, onCreated }) {
  const dialog = useDialog();
  const [form, setForm] = useState({ title: '', description: '', type: '' });
  const [submitting, setSubmitting] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const btnAdd = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const benefit = await HTTPClient.fetch('POST', '/benefits', form);
      await dialog.alert(`Đã thêm quyền lợi "${benefit.title}" vào hợp đồng.`, { type: 'success', title: 'Thêm thành công' });
      onCreated(benefit);
    } catch (err) {
      await dialog.alert(err.message, { type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <FormModal prefix={P} icon="flag" title="Thêm quyền lợi"
      subtitle="Quyền lợi mới được lưu vào danh mục và thêm vào hợp đồng đang soạn."
      onClose={onClose} onSubmit={btnAdd} submitting={submitting}>
      <Field prefix={P} name="txtTitle" label="Tiêu đề" wide>
        {(a) => <input {...a} value={form.title} onChange={set('title')} required />}
      </Field>
      <Field prefix={P} name="txtDescription" label="Mô tả">
        {(a) => <input {...a} value={form.description} onChange={set('description')} required />}
      </Field>
      <Field prefix={P} name="txtType" label="Loại">
        {(a) => (
          <select {...a} value={form.type} onChange={set('type')} required>
            <option value="">Chọn loại quyền lợi</option>
            {BENEFIT_TYPES.map((t) => <option key={t}>{t}</option>)}
          </select>
        )}
      </Field>
    </FormModal>
  );
}
