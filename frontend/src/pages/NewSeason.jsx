import { useState } from 'react';
import HTTPClient from '../HTTPClient.js';
import FormModal, { Field } from '../components/FormModal.jsx';
import useDialog from '../hooks/useDialog.js';

const P = 'newSeason';

/** Giao diện NewSeason: cửa sổ nổi trên ContractSigning. onCreated(season) khi thêm thành công. */
export default function NewSeason({ onClose, onCreated }) {
  const dialog = useDialog();
  const [form, setForm] = useState({ name: '', year: '', startDate: '', endDate: '' });
  const [submitting, setSubmitting] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const btnAdd = async (e) => {
    e.preventDefault();
    if (form.startDate >= form.endDate) {
      await dialog.alert('Ngày kết thúc phải sau ngày bắt đầu.', { type: 'error' });
      return;
    }
    setSubmitting(true);
    try {
      const season = await HTTPClient.fetch('POST', '/seasons', { ...form, year: Number(form.year) });
      await dialog.alert(`Đã thêm mùa giải "${season.name}" và chọn vào hợp đồng.`, { type: 'success', title: 'Thêm thành công' });
      onCreated(season);
    } catch (err) {
      await dialog.alert(err.message, { type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <FormModal prefix={P} icon="calendar" title="Thêm mùa giải"
      subtitle="Mùa giải mới sẽ được chọn ngay vào hợp đồng đang soạn."
      onClose={onClose} onSubmit={btnAdd} submitting={submitting}>
      <Field prefix={P} name="txtName" label="Tên mùa giải">
        {(a) => <input {...a} value={form.name} onChange={set('name')} placeholder="vd: F1 FIA 2027" required />}
      </Field>
      <Field prefix={P} name="txtYear" label="Năm">
        {(a) => <input {...a} type="number" min="1950" max="2100" value={form.year} onChange={set('year')} required />}
      </Field>
      <Field prefix={P} name="txtStartDate" label="Ngày bắt đầu">
        {(a) => <input {...a} type="date" value={form.startDate} onChange={set('startDate')} required />}
      </Field>
      <Field prefix={P} name="txtEndDate" label="Ngày kết thúc">
        {(a) => <input {...a} type="date" value={form.endDate} onChange={set('endDate')} required />}
      </Field>
    </FormModal>
  );
}
