import { createContext, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import Icon from './Icon.jsx';

export const DialogContext = createContext(null);

const DEFAULTS = {
  info: { title: 'Thông báo', icon: 'info' },
  success: { title: 'Thành công', icon: 'checkCircle' },
  error: { title: 'Không thành công', icon: 'alert' },
  confirm: { title: 'Xác nhận', icon: 'help' },
};

let seq = 0;

/**
 * Hộp thoại thay cho window.alert / window.confirm (vẫn là "click OK" như kịch bản).
 * dialog.alert(message, { type: 'info' | 'success' | 'error', title }) -> Promise<void>
 * dialog.confirm(message, { title, confirmText }) -> Promise<boolean>
 */
export default function DialogProvider({ children }) {
  const [dialog, setDialog] = useState(null);
  const current = useRef(null);
  const queue = useRef([]);
  const okRef = useRef(null);

  const open = useCallback((opts) => new Promise((resolve) => {
    const d = { ...opts, id: ++seq, resolve };
    if (current.current) {
      queue.current.push(d);
    } else {
      current.current = d;
      setDialog(d);
    }
  }), []);

  const close = useCallback((result) => {
    const d = current.current;
    const next = queue.current.shift() || null;
    current.current = next;
    setDialog(next);
    d?.resolve(result);
  }, []);

  useEffect(() => {
    if (!dialog) return undefined;
    okRef.current?.focus();
    const onKey = (e) => {
      if (e.key === 'Escape') close(dialog.kind === 'confirm' ? false : undefined);
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [dialog, close]);

  const api = useMemo(() => ({
    alert: (message, o = {}) => open({ kind: 'alert', type: o.type || 'info', title: o.title, message }),
    confirm: (message, o = {}) => open({
      kind: 'confirm', type: 'confirm', title: o.title, message, confirmText: o.confirmText,
    }),
  }), [open]);

  const meta = dialog ? DEFAULTS[dialog.type] : null;

  return (
    <DialogContext.Provider value={api}>
      {children}
      {dialog && (
        <div className="dialog-backdrop" onMouseDown={(e) => e.target === e.currentTarget && dialog.kind === 'confirm' && close(false)}>
          <div key={dialog.id} className="dialog" role="alertdialog" aria-modal="true" aria-labelledby="dialog-title">
            <div className={`dialog-icon ${dialog.type}`}><Icon name={meta.icon} size={22} /></div>
            <h3 id="dialog-title">{dialog.title || meta.title}</h3>
            <div className="dialog-message">{dialog.message}</div>
            <div className="dialog-actions">
              {dialog.kind === 'confirm' && (
                <button type="button" className="btn" onClick={() => close(false)}>Hủy</button>
              )}
              <button
                ref={okRef}
                type="button"
                className={`btn ${dialog.type === 'error' ? 'btn-dark' : 'btn-primary'}`}
                data-dialog-ok
                onClick={() => close(dialog.kind === 'confirm' ? true : undefined)}
              >
                {dialog.kind === 'confirm' ? dialog.confirmText || 'Đồng ý' : 'OK'}
              </button>
            </div>
          </div>
        </div>
      )}
    </DialogContext.Provider>
  );
}
