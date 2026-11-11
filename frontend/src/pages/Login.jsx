import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import HTTPClient from '../HTTPClient.js';
import { session } from '../hooks/useSession.js';
import useDialog from '../hooks/useDialog.js';

export default function Login() {
  const navigate = useNavigate();
  const dialog = useDialog();
  const [txtUsername, setTxtUsername] = useState('');
  const [txtPassword, setTxtPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const doLogin = async (username, password) => {
    setLoading(true);
    try {
      const res = await HTTPClient.fetch('POST', '/auth/login', {
        username: username.trim(),
        password,
      });

      session.set('role', res.role);
      session.set('roleName', res.roleName);
      session.set('user', res.user);

      if (res.role === 'ORGANIZER') {
        session.set('organizer', res.user);
        navigate('/', { replace: true });
      } else if (res.role === 'SPONSOR') {
        session.set('sponsor', res.user);
        navigate('/sponsor-portal', { replace: true });
      } else if (res.role === 'DRIVER') {
        session.set('driver', res.user);
        navigate('/driver-portal', { replace: true });
      } else {
        session.set('customer', res.user);
        navigate('/sponsors', { replace: true });
      }
    } catch (err) {
      await dialog.alert(err.message, { type: 'error', title: 'Đăng nhập thất bại' });
    } finally {
      setLoading(false);
    }
  };

  const btnLogin = async (e) => {
    e.preventDefault();
    await doLogin(txtUsername, txtPassword);
  };

  return (
    <div className="auth">
      <form className="auth-card" onSubmit={btnLogin} style={{ maxWidth: 440 }}>
        <div className="brand">
          <span className="brand-mark">F1</span>
          <span className="brand-text"><b>Mùa giải F1</b><small>Hệ thống quản lý tài trợ & trả thưởng</small></span>
        </div>
        <h1>Đăng nhập</h1>
        <p>Hệ thống hỗ trợ Ban tổ chức, Nhà tài trợ, Tay đua & Khách hàng</p>

        <div className="grid">
          <div className="field">
            <label htmlFor="txtUsername">Tên đăng nhập / Mã số thuế</label>
            <input
              id="txtUsername"
              value={txtUsername}
              placeholder="Username hoặc MST của nhà tài trợ"
              onChange={(e) => setTxtUsername(e.target.value)}
              autoComplete="username"
              autoFocus
              required
            />
          </div>
          <div className="field">
            <label htmlFor="txtPassword">Mật khẩu</label>
            <input
              id="txtPassword"
              type="password"
              value={txtPassword}
              placeholder="Nhập mật khẩu"
              onChange={(e) => setTxtPassword(e.target.value)}
              autoComplete="current-password"
              required
            />
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
