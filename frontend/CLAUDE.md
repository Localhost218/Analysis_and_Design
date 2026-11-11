# Frontend

## Stack
- React 18 + Vite, **JavaScript, file `.jsx`** (không dùng TypeScript)
- React Router; CSS thuần (`src/index.css`, design tokens trên `:root`); không thêm UI library khi chưa hỏi
- Font Be Vietnam Pro (Google Fonts, fallback system-ui); icon SVG nội tuyến trong `components/Icon.jsx`
- Gọi API: lớp `HTTPClient` bọc `fetch`, base `/api` (Vite proxy `/api` → `http://localhost:8080`)

## Lệnh
```bash
npm install
npm run dev     # http://localhost:5173, cần backend chạy ở :8080 (hoặc đặt VITE_API_TARGET)
npm run build   # ra dist/
```
Docker: `Dockerfile` build bằng node:22-alpine rồi phục vụ bằng nginx:1.27-alpine; `nginx.conf` proxy `/api/` → `http://backend:8080` và trả `index.html` cho mọi route (SPA). Trong docker-compose: http://localhost:3000

## Cấu trúc
```
src/
  pages/                 Trang chính và cổng thông tin theo vai trò (OrganizerHome, SponsorPortal, DriverPortal, Login, SponsorManagement, ContractSigning, RewardPayment) + Modal components (SponsorDetail, EditSponsor, NewSponsor, NewSeason, NewTerm, NewBenefit, Invoice, EditDriver, BankAccountForm)
  components/
    Layout.jsx           AppShell: header tối + viền đỏ, menu cố định Trang chủ (về trang chủ theo vai trò) · Giới thiệu · BXH, badge vai trò, avatar chữ cái đầu, btnLogout.
                         PageHeader: tiêu đề trang dạng hero (banner tối, breadcrumb, tiêu đề, mô tả, nút bên phải).
                         EmptyState, initials()
    DialogProvider.jsx   hộp thoại thay window.alert / confirm (bọc ngoài App trong main.jsx)
    Modal.jsx            cửa sổ nổi (portal vào body): đầu dạng hero tối, đóng bằng X / Hủy / Esc; size="lg" cho SponsorDetail, Invoice, ContractDetail
    FormModal.jsx        khung form cho các giao diện New* và Edit* (Modal + nút Hủy / Thêm; form sửa truyền `submitText="Lưu thay đổi"`) và Field (label + ô nhập)
    Badges.jsx           StatusBadge (màu theo trạng thái, gồm cả `Hoạt động`, `Tạm khóa`, `Mặc định`), RankBadge (huy chương hạng 1–3)
    Icon.jsx             bộ icon SVG: <Icon name="search" />. Tên chưa có trong PATHS sẽ ra icon trống, nên thêm icon mới vào PATHS trước khi dùng
    SuggestInput.jsx     ô nhập có danh sách gợi ý (debounce 300 ms), dùng cho nhà tài trợ / mùa giải / điều khoản / quyền lợi
  hooks/
    useSession.js        useSession(key, initial) → [value, setValue]: state đồng bộ sessionStorage
                         ("Lưu vào / Lấy từ session"); setValue(null) để xóa. Export thêm `session`
                         (get/set/remove/clear) cho chỗ ngoài component (route guard, đăng xuất)
    useContractDraft.js  hợp đồng đang soạn: { contract, patchContract, resetContract, discardContract }
    useDialog.js         const dialog = useDialog(); await dialog.alert(msg, { type: 'info'|'success'|'error', title });
                         if (await dialog.confirm(msg, { title, confirmText })) ...
  utils/
    format.js            formatDate, formatDateTime, formatMoney, sponsorCode, raceCode, seasonRaceLabel, duration, isRaceEnded
    options.js           giá trị các ô chọn (Lĩnh vực, Loại điều khoản, Loại quyền lợi)
  HTTPClient.js          lớp phụ trợ trong báo cáo: HTTPClient.fetch(method, path, body) → JSON; lỗi thì ném Error(message từ backend)
  App.jsx                route + RoleHome auto-routing + RequireLogin (bảo vệ các route theo phiên đăng nhập)
  index.css              CSS chung, có @media print (in hợp đồng / hóa đơn); cuối file là phần cổng thông tin: layout-portal, profile-card, stat-card, segmented, default-account
```
- Trạng thái dùng chung giữa các trang **luôn** đi qua `useSession` / `useContractDraft`, không gọi `sessionStorage` trực tiếp.
- `useSession` ghi storage **ngay khi gọi setter** (không đợi render), vì trang thường `navigate()` ngay sau đó và trang mới đọc session lập tức.
- Hàm thuần đặt ở `utils/`, không phải hook.

## Route và cửa sổ nổi
- **Các route chính**:
  - `/login`: Đăng nhập (hỗ trợ 4 vai trò: Ban tổ chức, Nhà tài trợ, Tay đua, Khách hàng)
  - `/`: Trang chủ Ban tổ chức (OrganizerHome)
  - `/sponsors`: Quản lý thông tin nhà tài trợ (Organizer: full CRUD; Customer: read-only)
  - `/contracts/new`: Ký hợp đồng tài trợ (Organizer)
  - `/rewards/payment`: Trả thưởng cho tay đua (Organizer: Online & Trực tiếp)
  - `/sponsor-portal`: Cổng thông tin Nhà tài trợ (Sponsor)
  - `/driver-portal`: Cổng thông tin Tay đua (Driver)
- Giao diện con mở từ trang chính (**SponsorDetail, EditSponsor, NewSponsor, NewSeason, NewTerm, NewBenefit, Invoice**) là **cửa sổ nổi** (`Modal` / `FormModal`) render ngay trong trang chính: không thêm route, không `navigate()`. Trang chính giữ state mở/đóng và nhận kết quả qua callback (`onCreated`, `onUpdated`, `onClose`, `onBack`).
- Trong modal, tên phần tử theo báo cáo đặt ở thuộc tính `name`; `id` có tiền tố tên giao diện (vd: `newTerm-txtTitle`, `sponsorDetail-btnCancel`) để không trùng `id` với trang chính.

## Quy ước chung
- `id` / tên state của phần tử **đúng tên trong báo cáo** (cột "Tên" ở các bảng dưới).
- Nhãn hiển thị **đúng chữ trên bản thiết kế giao diện** (cột "Nhãn").
- Hộp thoại "OK" / xác nhận dùng `useDialog()` (**không** dùng `window.alert` / `window.confirm`). Chọn `type` đúng: lỗi → `error`, thành công → `success`, không tìm thấy / nhắc nhở → `info`. Luồng "click OK thì ..." viết bằng `await dialog.alert(...)` rồi mới điều hướng.
- Giao diện:
  - Mọi trang sau đăng nhập: `<main className="main">` + `PageHeader` (hero) + các `section.card` (`card-head` / `card-body`). Trang có bảng nhiều cột dùng `main wide` (RewardPayment, ContractSigning) để bảng không phải cuộn ngang ở màn hình ≥ 1440px.
  - Ký hợp đồng: dùng `main wide`; cột tóm tắt bên phải là **sidecar** `layout-aside` / `aside` (sticky), có thể thu gọn/mở rộng để giải phóng tối đa không gian cho bảng điều khoản và quyền lợi; khi thu gọn có nút nổi `sidecar-floating-tab` bên mép phải màn hình để mở lại nhanh.
  - Trả thưởng: các bảng trải hết chiều ngang; tổng tiền, nội dung, nút Hủy / Thanh toán nằm trên `checkout-bar` dính đáy màn hình (không dùng cột bên phải, vì khi xuống dòng trên màn hình hẹp sẽ để lại khoảng trống).
  - Nút: `btn btn-primary` (hành động chính, đỏ), `btn-success` (xác nhận đã ký), `btn` (phụ), `btn-ghost` (Hủy).
  - Trạng thái luôn hiển thị bằng `StatusBadge`, mã (MST, mã hợp đồng, mã hóa đơn) bằng `.chip`.
  - Grid chứa bảng phải dùng `minmax(0, 1fr)` để không tràn ngang trên mobile (kiểm tra ở bề rộng 390px).
- Không đặt `setState` gây render lại ngay trước `navigate()` nếu trang có `<Navigate>` theo state đó (vd: Invoice dùng `session.remove` rồi mới `navigate`).
- Ngày hiển thị `dd/mm/yyyy`, ngày giờ `dd/mm/yyyy hh:mm`, tiền `1.000.000 VNĐ` (`toLocaleString('vi-VN')`).
- Bảng có cột `TT` (số thứ tự từ 1).
- Component dạng function + hooks, tên PascalCase.

## Session keys (thuộc tính ẩn trong báo cáo)
| Key | Kiểu | Ghi bởi | Đọc bởi |
|---|---|---|---|
| auth | `{ role, roleName, user }` | Login | App.jsx (RoleHome, Layout, Route Guard) |
| organizer | Organizer | Login | mọi trang của Ban tổ chức |
| sponsor | Sponsor | Login, SponsorManagement | SponsorDetail, SponsorPortal |
| driver | Driver | Login | DriverPortal |
| customer | Customer/Member (vd `d`) | Login | SponsorManagement (read-only) |
| contract | Contract đang soạn (kèm listContractTerm, listContractBenefit), qua `useContractDraft` | ContractSigning, New* | ContractSigning (giữ dữ liệu khi qua lại New*) |
| sponsorKeyword, sponsorResults | từ khóa và kết quả tìm gần nhất | SponsorManagement | SponsorManagement (quay lại từ chi tiết vẫn còn danh sách) |
| season | Season đang chọn (kèm listRace) | RewardPayment | RewardPayment |
| race | Race đang chọn (kèm listRegisteredReceipt) | RewardPayment | RewardPayment |
| invoice | RewardInvoice: lúc soạn là bảng thanh toán, sau khi trả là hóa đơn đã lưu | RewardPayment | RewardPayment, Invoice |

---

## Chức năng chung

### Login.jsx — `/login` — tiêu đề "Đăng nhập"
| Tên | Loại | Nhãn |
|---|---|---|
| txtUsername | input text | Tên đăng nhập: (hoặc Mã số thuế đối với Nhà tài trợ) |
| txtPassword | input password | Mật khẩu: |
| btnLogin | button | Đăng nhập |

- Gọi `POST /api/auth/login`. Thành công: lưu `auth` và đối tượng người dùng tương ứng vào session, tự động điều hướng:
  - `ORGANIZER` → `/` (Trang chủ BTC)
  - `SPONSOR` → `/sponsor-portal` (Cổng thông tin NTT)
  - `DRIVER` → `/driver-portal` (Cổng thông tin Tay đua)
  - `CUSTOMER` → `/` (RoleHome hiển thị `SponsorManagement` chỉ đọc, ẩn nút Quay lại và breadcrumb)
- Thất bại: alert "Tên đăng nhập hoặc mật khẩu không chính xác", OK thì giữ nguyên form.

### OrganizerHome.jsx — `/` — tiêu đề "Trang chủ Ban tổ chức"
| Tên | Loại | Nhãn | Tới |
|---|---|---|---|
| btnSponsorManagement | Link | Quản lý thông tin nhà tài trợ | /sponsors |
| btnContractSigning | Link | Ký hợp đồng tài trợ | /contracts/new |
| btnRewardPayment | Link | Trả thưởng cho tay đua | /rewards/payment |
| btnLogout | button | Đăng xuất | xóa session, về /login |

---

## 1. Quản lý thông tin nhà tài trợ (Module 1 & Module 2)

### SponsorManagement.jsx — `/sponsors` — tiêu đề "Quản lý thông tin nhà tài trợ" / "Danh sách nhà tài trợ"
| Tên | Loại | Nhãn |
|---|---|---|
| txtKeyword | input text | Tên nhà tài trợ: |
| btnSearch | button | Tìm kiếm |
| btnNewSponsor | button | + Thêm nhà tài trợ (chỉ hiện cho Organizer) → mở modal `NewSponsor` |
| tblSponsor | table | cột: TT, Mã nhà tài trợ, Tên nhà tài trợ, Email, Số điện thoại, Trạng thái, Thao tác (Xem chi tiết, Sửa, Xóa) |
| btnCancel | Link | Quay lại |

- Click Tìm kiếm thì gọi `GET /api/sponsors?name=`. Rỗng: alert "Không tìm thấy nhà tài trợ".
- **Không hiển thị Mã số thuế trong bảng.**
- **Thêm nhà tài trợ (Module 1)**: Nút "+ Thêm nhà tài trợ" mở modal `NewSponsor`. Sau khi tạo mới thành công, nạp lại danh sách và mở ngay chi tiết NTT vừa tạo.
- **Sửa nhà tài trợ (Module 1)**: Nút "Sửa" trên từng dòng mở modal `EditSponsor` để chỉnh sửa thông tin.
- **Xóa nhà tài trợ (Module 1)**: Nút "Xóa" yêu cầu xác nhận xác thực, sau đó gọi `DELETE /api/sponsors/{id}`. Nếu NTT đã có hợp đồng tài trợ, hệ thống chặn xóa và báo lỗi `"Không thể xóa nhà tài trợ đã có hợp đồng tài trợ"`.
- **Phân quyền Khách hàng (Module 2)**: Khi tài khoản `CUSTOMER` truy cập, giao diện hiển thị ở chế độ chỉ đọc: tiêu đề đổi thành "Danh sách nhà tài trợ", ẩn nút "+ Thêm nhà tài trợ", cột thao tác chỉ còn nút "Xem chi tiết" (ẩn Sửa, Xóa).
- Click "Xem chi tiết" một dòng: lưu `sponsor` vào session, mở modal `SponsorDetail`.

### SponsorDetail.jsx — **modal trên `/sponsors`** — tiêu đề "Thông tin nhà tài trợ"
`sponsorDetail` (dl, 2 cột):
- Trái: Tên nhà tài trợ, Mã số thuế, Lĩnh vực, Địa chỉ
- Phải: Email, Số điện thoại, Trạng thái

| Tên | Loại | Nhãn |
|---|---|---|
| tblContract | table | tiêu đề "Danh sách hợp đồng đã ký"; cột: TT, Mã hợp đồng (barcode), Mùa giải, Giá trị, Ngày ký, Hiệu lực đến |
| btnEditSponsor | button | Chỉnh sửa thông tin (chỉ hiện cho Organizer) → mở modal `EditSponsor` |
| btnDeleteSponsor | button | Xóa nhà tài trợ (chỉ hiện cho Organizer, kiểm tra ràng buộc không có HĐ) |
| btnCancel (id `sponsorDetail-btnCancel`) | button | Quay lại → đóng cửa sổ |

- Khi mở trang thì gọi `GET /api/sponsors/{id}` (SponsorDAO.getSponsorInfo, kèm listContract đã ký). Không có hợp đồng thì hiện "Chưa có".
- Chế độ `readOnly` cho vai trò `CUSTOMER`: ẩn các nút "Chỉnh sửa thông tin" và "Xóa nhà tài trợ".

### EditSponsor.jsx — **modal trên `/sponsors` và `/sponsor-portal`** — tiêu đề "Chỉnh sửa nhà tài trợ"
- Các trường: Tên nhà tài trợ, Mã số thuế (readonly), Lĩnh vực hoạt động, Địa chỉ trụ sở, Email liên hệ, Số điện thoại, Trạng thái (Đang hợp tác / Ngừng hợp tác).
- Khi submit: gọi `PUT /api/sponsors/{id}`, xử lý kiểm tra định dạng và báo lỗi nếu có xung đột dữ liệu. Bấm "Lưu thay đổi" thành công thì cập nhật dữ liệu và đóng modal.

---

## 2. Ký hợp đồng tài trợ

### ContractSigning.jsx — `/contracts/new` — tiêu đề "Ký hợp đồng tài trợ"
Phần thông tin chung:
| Tên | Loại | Nhãn |
|---|---|---|
| lblCreatedDate | time (chỉ đọc) | Ngày lập: (hôm nay) |
| (trạng thái) | text chỉ đọc | Trạng thái: `Chờ ký` hoặc `Đã ký` |
| txtSponsorName + listSponsor | input gợi ý | Nhà tài trợ: (placeholder "nhập tên, chọn từ danh sách gợi ý") |
| btnNewSponsor | button | Thêm nhà tài trợ → mở cửa sổ NewSponsor |
| txtSeasonName + listSeason | input gợi ý | Mùa giải: |
| btnNewSeason | button | Thêm mùa giải → mở cửa sổ NewSeason |
| txtContractValue | input number | Giá trị hợp đồng: (VNĐ) |
| txtEffectiveFrom | input date | Hiệu lực từ: |
| txtEffectiveTo | input date | đến: |
| txtSponsorRepresentative | input text | Đại diện NTT: |
| txtOrganizerRepresentative | input text | Đại diện BTC: |

Phần **Điều khoản**:
| Tên | Loại | Nhãn |
|---|---|---|
| txtTermTitle + listTerm | input gợi ý | Tiêu đề: (placeholder "nhập tiêu đề, chọn từ gợi ý") |
| btnNewTerm | button | Thêm điều khoản mới → mở cửa sổ NewTerm |
| tblContractTerm | table | cột: TT, Tiêu đề, Nội dung (term.details), Xử lý vi phạm (term.punishment), Ghi chú (input), Xóa |

Phần **Quyền lợi**:
| Tên | Loại | Nhãn |
|---|---|---|
| txtBenefitTitle + listBenefit | input gợi ý | Tiêu đề: |
| btnNewBenefit | button | Thêm quyền lợi mới → mở cửa sổ NewBenefit |
| tblContractBenefit | table | cột: TT, Tiêu đề, Giá trị (input), Đơn vị (input), Ghi chú (input), Xóa |

Nút:
| Tên | Loại | Nhãn |
|---|---|---|
| btnSave | button | Lưu và in hợp đồng |
| btnConfirmSigned | button | Xác nhận đã ký |
| btnCancel | Link | Hủy → / |

Hành vi:
- Gõ vào ô gợi ý (debounce 300 ms) thì gọi `GET /api/sponsors?name=`, `/api/seasons?name=`, `/api/terms?title=`, `/api/benefits?title=`. Rỗng: hiện "Không tìm thấy ..." và gợi ý bấm nút Thêm tương ứng.
- Chọn điều khoản hoặc quyền lợi thì thêm một dòng vào bảng. Không cho chọn trùng. Click Xóa để bỏ dòng.
- Mọi thay đổi ghi vào session `contract` (qua `useContractDraft`), để khi quay lại từ trang New* thì không mất dữ liệu.
- Lưu và in hợp đồng: kiểm tra ràng buộc form (`validate()`: nhà tài trợ, mùa giải, giá trị số nguyên dương, thời gian hiệu lực, đại diện 2 bên, ít nhất 1 điều khoản, ít nhất 1 quyền lợi); nếu lỗi thì alert danh sách vi phạm. Hợp lệ thì gọi `POST /api/contracts`.
  - Lỗi: alert message, giữ nguyên dữ liệu đang soạn và không chuyển trạng thái "Chờ ký" (cho phép thử lại).
  - Thành công: alert "Đã lưu hợp đồng <mã> ở trạng thái Chờ ký", lưu id/barcode vào `contract`, khóa form, rồi `window.print()` (in phần `.print-only`).
- Xác nhận đã ký:
  - Chưa lưu: alert "Hợp đồng chưa được lưu. Hãy bấm 'Lưu và in hợp đồng' trước."
  - Đã lưu: `confirm`, rồi gọi `PUT /api/contracts/{id}/signed`, alert "Đã cập nhật trạng thái hợp đồng", trạng thái thành `Đã ký` và khóa form (không cho ký lần 2).

### NewSponsor.jsx — **modal trên `/contracts/new`** — tiêu đề "Thêm nhà tài trợ"
| Tên | Loại | Nhãn |
|---|---|---|
| txtSponsorName | input | Tên nhà tài trợ: |
| txtTaxCode | input | Mã số thuế: |
| txtField | select | Lĩnh vực: |
| txtAddress | input | Địa chỉ: |
| txtTel | input | Số điện thoại: |
| txtEmail | input email | Email: |
| btnAdd | button | Thêm |
| btnCancel | button | Hủy → đóng cửa sổ |

### NewSeason.jsx — **modal trên `/contracts/new`** — tiêu đề "Thêm mùa giải"
| Tên | Loại | Nhãn |
|---|---|---|
| txtName | input | Tên mùa giải: |
| txtYear | input number | Năm: |
| txtStartDate | input date | Ngày bắt đầu: |
| txtEndDate | input date | Ngày kết thúc: |
| btnAdd / btnCancel | button / Link | Thêm / Hủy |

### NewTerm.jsx — **modal trên `/contracts/new`** — tiêu đề "Thêm điều khoản"
| Tên | Loại | Nhãn |
|---|---|---|
| txtTitle | input | Tiêu đề: |
| txtDetails | input | Nội dung: |
| txtPunishment | input | Xử lý vi phạm: |
| txtType | select | Loại: |
| btnAdd / btnCancel | button / Link | Thêm / Hủy |

### NewBenefit.jsx — **modal trên `/contracts/new`** — tiêu đề "Thêm quyền lợi"
| Tên | Loại | Nhãn |
|---|---|---|
| txtTitle | input | Tiêu đề: |
| txtDescription | input | Mô tả: |
| txtType | select | Loại: |
| btnAdd / btnCancel | button / Link | Thêm / Hủy |

Hành vi chung của 4 trang New*:
- Add thành công: alert "Thêm thành công", gán bản ghi mới vào hợp đồng đang soạn (`patchContract`) (với term hoặc benefit thì thêm dòng vào bảng), rồi đóng cửa sổ.
- Lỗi (không hợp lệ hoặc đã tồn tại): alert message, giữ nguyên dữ liệu đã nhập.

---

## 3. Trả thưởng cho tay đua theo chặng đua (Online)
Luồng: Login.jsx → OrganizerHome.jsx → RewardPayment.jsx → Invoice.jsx

### RewardPayment.jsx — `/rewards/payment` — tiêu đề "Trả thưởng cho tay đua"
Phần chọn:
| Tên | Loại | Nhãn |
|---|---|---|
| txtSeasonName + listSeason | input gợi ý | Mùa giải: (placeholder "nhập tên, chọn từ danh sách gợi ý") |
| (thông tin mùa giải) | text | "Mùa giải: F1 FIA 2025 (6/3/2025 – 6/12/2025)" |
| listRace | select (dropdown) | Chặng đua: (placeholder "chọn chặng đua đã kết thúc"); mỗi mục "C03 – Australian" |

Mục **"Kết quả chặng đua"**:
| Tên | Loại | Cột |
|---|---|---|
| tblResult | table | TT, Số xe, Tay đua, Đội đua, Hạng, Số vòng, Thời gian, Điểm, Hệ số, Tiền thưởng, Trạng thái (`Chưa trả`/`Đã trả`), Chọn (checkbox) |

Mục **"Bảng thanh toán"**:
| Tên | Loại | Nhãn / Cột |
|---|---|---|
| tblPayment | table | TT, Mùa giải – Chặng ("2025 – C03"), Tay đua ("Lando Norris (#1)"), Ngân hàng, Chủ tài khoản, Số tài khoản, Tiền thưởng |
| lblTotalAmount | label | Tổng tiền: 172.000.000 VNĐ |
| txtContent | input | Nội dung: |
| btnPay | button | Thanh toán (disabled khi tblPayment rỗng) |
| btnCancel | Link | Hủy |

Hành vi:
1. **Gõ tên mùa giải** (debounce 300 ms): gọi `GET /api/seasons?name=`.
   - Rỗng: alert "Không tìm thấy mùa giải".
2. **Chọn một mùa giải**: lưu `season`, gọi `GET /api/seasons/{id}`, đưa vào `listRace` **chỉ những chặng đã kết thúc**.
   - Không có chặng nào: alert "Chưa có chặng đua đủ điều kiện trả thưởng", để trống dropdown.
3. **Chọn chặng**: lưu `race`, gọi `GET /api/races/{id}`, hiện `tblResult`.
   - Chặng chưa có kết quả hoặc basePrize chưa có: alert "Chặng đua còn thiếu dữ liệu kết quả hoặc mức thưởng cơ sở", không hiện bảng.
   - Checkbox **bị khóa** khi kết quả `Đã trả` hoặc tiền thưởng = 0.
   - Cột cuối **"Trả trực tiếp"** (Module 3): nút trên từng dòng, khóa khi `Đã trả`, thưởng 0 hoặc tay đua đang ở tblPayment.
   - Kết quả đã nằm trong tblPayment thì checkbox hiện tích sẵn.
4. **Tích một tay đua**:
   - Gọi `GET /api/drivers/{driverId}`, lấy tài khoản `isDefault = true` còn hoạt động, thêm một dòng vào `tblPayment`, cập nhật `lblTotalAmount`. Nếu không có tài khoản mặc định: alert "Thiếu thông tin nhận thưởng: <tên tay đua>" và bỏ tích.
   - **Bỏ tích**: xóa dòng đó khỏi tblPayment, cập nhật tổng tiền.
5. **Đổi sang mùa giải hoặc chặng khác**: `tblPayment` **giữ nguyên**, nên có thể trả nhiều chặng, nhiều mùa trong một hóa đơn.
6. **Click Thanh toán**: `confirm("Bạn xác nhận thanh toán?")`, đồng ý thì gọi `POST /api/invoices` với organizer, `paymentMethod = "Chuyển khoản online"` và các id trong tblPayment (bảng thanh toán chỉ dùng cho chuyển khoản online).
   - Thành công: alert "Thanh toán thành công". Click OK thì lưu `invoice` vào session → **mở cửa sổ nổi Invoice** (không đổi URL); bảng kết quả chặng đang xem được nạp lại để hiện "Đã trả".
   - Thất bại: alert "Thanh toán không thành công: <message>", mọi kết quả giữ `Chưa trả`, bảng thanh toán giữ nguyên để bấm Thanh toán lại ("Thử lại").
7. **Bấm "Trả trực tiếp" trên một dòng** (Module 3, **từng tay đua, không gộp**): confirm số tiền + tên tay đua → `POST /api/invoices` với `paymentMethod = "Trả thưởng trực tiếp"` và **đúng 1** id (không cần tài khoản ngân hàng). Thành công: alert "Trả thưởng thành công", **KHÔNG mở cửa sổ Invoice**, nạp lại bảng kết quả để hiện "Đã trả"; bảng thanh toán online giữ nguyên.
8. **Click Hủy**: xóa `invoice`, `season`, `race` khỏi session rồi về `/`.

### Invoice.jsx — **modal trên `/rewards/payment` và `/driver-portal`** — tiêu đề "Hóa đơn trả thưởng"
Thông tin (dl):
| Nhãn | Giá trị |
|---|---|
| Mã hóa đơn: | barcode (vd: HD001) |
| Thời điểm thanh toán: | paidAt (`dd/mm/yyyy hh:mm`) |
| Phương thức: | paymentMethod |
| Người thực hiện: | organizer.fullName |
| Trạng thái: | status |

| Tên | Loại | Nhãn / Cột |
|---|---|---|
| tblReceipt | table | tiêu đề "Các khoản trả thưởng"; cột: TT, Mùa giải – Chặng, Tay đua, Tiền thưởng |
| (tổng) | text | Tổng tiền: amount VNĐ |
| btnBack | button | Quay lại trang chủ → / (xóa `invoice`, `season`, `race`) |
| (In hóa đơn) | button | Mở hộp thoại in `window.print()` |
| (Gửi lại email) | button | Gửi lại hóa đơn đến email của từng tay đua (cho trường hợp gửi email thất bại - ngoại lệ 21.3) |
| (X / Tiếp tục trả thưởng) | button | Đóng hóa đơn, ở lại trang trả thưởng với bảng thanh toán trống |

- Hỗ trợ 2 chế độ nạp dữ liệu:
  1. Đọc từ `invoice` trong session (dùng sau khi Ban tổ chức thanh toán trên `/rewards/payment`).
  2. Tải qua API `GET /api/invoices/{id}` khi nhận prop `invoiceId` (dùng khi Tay đua bấm "Xem hóa đơn" từ `/driver-portal`).

---

## 4. Cổng thông tin Nhà tài trợ (SponsorPortal.jsx) — `/sponsor-portal` — tiêu đề "Trang chủ Nhà tài trợ"
Dành riêng cho tài khoản Nhà tài trợ (`role = SPONSOR`). Bố cục `layout-portal`: cột trái 320px (sticky) + nội dung bên phải; dưới 1080px xếp thành 1 cột.
- **Cột trái, thẻ hồ sơ (`profile-card`)**: avatar chữ cái đầu, tên, mã NTT (chip), trạng thái hợp tác (badge); danh sách Mã số thuế, Lĩnh vực, Email, Số điện thoại, Địa chỉ (mỗi dòng có icon); nút "Cập nhật thông tin liên hệ" mở modal `EditSponsor`.
- **Bên phải, thẻ thống kê (`stat-card`, 4 cột)**: Tổng số hợp đồng, Hợp đồng đã ký, Hợp đồng chờ ký, Tổng giá trị tài trợ.
- **Bảng hợp đồng**: bộ lọc dạng `segmented` Tất cả / Đã ký / Chờ ký; cột TT, Mã hợp đồng (barcode), Mùa giải, Giá trị, Ngày lập, Ngày ký, Thời hạn hiệu lực, Trạng thái (badge), Thao tác ("Xem chi tiết"). Đại diện hai bên chỉ hiện trong modal chi tiết, để bảng không tràn ngang.
- **Modal chi tiết hợp đồng**:
  - Thông tin chung: Mã barcode, Trạng thái, Ngày lập, Ngày ký, Thời hạn hiệu lực, Giá trị, Đại diện hai bên.
  - Bảng Điều khoản: TT, Tiêu đề điều khoản, Nội dung chi tiết, **Xử lý vi phạm (punishment)**, Ghi chú.
  - Bảng Quyền lợi: TT, Tiêu đề quyền lợi, Mô tả, Số lượng / Giá trị, Ghi chú.

---

## 5. Cổng thông tin Tay đua (DriverPortal.jsx) — `/driver-portal` — tiêu đề "Trang chủ Tay đua"
Dành riêng cho tài khoản Tay đua (`role = DRIVER`). Tay đua **chỉ xem và sửa dữ liệu của chính mình** (id lấy từ session `driver`). Bố cục `layout-portal` như trang Nhà tài trợ.
- **Cột trái, thẻ hồ sơ**: avatar, họ tên, `#racingNumber`, tên đăng nhập (chip); Ngày sinh, Quốc tịch, Số xe; nút `btnEditDriver` "Chỉnh sửa thông tin" mở modal `EditDriver`.
- **Cột trái, thẻ `default-account`**: tài khoản nhận thưởng mặc định (số TK, ngân hàng, chủ TK); chưa có thì thẻ đỏ nhắc thêm tài khoản.
- **Bên phải, thẻ thống kê (4 cột)**: Tổng chặng đã đua, Đã nhận (số chặng + tiền), Chờ chi trả (số chặng + tiền), Tổng tiền thưởng.
- **Bảng tiền thưởng (`tblDriverReward`)**: TT, Mùa giải, Chặng đua (kèm địa điểm), Ngày thi đấu, Thứ hạng, Vòng, Điểm, Tiền thưởng, Trạng thái ("Đã trả" / "Chưa trả"), Hóa đơn. Kết quả "Đã trả" có nút "Xem HDxxx" mở modal `Invoice` với `readOnly` (ẩn "Gửi lại email" và "Tiếp tục trả thưởng").
- **Bảng tài khoản ngân hàng (`tblBankAccount`)**: nút `btnNewBankAccount` "Thêm tài khoản"; cột TT, Ngân hàng, Số tài khoản, Chủ tài khoản, Trạng thái (badge "Mặc định" + trạng thái), Thao tác: "Đặt mặc định" (chỉ tài khoản chưa mặc định, gọi PUT với `isDefault: true`), "Sửa" (modal `BankAccountForm`), "Xóa" (confirm; **khóa với tài khoản mặc định**).
- Sau mỗi lần sửa thông tin / tài khoản: backend trả Driver kèm `listBankAccount`, trang cập nhật state và session `driver`, `user`.

### EditDriver.jsx — **modal trên `/driver-portal`** — tiêu đề "Chỉnh sửa thông tin cá nhân"
| Tên | Loại | Nhãn |
|---|---|---|
| txtFullName | input | Họ và tên |
| txtDateOfBirth | input date | Ngày sinh |
| txtNationality | input | Quốc tịch |
| txtUsername | input (disabled) | Tên đăng nhập |
| txtRacingNumber | input (disabled) | Số xe (Do Ban tổ chức cấp) |
- Lưu thay đổi thì gọi `PUT /api/drivers/{id}`; thành công alert "Đã cập nhật thông tin cá nhân.", lỗi thì alert message và giữ form.

### BankAccountForm.jsx — **modal trên `/driver-portal`** — tiêu đề "Thêm tài khoản ngân hàng" / "Sửa tài khoản ngân hàng"
| Tên | Loại | Nhãn |
|---|---|---|
| txtBank | select (từ `GET /api/drivers/banks`) | Ngân hàng |
| txtAccountNumber | input (6–19 chữ số) | Số tài khoản |
| txtAccountHolder | input (backend lưu in hoa) | Chủ tài khoản |
| chkDefault | checkbox | Đặt làm tài khoản nhận thưởng mặc định |
- `chkDefault` bị khóa và tích sẵn khi đang sửa tài khoản mặc định, hoặc khi tay đua chưa có tài khoản mặc định (`forceDefault`).
- Thêm: `POST /api/drivers/{id}/bank-accounts`; Sửa: `PUT /api/drivers/{id}/bank-accounts/{accountId}`. Lỗi (sai định dạng, trùng ngân hàng + số TK, bỏ mặc định) thì alert message của backend.

---

## Điểm báo cáo chưa nói (đã chọn tạm, cần chủ dự án xác nhận)
1. **Giá trị các select, đang đề xuất:**
   - Lĩnh vực: Truyền thông và quảng cáo, Năng lượng, Công nghệ, Tài chính – Ngân hàng, Viễn thông, Ô tô, Đồ uống, Thời trang, Khác
   - Loại điều khoản: Tài chính, Quảng bá, Pháp lý, Khác
   - Loại quyền lợi: Quảng bá thương hiệu, Hình ảnh – Truyền thông, Sự kiện, Khác
2. **Thanh toán là giả lập** và không gửi email hóa đơn thật.
