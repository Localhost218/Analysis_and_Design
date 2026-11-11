import { useState } from 'react';
import HTTPClient from '../HTTPClient.js';
import FormModal, { Field } from '../components/FormModal.jsx';
import useDialog from '../hooks/useDialog.js';
import { SPONSOR_FIELDS } from '../utils/options.js';

const P = 'editSponsor';

/** Giao diện EditSponsor: cửa sổ nổi cập nhật thông tin nhà tài trợ. */
export default function EditSponsor({ sponsor, onClose, onUpdated }) {
  const dialog = useDialog();
  const [form, setForm] = useState({
    name: sponsor.name || '',
    taxCode: sponsor.taxCode || '',
    field: sponsor.field || '',
    address: sponsor.address || '',
    tel: sponsor.tel || '',
    email: sponsor.email || '',
    status: sponsor.status || 'Đang hợp tác',
  });
  const [submitting, setSubmitting] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const btnSave = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const updated = await HTTPClient.fetch('PUT', `/sponsors/${sponsor.id}`, form);
      await dialog.alert(`Đã cập nhật thông tin nhà tài trợ "${updated.name}".`, { type: 'success', title: 'Cập nhật thành công' });
      onUpdated(updated);
    } catch (err) {
      await dialog.alert(err.message, { type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <FormModal prefix={P} icon="sponsor" title="Cập nhật thông tin nhà tài trợ"
      subtitle={`Cập nhật dữ liệu cho nhà tài trợ ${sponsor.name}`}
      onClose={onClose} onSubmit={btnSave} submitting={submitting}
      submitText="Lưu thay đổi">
      <Field prefix={P} name="txtSponsorName" label="Tên nhà tài trợ" wide>
        {(a) => <input {...a} value={form.name} onChange={set('name')} required />}
      </Field>
      <Field prefix={P} name="txtTaxCode" label="Mã số thuế" hint="10 hoặc 13 chữ số">
        {(a) => <input {...a} value={form.taxCode} onChange={set('taxCode')} required inputMode="numeric"
          pattern="\d{10}(\d{3})?" title="Mã số thuế gồm 10 hoặc 13 chữ số" />}
      </Field>
      <Field prefix={P} name="txtField" label="Lĩnh vực">
        {(a) => (
          <select {...a} value={form.field} onChange={set('field')} required>
            <option value="">Chọn lĩnh vực</option>
            {SPONSOR_FIELDS.map((f) => <option key={f}>{f}</option>)}
          </select>
        )}
      </Field>
      <Field prefix={P} name="txtAddress" label="Địa chỉ" wide>
        {(a) => <input {...a} value={form.address} onChange={set('address')} required />}
      </Field>
      <Field prefix={P} name="txtTel" label="Số điện thoại">
        {(a) => <input {...a} value={form.tel} onChange={set('tel')} required inputMode="tel"
          pattern="0\d{9,10}" title="Số điện thoại bắt đầu bằng 0, gồm 10–11 chữ số" />}
      </Field>
      <Field prefix={P} name="txtEmail" label="Email">
        {(a) => <input {...a} type="email" value={form.email} onChange={set('email')} required />}
      </Field>
      <Field prefix={P} name="txtStatus" label="Trạng thái hợp tác">
        {(a) => (
          <select {...a} value={form.status} onChange={set('status')} required>
            <option value="Đang hợp tác">Đang hợp tác</option>
            <option value="Ngừng hợp tác">Ngừng hợp tác</option>
          </select>
        )}
      </Field>
    </FormModal>
  );
}
