import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import HTTPClient from '../HTTPClient.js';
import useSession from '../hooks/useSession.js';
import useDialog from '../hooks/useDialog.js';

export default function Login() {
  const navigate = useNavigate();
  const dialog = useDialog();
  const [, setOrganizer] = useSession('organizer');
  const [txtUsername, setTxtUsername] = useState('');
  const [txtPassword, setTxtPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const btnLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const organizer = await HTTPClient.fetch('POST', '/organizers/login', {
        username: txtUsername,
        password: txtPassword,
      });
      setOrganizer(organizer);
      navigate('/', { replace: true });
    } catch (err) {
      await dialog.alert(err.message, { type: 'error', title: 'Đăng nhập thất bại' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth">
      <form className="auth-card" onSubmit={btnLogin}>
        <div className="brand">
          <span className="brand-mark">F1</span>
          <span className="brand-text"><b>Mùa giải F1</b><small>Hệ thống quản lý tài trợ & trả thưởng</small></span>
        </div>
        <h1>Đăng nhập</h1>
        <p>Dành cho thành viên Ban tổ chức</p>
        <div className="grid">
          <div className="field">
            <label htmlFor="txtUsername">Tên đăng nhập</label>
            <input id="txtUsername" value={txtUsername} onChange={(e) => setTxtUsername(e.target.value)}
              autoComplete="username" autoFocus required />
          </div>
          <div className="field">
            <label htmlFor="txtPassword">Mật khẩu</label>
            <input id="txtPassword" type="password" value={txtPassword} onChange={(e) => setTxtPassword(e.target.value)}
              autoComplete="current-password" required />
          </div>
          <button id="btnLogin" type="submit" className="btn btn-primary btn-block btn-lg" disabled={loading}>
            {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>
        </div>
        <div className="auth-foot">Học viện Công nghệ Bưu chính Viễn thông · Nhóm N07</div>
      </form>
    </div>
  );
}
