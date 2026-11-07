import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import HTTPClient from '../HTTPClient.js';
import Icon from '../components/Icon.jsx';
import { EmptyState, PageHeader } from '../components/Layout.jsx';
import { StatusBadge } from '../components/Badges.jsx';
import useSession, { session } from '../hooks/useSession.js';
import useDialog from '../hooks/useDialog.js';
import { sponsorCode } from '../utils/format.js';
import SponsorDetail from './SponsorDetail.jsx';

export default function SponsorManagement() {
  const dialog = useDialog();
  const [keyword, setKeyword] = useSession('sponsorKeyword', '');
  const [txtKeyword, setTxtKeyword] = useState(keyword);
  const [tblSponsor, setTblSponsor] = useSession('sponsorResults', []);
  const [, setSponsor] = useSession('sponsor');
  // SponsorDetail hiện dạng cửa sổ nổi, không có route riêng
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(false);

  const searchByName = useCallback(async (k, notify) => {
    setLoading(true);
    try {
      const list = await HTTPClient.fetch('GET', `/sponsors?name=${encodeURIComponent(k)}`);
      setTblSponsor(list);
      setKeyword(k);
      if (notify && list.length === 0) {
        await dialog.alert(`Không tồn tại nhà tài trợ chứa từ khóa "${k}".`, { title: 'Không tìm thấy' });
      }
    } catch (e) {
      await dialog.alert(e.message, { type: 'error' });
    } finally {
      setLoading(false);
    }
  }, [setTblSponsor, setKeyword, dialog]);

  // Quay lại từ SponsorDetail: hiện lại danh sách đã tìm
  useEffect(() => {
    const saved = session.get('sponsorKeyword');
    if (saved) searchByName(saved, false);
  }, [searchByName]);

  const btnSearch = (e) => {
    e.preventDefault();
    searchByName(txtKeyword.trim(), true);
  };

  const viewDetail = (sponsor) => {
    setSponsor(sponsor); // "Lưu vào session: Sponsor"
    setDetail(sponsor);
  };

  return (
    <main className="main">
      <PageHeader
        crumbs={[{ label: 'Trang chủ', to: '/' }, { label: 'Nhà tài trợ' }]}
        title="Quản lý thông tin nhà tài trợ"
        subtitle="Tìm theo tên nhà tài trợ; chọn một dòng để xem thông tin chi tiết."
        actions={<Link id="btnCancel" className="btn" to="/"><Icon name="back" />Quay lại</Link>}
      />

      <section className="card">
        <div className="card-body">
          <form className="field-row" onSubmit={btnSearch}>
            <label htmlFor="txtKeyword" className="sr-only">Tên nhà tài trợ</label>
            <div className="input-icon">
              <Icon name="search" />
              <input id="txtKeyword" value={txtKeyword} placeholder="Nhập tên nhà tài trợ, vd: Red"
                onChange={(e) => setTxtKeyword(e.target.value)} autoFocus />
            </div>
            <button id="btnSearch" type="submit" className="btn btn-primary" disabled={loading}>
              <Icon name="search" />Tìm kiếm
            </button>
          </form>
        </div>
      </section>

      <section className="card">
        <div className="card-head">
          <h2>Kết quả tìm kiếm</h2>
          {keyword && <span className="count">{tblSponsor.length} nhà tài trợ · “{keyword}”</span>}
        </div>
        <div className="card-body flush table-wrap">
          <table id="tblSponsor">
            <thead>
              <tr>
                <th className="tt">TT</th><th>Mã nhà tài trợ</th><th>Tên nhà tài trợ</th><th>Email</th>
                <th>Số điện thoại</th><th>Trạng thái</th><th className="num">Xem chi tiết</th>
              </tr>
            </thead>
            <tbody>
              {tblSponsor.length === 0 && (
                <tr><td colSpan={7}>
                  <EmptyState icon="search" title="Chưa có kết quả">Nhập tên nhà tài trợ và bấm Tìm kiếm</EmptyState>
                </td></tr>
              )}
              {tblSponsor.map((s, i) => (
                <tr key={s.id} className="clickable" onClick={() => viewDetail(s)}>
                  <td className="tt">{i + 1}</td>
                  <td><span className="chip">{sponsorCode(s.id)}</span></td>
                  <td className="cell-title">{s.name}</td>
                  <td>{s.email}</td>
                  <td className="nowrap">{s.tel}</td>
                  <td><StatusBadge status={s.status} /></td>
                  <td className="num"><span className="row-link">Xem chi tiết <Icon name="arrow" size={14} /></span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {detail && <SponsorDetail sponsor={detail} onClose={() => setDetail(null)} />}
    </main>
  );
}
