import { useState } from 'react';
import HTTPClient from '../HTTPClient.js';
import FormModal, { Field } from '../components/FormModal.jsx';
import useDialog from '../hooks/useDialog.js';

const P = 'editDriver';

/**
 * Giao diện EditDriver: cửa sổ nổi trên DriverPortal, tay đua tự sửa thông tin cá nhân của mình
 * (họ tên, ngày sinh, quốc tịch). Tên đăng nhập và số xe chỉ đọc.
 */
export default function EditDriver({ driver, onClose, onUpdated }) {
  const dialog = useDialog();
  const [form, setForm] = useState({
    fullName: driver.fullName || '',
    dateOfBirth: driver.dateOfBirth || '',
    nationality: driver.nationality || '',
  });
  const [submitting, setSubmitting] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const btnSave = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const updated = await HTTPClient.fetch('PUT', `/drivers/${driver.id}`, form);
      await dialog.alert('Đã cập nhật thông tin cá nhân.', { type: 'success', title: 'Cập nhật thành công' });
      onUpdated(updated);
    } catch (err) {
      await dialog.alert(err.message, { type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <FormModal prefix={P} icon="user" title="Chỉnh sửa thông tin cá nhân"
      subtitle="Chỉ thay đổi được họ tên, ngày sinh và quốc tịch"
      onClose={onClose} onSubmit={btnSave} submitting={submitting} submitText="Lưu thay đổi">
      <Field prefix={P} name="txtFullName" label="Họ và tên" wide>
        {(a) => <input {...a} value={form.fullName} onChange={set('fullName')} required autoFocus />}
      </Field>
      <Field prefix={P} name="txtDateOfBirth" label="Ngày sinh">
        {(a) => <input {...a} type="date" value={form.dateOfBirth} onChange={set('dateOfBirth')} required />}
      </Field>
      <Field prefix={P} name="txtNationality" label="Quốc tịch">
        {(a) => <input {...a} value={form.nationality} onChange={set('nationality')} required />}
      </Field>
      <Field prefix={P} name="txtUsername" label="Tên đăng nhập">
        {(a) => <input {...a} value={driver.username || ''} disabled />}
      </Field>
      <Field prefix={P} name="txtRacingNumber" label="Số xe" hint="Do Ban tổ chức cấp">
        {(a) => <input {...a} value={driver.racingNumber ?? ''} disabled />}
      </Field>
    </FormModal>
  );
}
