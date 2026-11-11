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
  pages/                 11 trang (liệt kê bên dưới)
  components/
    Layout.jsx           AppShell: header tối + viền đỏ, menu chỉ gồm "Trang chủ", "Giới thiệu", "BXH"
                         (2 mục sau chưa có trang, bấm hiện dialog "Sắp ra mắt"; KHÔNG thêm route cho chúng),
                         tên người dùng, btnLogout. Các chức năng vào từ thẻ ở OrganizerHome, không có link riêng trên header.
                         PageHeader: tiêu đề trang dạng hero (banner tối, breadcrumb, tiêu đề, mô tả, nút bên phải).
                         EmptyState, initials()
    DialogProvider.jsx   hộp thoại thay window.alert / confirm (bọc ngoài App trong main.jsx)
    Modal.jsx            cửa sổ nổi (portal vào body): đầu dạng hero tối, đóng bằng X / Hủy / Esc; size="lg" cho SponsorDetail, Invoice
    FormModal.jsx        khung form cho 4 giao diện New* (Modal + nút Hủy / Thêm) và Field (label + ô nhập)
    Badges.jsx           StatusBadge (màu theo trạng thái), RankBadge (huy chương hạng 1–3)
    Icon.jsx             bộ icon SVG: <Icon name="search" />
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
  App.jsx                route + RequireLogin (chưa có "organizer" trong session thì về /login)
  index.css              CSS chung, có @media print (in hợp đồng / hóa đơn)
```
- Trạng thái dùng chung giữa các trang **luôn** đi qua `useSession` / `useContractDraft`, không gọi `sessionStorage` trực tiếp.
- `useSession` ghi storage **ngay khi gọi setter** (không đợi render), vì trang thường `navigate()` ngay sau đó và trang mới đọc session lập tức.
- Hàm thuần đặt ở `utils/`, không phải hook.

## Route và cửa sổ nổi
- **Chỉ trang chức năng chính có route**: `/login`, `/`, `/sponsors`, `/contracts/new`, `/rewards/payment`.
- Giao diện con mở từ trang chính (**SponsorDetail, NewSponsor, NewSeason, NewTerm, NewBenefit, Invoice**) là **cửa sổ nổi** (`Modal` / `FormModal`) render ngay trong trang chính: không thêm route, không `navigate()`. Trang chính giữ state mở/đóng và nhận kết quả qua callback (`onCreated`, `onClose`, `onBack`).
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
| organizer | Organizer | Login | mọi trang |
| sponsor | Sponsor | SponsorManagement | SponsorDetail |
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
| txtUsername | input text | Tên đăng nhập: |
| txtPassword | input password | Mật khẩu: |
| btnLogin | button | Đăng nhập |

- Gọi `POST /api/organizers/login`. Thành công: lưu `organizer`, chuyển tới `/`.
- Thất bại: alert "Tên đăng nhập hoặc mật khẩu không chính xác", OK thì giữ nguyên form.

### OrganizerHome.jsx — `/` — tiêu đề "Trang chủ Ban tổ chức"
| Tên | Loại | Nhãn | Tới |
|---|---|---|---|
| btnSponsorManagement | Link | Quản lý thông tin nhà tài trợ | /sponsors |
| btnContractSigning | Link | Ký hợp đồng tài trợ | /contracts/new |
| btnRewardPayment | Link | Trả thưởng cho tay đua | /rewards/payment |
| btnLogout | button | Đăng xuất | xóa session, về /login |

---

## 1. Quản lý thông tin nhà tài trợ

### SponsorManagement.jsx — `/sponsors` — tiêu đề "Quản lý thông tin nhà tài trợ"
| Tên | Loại | Nhãn |
|---|---|---|
| txtKeyword | input text | Tên nhà tài trợ: |
| btnSearch | button | Tìm kiếm |
| tblSponsor | table | cột: TT, Mã nhà tài trợ, Tên nhà tài trợ, Email, Số điện thoại, Trạng thái, Xem chi tiết |
| btnCancel | Link | Quay lại → / |

- Click Tìm kiếm thì gọi `GET /api/sponsors?name=`. Rỗng: alert "Không tìm thấy nhà tài trợ".
- **Không hiển thị Mã số thuế trong bảng.**
- Click một dòng: lưu `sponsor` vào session, mở cửa sổ SponsorDetail (không đổi URL).

### SponsorDetail.jsx — **modal trên `/sponsors`** — tiêu đề "Thông tin nhà tài trợ"
`sponsorDetail` (dl, 2 cột):
- Trái: Tên nhà tài trợ, Mã số thuế, Lĩnh vực, Địa chỉ
- Phải: Email, Số điện thoại, Trạng thái

| Tên | Loại | Nhãn |
|---|---|---|
| tblContract | table | tiêu đề "Danh sách hợp đồng đã ký"; cột: TT, Mã hợp đồng (barcode), Mùa giải, Giá trị, Ngày ký, Hiệu lực đến |
| btnCancel (id `sponsorDetail-btnCancel`) | button | Quay lại → đóng cửa sổ |

- Khi mở trang thì gọi `GET /api/sponsors/{id}` (SponsorDAO.getSponsorInfo, kèm listContract đã ký). Không có hợp đồng thì hiện "Chưa có".

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
| tblPayment | table | TT, Mùa giải – Chặng ("2025 – C03"), Tay đua ("Trần Văn B (#50)"), Ngân hàng, Chủ tài khoản, Số tài khoản, Tiền thưởng |
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
   - Kết quả đã nằm trong tblPayment thì checkbox hiện tích sẵn.
4. **Tích một tay đua**: gọi `GET /api/drivers/{driverId}`, lấy tài khoản `isDefault = true` còn hoạt động, thêm một dòng vào `tblPayment`, cập nhật `lblTotalAmount`, lưu `invoice`.
   - Không có tài khoản mặc định: alert "Thiếu thông tin nhận thưởng: <tên tay đua>" và bỏ tích.
   - **Bỏ tích**: xóa dòng đó khỏi tblPayment, cập nhật tổng tiền.
5. **Đổi sang mùa giải hoặc chặng khác**: `tblPayment` **giữ nguyên**, nên có thể trả nhiều chặng, nhiều mùa trong một hóa đơn.
6. **Click Thanh toán**: `confirm("Bạn xác nhận thanh toán?")`, đồng ý thì gọi `POST /api/invoices` với organizer, `paymentMethod = "Chuyển khoản online"` và các id trong tblPayment.
   - Thành công: alert "Thanh toán thành công". Click OK thì lưu `invoice` (bản đã lưu, kèm mùa giải, chặng, tay đua) → mở cửa sổ Invoice (không đổi URL); bảng kết quả chặng đang xem được nạp lại để hiện "Đã trả".
   - Thất bại: alert "Thanh toán không thành công: <message>", mọi kết quả giữ `Chưa trả`, bảng thanh toán giữ nguyên để bấm Thanh toán lại ("Thử lại").
7. **Click Hủy**: xóa `invoice`, `season`, `race` khỏi session rồi về `/`.

### Invoice.jsx — **modal trên `/rewards/payment`** — tiêu đề "Hóa đơn trả thưởng"
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

- Không gọi backend, đọc từ `invoice` trong session.

---

## Điểm báo cáo chưa nói (đã chọn tạm, cần chủ dự án xác nhận)
1. **Giá trị các select, đang đề xuất:**
   - Lĩnh vực: Truyền thông và quảng cáo, Năng lượng, Công nghệ, Tài chính – Ngân hàng, Viễn thông, Ô tô, Đồ uống, Thời trang, Khác
   - Loại điều khoản: Tài chính, Quảng bá, Pháp lý, Khác
   - Loại quyền lợi: Quảng bá thương hiệu, Hình ảnh – Truyền thông, Sự kiện, Khác
2. **Thanh toán là giả lập** và không gửi email hóa đơn thật.
