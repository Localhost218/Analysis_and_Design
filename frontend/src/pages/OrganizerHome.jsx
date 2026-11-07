import { Link } from 'react-router-dom';
import Icon from '../components/Icon.jsx';
import useSession from '../hooks/useSession.js';

const FEATURES = [
  {
    id: 'btnSponsorManagement', to: '/sponsors', icon: 'sponsor', title: 'Quản lý thông tin nhà tài trợ',
    desc: 'Tìm kiếm nhà tài trợ theo tên, xem thông tin chi tiết và các hợp đồng đã ký.',
  },
  {
    id: 'btnContractSigning', to: '/contracts/new', icon: 'contract', title: 'Ký hợp đồng tài trợ',
    desc: 'Soạn hợp đồng với điều khoản, quyền lợi; lưu, in và xác nhận hai bên đã ký.',
  },
  {
    id: 'btnRewardPayment', to: '/rewards/payment', icon: 'trophy', title: 'Trả thưởng cho tay đua',
    desc: 'Chọn kết quả các chặng đua đã kết thúc, thanh toán online và lập hóa đơn.',
  },
];

export default function OrganizerHome() {
  const [organizer] = useSession('organizer');

  return (
    <main className="main">
      <section className="hero">
        <h1>Xin chào, {organizer?.fullName}</h1>
        <p>Trang chủ Ban tổ chức — chọn một chức năng để bắt đầu.</p>
      </section>
      <div className="features">
        {FEATURES.map((f) => (
          <Link key={f.id} id={f.id} to={f.to} className="feature">
            <span className="feature-icon"><Icon name={f.icon} size={22} /></span>
            <h3>{f.title}</h3>
            <p>{f.desc}</p>
            <span className="row-link">Mở chức năng <Icon name="arrow" size={15} /></span>
          </Link>
        ))}
      </div>
    </main>
  );
}
