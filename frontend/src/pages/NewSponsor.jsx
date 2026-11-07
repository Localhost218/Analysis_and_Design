import { useState } from 'react';
import HTTPClient from '../HTTPClient.js';
import FormModal, { Field } from '../components/FormModal.jsx';
import useDialog from '../hooks/useDialog.js';
import { SPONSOR_FIELDS } from '../utils/options.js';

const P = 'newSponsor';

/** Giao diện NewSponsor: cửa sổ nổi trên ContractSigning. onCreated(sponsor) khi thêm thành công. */
export default function NewSponsor({ onClose, onCreated }) {
  const dialog = useDialog();
  const [form, setForm] = useState({ name: '', taxCode: '', field: '', address: '', tel: '', email: '' });
  const [submitting, setSubmitting] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const btnAdd = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const sponsor = await HTTPClient.fetch('POST', '/sponsors', form);
      await dialog.alert(`Đã thêm nhà tài trợ "${sponsor.name}" và chọn vào hợp đồng.`, { type: 'success', title: 'Thêm thành công' });
      onCreated(sponsor);
    } catch (err) {
      await dialog.alert(err.message, { type: 'error' }); // giữ nguyên dữ liệu đã nhập
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <FormModal prefix={P} icon="sponsor" title="Thêm nhà tài trợ"
      subtitle="Nhà tài trợ mới sẽ được chọn ngay vào hợp đồng đang soạn."
      onClose={onClose} onSubmit={btnAdd} submitting={submitting}>
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
    </FormModal>
  );
}
