import { Link, NavLink, useNavigate } from 'react-router-dom';
import Icon from './Icon.jsx';
import useSession, { session } from '../hooks/useSession.js';
import useDialog from '../hooks/useDialog.js';

export function initials(name = '') {
  const parts = name.trim().split(/\s+/);
  return ((parts[0]?.[0] || '') + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase();
}

/** Khung chung cho các trang sau đăng nhập: thanh điều hướng + nội dung. */
export function AppShell({ children }) {
  const navigate = useNavigate();
  const dialog = useDialog();
  const [organizer] = useSession('organizer');

  // "Giới thiệu", "BXH" chỉ có trên header, chưa có trang riêng
  const comingSoon = (name) => (e) => {
    e.preventDefault();
    dialog.alert(`Mục "${name}" đang được phát triển.`, { title: 'Sắp ra mắt' });
  };

  const btnLogout = () => {
    session.clear();
    navigate('/login', { replace: true });
  };

  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <Link to="/" className="brand">
            <span className="brand-mark">F1</span>
            <span className="brand-text"><b>Mùa giải F1</b><small>Tài trợ & Trả thưởng</small></span>
          </Link>
          <nav className="nav">
            <NavLink to="/"><Icon name="home" /><span>Trang chủ</span></NavLink>
            <a href="#gioi-thieu" onClick={comingSoon('Giới thiệu')}><Icon name="info" /><span>Giới thiệu</span></a>
            <a href="#bxh" onClick={comingSoon('BXH')}><Icon name="trophy" /><span>BXH</span></a>
          </nav>
          <div className="user">
            <div className="user-chip">
              <span className="avatar">{initials(organizer?.fullName)}</span>
              <span className="user-meta"><b>{organizer?.fullName}</b><small>{organizer?.role}</small></span>
            </div>
            <button id="btnLogout" type="button" className="icon-btn" title="Đăng xuất" onClick={btnLogout}>
              <Icon name="logout" />
              <span className="sr-only">Đăng xuất</span>
            </button>
          </div>
        </div>
      </header>
      {children}
    </>
  );
}

/** Tiêu đề trang dạng hero (banner tối): breadcrumb, tiêu đề, mô tả, nút thao tác bên phải. */
export function PageHeader({ crumbs = [], title, subtitle, actions }) {
  return (
    <div className="page-head hero hero-compact">
      <div>
        {crumbs.length > 0 && (
          <nav className="crumbs" aria-label="Đường dẫn">
            {crumbs.map((c, i) => (
              <span key={c.label} style={{ display: 'contents' }}>
                {i > 0 && <span>/</span>}
                {c.to ? <Link to={c.to}>{c.label}</Link> : <span>{c.label}</span>}
              </span>
            ))}
          </nav>
        )}
        <h1>{title}</h1>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {actions && <div className="page-head-actions">{actions}</div>}
    </div>
  );
}

export function EmptyState({ icon = 'inbox', title, children }) {
  return (
    <div className="empty-state">
      <Icon name={icon} size={36} />
      {title && <b>{title}</b>}
      {children && <span>{children}</span>}
    </div>
  );
}
