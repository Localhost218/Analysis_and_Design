import { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import Icon from './Icon.jsx';

/**
 * Cửa sổ nổi hiện ngay trên trang hiện tại (không tạo route mới).
 * Phần đầu dạng hero tối; đóng bằng nút X, nút Hủy hoặc phím Esc (khi không có hộp thoại DialogProvider đang mở).
 * Nếu truyền onSubmit thì phần thân là <form>.
 */
export default function Modal({ title, subtitle, icon, onClose, onSubmit, footer, size, headerExtra, children }) {
  const onCloseRef = useRef(onClose);
  onCloseRef.current = onClose;
  const panelRef = useRef(null);

  useEffect(() => {
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    document.body.classList.add('modal-open'); // khi in chỉ in nội dung modal
    const onKey = (e) => {
      if (e.key === 'Escape' && !document.querySelector('.dialog')) onCloseRef.current();
    };
    window.addEventListener('keydown', onKey);
    panelRef.current?.querySelector('input, select, textarea')?.focus();
    return () => {
      document.body.style.overflow = prevOverflow;
      document.body.classList.remove('modal-open');
      window.removeEventListener('keydown', onKey);
    };
  }, []);

  const Panel = onSubmit ? 'form' : 'div';

  return createPortal(
    <div className="modal-backdrop">
      <Panel ref={panelRef} className={`modal${size ? ` modal-${size}` : ''}`} role="dialog" aria-modal="true" aria-labelledby="modal-title" onSubmit={onSubmit}>
        <header className="modal-hero">
          {icon && <span className="modal-hero-icon"><Icon name={icon} size={22} /></span>}
          <div className="modal-hero-text">
            <h2 id="modal-title">{title}</h2>
            {subtitle && <p>{subtitle}</p>}
          </div>
          {headerExtra}
          <button type="button" className="modal-close" onClick={() => onCloseRef.current()} aria-label="Đóng">
            <Icon name="x" />
          </button>
        </header>
        <div className="modal-body">{children}</div>
        {footer && <footer className="modal-foot">{footer}</footer>}
      </Panel>
    </div>,
    document.body,
  );
}
