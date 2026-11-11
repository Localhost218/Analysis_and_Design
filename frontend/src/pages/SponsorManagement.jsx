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
import NewSponsor from './NewSponsor.jsx';
import EditSponsor from './EditSponsor.jsx';

export default function SponsorManagement() {
  const dialog = useDialog();
  const [role] = useSession('role', 'ORGANIZER');
  const isCustomer = role === 'CUSTOMER';
  const isOrganizer = role === 'ORGANIZER';

  const [keyword, setKeyword] = useSession('sponsorKeyword', '');
  const [txtKeyword, setTxtKeyword] = useState(keyword);
  const [tblSponsor, setTblSponsor] = useSession('sponsorResults', []);
  const [, setSponsor] = useSession('sponsor');

  // Modals
  const [detail, setDetail] = useState(null);
  const [showNew, setShowNew] = useState(false);
  const [editSponsor, setEditSponsor] = useState(null);
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

  useEffect(() => {
    const saved = session.get('sponsorKeyword') ?? '';
    searchByName(saved, false);
  }, [searchByName]);

  const btnSearch = (e) => {
    e.preventDefault();
    searchByName(txtKeyword.trim(), true);
  };

  const viewDetail = (sponsor) => {
    setSponsor(sponsor);
    setDetail(sponsor);
  };

  const handleCreated = (newSp) => {
    setShowNew(false);
    searchByName(txtKeyword.trim(), false);
  };

  const handleUpdated = (updated) => {
    setTblSponsor((prev) => prev.map((s) => (s.id === updated.id ? { ...s, ...updated } : s)));
    if (detail && detail.id === updated.id) {
      setDetail((d) => ({ ...d, ...updated }));
    }
    setEditSponsor(null);
  };

  const handleDeleted = (deletedId) => {
    setTblSponsor((prev) => prev.filter((s) => s.id !== deletedId));
    if (detail && detail.id === deletedId) {
      setDetail(null);
    }
  };

  const quickDelete = async (e, s) => {
    e.stopPropagation();
    const ok = await dialog.confirm(
      `Bạn có chắc chắn muốn xóa nhà tài trợ "${s.name}"?`,
      { title: 'Xác nhận xóa nhà tài trợ', confirmText: 'Xóa' }
    );
    if (!ok) return;
    try {
      await HTTPClient.fetch('DELETE', `/sponsors/${s.id}`);
      await dialog.alert(`Đã xóa nhà tài trợ "${s.name}".`, { type: 'success', title: 'Xóa thành công' });
      handleDeleted(s.id);
    } catch (err) {
      await dialog.alert(err.message, { type: 'error', title: 'Không thể xóa nhà tài trợ' });
    }
  };

  return (
    <main className="main">
      <PageHeader
        crumbs={isCustomer ? [] : [{ label: 'Trang chủ', to: '/' }, { label: 'Nhà tài trợ' }]}
        title={isCustomer ? 'Thông tin Nhà tài trợ' : 'Quản lý thông tin nhà tài trợ'}
        subtitle={
          isCustomer
            ? 'Xem thông tin chi tiết và danh sách hợp đồng đã ký của các nhà tài trợ giải đua F1.'
            : 'Tìm theo tên nhà tài trợ; chọn một dòng để xem thông tin chi tiết, thêm, sửa hoặc xóa.'
        }
        actions={
          <div style={{ display: 'flex', gap: 8 }}>
            {isOrganizer && (
              <button
                id="btnNewSponsor"
                type="button"
                className="btn btn-primary"
                onClick={() => setShowNew(true)}
              >
                <Icon name="plus" />Thêm nhà tài trợ
              </button>
            )}
            {!isCustomer && (
              <Link id="btnCancel" className="btn" to="/">
                <Icon name="back" />Quay lại
              </Link>
            )}
          </div>
        }
      />

      <section className="card">
        <div className="card-body">
          <form className="field-row" onSubmit={btnSearch}>
            <label htmlFor="txtKeyword" className="sr-only">Tên nhà tài trợ</label>
            <div className="input-icon">
              <Icon name="search" />
              <input
                id="txtKeyword"
                value={txtKeyword}
                placeholder="Nhập tên nhà tài trợ, vd: Red"
                onChange={(e) => setTxtKeyword(e.target.value)}
                autoFocus
              />
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
                <th className="tt">TT</th>
                <th>Mã nhà tài trợ</th>
                <th>Tên nhà tài trợ</th>
                <th>Email</th>
                <th>Số điện thoại</th>
                <th>Trạng thái</th>
                <th className="num">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {tblSponsor.length === 0 && (
                <tr>
                  <td colSpan={7}>
                    <EmptyState icon="search" title="Chưa có kết quả">
                      Nhập tên nhà tài trợ và bấm Tìm kiếm
                    </EmptyState>
                  </td>
                </tr>
              )}
              {tblSponsor.map((s, i) => (
                <tr key={s.id} className="clickable" onClick={() => viewDetail(s)}>
                  <td className="tt">{i + 1}</td>
                  <td><span className="chip">{sponsorCode(s.id)}</span></td>
                  <td className="cell-title">{s.name}</td>
                  <td>{s.email}</td>
                  <td className="nowrap">{s.tel}</td>
                  <td><StatusBadge status={s.status} /></td>
                  <td className="num" style={{ whiteSpace: 'nowrap' }}>
                    <div style={{ display: 'inline-flex', gap: 6, alignItems: 'center' }} onClick={(e) => e.stopPropagation()}>
                      <button
                        type="button"
                        className="btn btn-sm"
                        onClick={() => viewDetail(s)}
                        title="Xem chi tiết"
                      >
                        <Icon name="eye" size={14} />Xem chi tiết
                      </button>
                      {isOrganizer && (
                        <>
                          <button
                            type="button"
                            className="btn btn-sm"
                            title="Sửa"
                            onClick={() => setEditSponsor(s)}
                          >
                            <Icon name="pencil" size={14} />
                          </button>
                          <button
                            type="button"
                            className="btn btn-sm btn-danger"
                            title="Xóa"
                            onClick={(e) => quickDelete(e, s)}
                          >
                            <Icon name="trash" size={14} />
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {detail && (
        <SponsorDetail
          sponsor={detail}
          readOnly={isCustomer}
          onClose={() => setDetail(null)}
          onUpdated={handleUpdated}
          onDeleted={handleDeleted}
        />
      )}

      {showNew && (
        <NewSponsor
          onClose={() => setShowNew(false)}
          onCreated={handleCreated}
        />
      )}

      {editSponsor && (
        <EditSponsor
          sponsor={editSponsor}
          onClose={() => setEditSponsor(null)}
          onUpdated={handleUpdated}
        />
      )}
    </main>
  );
}
