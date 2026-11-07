import { Navigate, Route, Routes } from 'react-router-dom';
import { session } from './hooks/useSession.js';
import { AppShell } from './components/Layout.jsx';
import Login from './pages/Login.jsx';
import OrganizerHome from './pages/OrganizerHome.jsx';
import SponsorManagement from './pages/SponsorManagement.jsx';
import ContractSigning from './pages/ContractSigning.jsx';
import RewardPayment from './pages/RewardPayment.jsx';

/**
 * Chỉ các trang chức năng chính có route; giao diện con (SponsorDetail, NewSponsor, NewSeason,
 * NewTerm, NewBenefit, Invoice) hiện dạng cửa sổ nổi trên trang chính.
 * Mọi trang trừ Login cần "organizer" trong session; hiển thị trong khung AppShell. */
function RequireLogin({ children }) {
  if (!session.get('organizer')) return <Navigate to="/login" replace />;
  return <AppShell>{children}</AppShell>;
}

const guarded = (el) => <RequireLogin>{el}</RequireLogin>;

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={guarded(<OrganizerHome />)} />
      <Route path="/sponsors" element={guarded(<SponsorManagement />)} />
      <Route path="/contracts/new" element={guarded(<ContractSigning />)} />
      <Route path="/rewards/payment" element={guarded(<RewardPayment />)} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
