import { Navigate, Route, Routes } from 'react-router-dom';
import { session } from './hooks/useSession.js';
import { AppShell } from './components/Layout.jsx';
import Login from './pages/Login.jsx';
import OrganizerHome from './pages/OrganizerHome.jsx';
import SponsorManagement from './pages/SponsorManagement.jsx';
import ContractSigning from './pages/ContractSigning.jsx';
import RewardPayment from './pages/RewardPayment.jsx';
import SponsorPortal from './pages/SponsorPortal.jsx';
import DriverPortal from './pages/DriverPortal.jsx';

/**
 * Khung bảo vệ trang: Kiểm tra phiên đăng nhập (bất kỳ vai trò nào).
 * Hiển thị trang trong khung AppShell.
 */
function RequireLogin({ children }) {
  const hasUser = session.get('user') || session.get('organizer') || session.get('sponsor') || session.get('driver') || session.get('customer');
  if (!hasUser) return <Navigate to="/login" replace />;
  return <AppShell>{children}</AppShell>;
}

const guarded = (el) => <RequireLogin>{el}</RequireLogin>;

/** Trang chủ điều hướng tự động theo vai trò người dùng */
function RoleHome() {
  const role = session.get('role');
  if (role === 'SPONSOR') return <SponsorPortal />;
  if (role === 'DRIVER') return <DriverPortal />;
  if (role === 'CUSTOMER') return <SponsorManagement />; // Khách hàng chỉ xem danh sách nhà tài trợ (chỉ đọc)
  return <OrganizerHome />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={guarded(<RoleHome />)} />
      <Route path="/sponsors" element={guarded(<SponsorManagement />)} />
      <Route path="/contracts/new" element={guarded(<ContractSigning />)} />
      <Route path="/rewards/payment" element={guarded(<RewardPayment />)} />
      <Route path="/sponsor-portal" element={guarded(<SponsorPortal />)} />
      <Route path="/driver-portal" element={guarded(<DriverPortal />)} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
