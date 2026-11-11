import { useEffect, useState } from 'react';
import HTTPClient from '../HTTPClient.js';
import FormModal, { Field } from '../components/FormModal.jsx';
import useDialog from '../hooks/useDialog.js';

const P = 'bankAccount';

/**
 * Cửa sổ nổi trên DriverPortal: tay đua thêm (account = null) hoặc sửa tài khoản ngân hàng của mình.
 * Luôn phải có đúng 1 tài khoản mặc định: tài khoản mặc định không bỏ tích được, tài khoản đầu tiên tự thành mặc định.
 */
export default function BankAccountForm({ driverId, account, forceDefault, onClose, onSaved }) {
  const dialog = useDialog();
  const editing = !!account;
  const lockedDefault = forceDefault || !!account?.isDefault;
  const [banks, setBanks] = useState([]);
  const [form, setForm] = useState({
    bankId: account?.bank?.id ?? '',
    accountNumber: account?.accountNumber || '',
    accountHolder: account?.accountHolder || '',
    isDefault: lockedDefault,
  });
  const [submitting, setSubmitting] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });

  useEffect(() => {
    HTTPClient.fetch('GET', '/drivers/banks')
      .then(setBanks)
      .catch((e) => dialog.alert(e.message, { type: 'error' }));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const btnSave = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    const body = {
      bank: { id: Number(form.bankId) },
      accountNumber: form.accountNumber,
      accountHolder: form.accountHolder,
      isDefault: form.isDefault,
    };
    try {
      const driver = editing
        ? await HTTPClient.fetch('PUT', `/drivers/${driverId}/bank-accounts/${account.id}`, body)
        : await HTTPClient.fetch('POST', `/drivers/${driverId}/bank-accounts`, body);
      await dialog.alert(editing ? 'Đã cập nhật tài khoản ngân hàng.' : 'Đã thêm tài khoản ngân hàng.',
        { type: 'success', title: 'Thành công' });
      onSaved(driver);
    } catch (err) {
      await dialog.alert(err.message, { type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <FormModal prefix={P} icon="bank"
      title={editing ? 'Sửa tài khoản ngân hàng' : 'Thêm tài khoản ngân hàng'}
      subtitle="Tài khoản dùng để nhận tiền thưởng chuyển khoản online"
      onClose={onClose} onSubmit={btnSave} submitting={submitting}
      submitText={editing ? 'Lưu thay đổi' : undefined}>
      <Field prefix={P} name="txtBank" label="Ngân hàng" wide>
        {(a) => (
          <select {...a} value={form.bankId} onChange={set('bankId')} required>
            <option value="">Chọn ngân hàng</option>
            {banks.map((b) => <option key={b.id} value={b.id}>{b.name} ({b.bankCode})</option>)}
          </select>
        )}
      </Field>
      <Field prefix={P} name="txtAccountNumber" label="Số tài khoản" hint="6–19 chữ số">
        {(a) => <input {...a} value={form.accountNumber} onChange={set('accountNumber')} required
          inputMode="numeric" pattern="\d{6,19}" title="Số tài khoản gồm 6–19 chữ số" />}
      </Field>
      <Field prefix={P} name="txtAccountHolder" label="Chủ tài khoản" hint="Viết không dấu, tự chuyển in hoa">
        {(a) => <input {...a} value={form.accountHolder} onChange={set('accountHolder')} required />}
      </Field>
      <div className="field" style={{ gridColumn: '1 / -1' }}>
        <label className="check-row">
          <input type="checkbox" name="chkDefault" checked={form.isDefault} onChange={set('isDefault')} disabled={lockedDefault} />
          <span>Đặt làm tài khoản nhận thưởng mặc định</span>
        </label>
        {lockedDefault && (
          <span className="hint">
            {forceDefault ? 'Bạn chưa có tài khoản mặc định nên tài khoản này sẽ là mặc định.' : 'Muốn đổi mặc định, hãy đặt tài khoản khác làm mặc định.'}
          </span>
        )}
      </div>
    </FormModal>
  );
}
