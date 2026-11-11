# Hệ thống quản lý Mùa giải F1 — Module M3 (Tài trợ & Trả thưởng)

Bài tập lớn môn Phân tích & Thiết kế HTTT (PTIT, nhóm N07). Code là bản **cài đặt đúng theo báo cáo thiết kế**, không tự ý thêm tầng, lớp hay chức năng ngoài thiết kế.

## Phạm vi
Người dùng chính: **thành viên Ban tổ chức (Organizer)**. Có 3 chức năng:
1. Quản lý thông tin nhà tài trợ (tìm kiếm theo tên, xem chi tiết + hợp đồng đã ký)
2. Ký hợp đồng tài trợ (soạn → "Chờ ký" → xác nhận → "Đã ký")
3. Trả thưởng cho tay đua theo chặng đua (chỉ luồng **Online**; chọn nhiều tay đua, nhiều chặng, gộp vào một hóa đơn)

## Nguồn thiết kế (nguồn sự thật)
- **Duy nhất:** `docs/report/B23DCCN465 - Nguyễn Tự Kiên - Báo cáo tổng hợp.pdf`. Các mục chính:
  - 2: kịch bản chuẩn và ngoại lệ
  - 7: lớp thực thể
  - 8: CSDL
  - 9: giao diện và lớp chi tiết
  - 10: hoạt động
  - 11: tuần tự
- Các file PDF từng pha (lấy yêu cầu, phân tích, thiết kế) là bản cũ. **Không dùng** khi chúng khác báo cáo tổng hợp.
- Chi tiết đã trích sẵn trong `backend/CLAUDE.md` và `frontend/CLAUDE.md`. Khi code lệch báo cáo tổng hợp thì **hỏi lại**, không tự chọn.

## Cấu trúc repo
- `backend/`: Java 21 + Spring Boot 3.5 + JPA (EntityManager) + Lombok, xem `backend/CLAUDE.md`
- `frontend/`: React 18 (JSX) + Vite + React Router, xem `frontend/CLAUDE.md`
- `docker-compose.yaml`: 4 service `database` (f1-database), `backend` (f1-backend), `frontend` (f1-frontend), `cloudflare` (f1-cloudflare)
- `database/`: dữ liệu PostgreSQL do container ghi (gitignore, thuộc uid 70)
- `docs/`: tài liệu, **không sửa**

## Kiến trúc (theo biểu đồ tuần tự)
`*.jsx` → `HTTPClient.fetch()` → `RestController` → `*DAO` (EntityManager) → entity. **Không có tầng service.**

## Chạy
Toàn bộ bằng Docker:
```bash
docker compose up -d --build     # http://localhost:3000 (nginx → /api → backend:8080)
docker compose logs -f backend
docker compose down              # dừng (dữ liệu vẫn còn ở ./database)
```
Đăng nhập: `a` / `a@123` hoặc `b` / `b@123`.

Khi phát triển (chỉ chạy DB trong Docker):
```bash
docker compose up -d database
cd backend && ./mvnw spring-boot:run       # :8080
cd backend && ./mvnw test                  # 33 test, cần database đang chạy, tự rollback
cd frontend && npm install && npm run dev  # :5173, proxy /api → :8080
```
Đã sửa code thì build lại image: `docker compose up -d --build backend` (hoặc `frontend`).

## Quy tắc chung
- Giao diện và thông báo viết tiếng Việt. Tên lớp, hàm, biến **giữ đúng tên trong báo cáo** (vd: `SponsorDAO.searchByName`, `txtKeyword`).
- Commit theo Conventional Commits (`feat:`, `fix:`, `docs:`...).
- Không thêm thư viện ngoài danh sách trong `backend/CLAUDE.md` / `frontend/CLAUDE.md` khi chưa hỏi.
- Cài đặt một chức năng mới: dùng skill `/implement-feature`.

---

## Bảng tổng hợp xử lý Kịch bản Ngoại lệ (Mục 2 Báo cáo tổng hợp)

Toàn bộ các luồng ngoại lệ được trích xuất từ Mục 2 ("Kịch bản chuẩn và ngoại lệ") của báo cáo thiết kế và được cài đặt đầy đủ, chính xác ở cả Frontend và Backend:

### 1. UC 2.1: Quản lý thông tin nhà tài trợ (Tìm kiếm NTT)
| Bước | Điều kiện ngoại lệ | Xử lý Frontend (`SponsorManagement.jsx`, `Login.jsx`) | Xử lý Backend / DB (`OrganizerDAO`, `SponsorDAO`) | Thông báo hiển thị |
|---|---|---|---|---|
| **2.1** | Đăng nhập thất bại (sai username/password) | Hiện popup lỗi qua `useDialog.alert()`. Nhấn "OK" → giữ nguyên giao diện để nhập lại | `OrganizerController.checkLogin` kiểm tra tài khoản, trả HTTP 401 Unauthorized | `"Tên đăng nhập hoặc mật khẩu không chính xác"` |
| **6.1** | Không tìm thấy NTT có tên chứa từ khóa | `btnSearch` bắt danh sách rỗng, hiện alert popup; bấm "OK" quay lại giao diện tìm kiếm | `SponsorDAO.searchByName` truy vấn LIKE `%keyword%`, trả về danh sách rỗng `[]` | `"Không tồn tại nhà tài trợ chứa từ khóa <từ_khóa>."` |

---

### 2. UC 2.2: Ký hợp đồng tài trợ
| Bước | Điều kiện ngoại lệ | Xử lý Frontend (`ContractSigning.jsx`, `New*`) | Xử lý Backend / DB (`ContractDAO`, `SponsorDAO`, `SeasonDAO`, `TermDAO`, `BenefitDAO`) | Thông báo hiển thị |
|---|---|---|---|---|
| **4.1** | Đăng nhập thất bại | Hiện popup lỗi. Bấm "OK" tiếp tục ở lại trang đăng nhập | Trả HTTP 401 Unauthorized | `"Tên đăng nhập hoặc mật khẩu không chính xác"` |
| **9.1** | Tìm NTT không thấy | `SuggestInput` hiện dòng nhắc không tìm thấy; có nút "Thêm nhà tài trợ" (`btnNewSponsor`) mở popup `NewSponsor` | Trả danh sách rỗng `[]` | `"Không tồn tại nhà tài trợ chứa từ khóa <từ_khóa>"` |
| **9.8** | Thêm NTT mới: thiếu thông tin, sai định dạng (MST 10/13 số, SĐT, Email), hoặc trùng MST | Form kiểm tra client-side; bắt lỗi từ backend, giữ nguyên dữ liệu đã nhập trên form để người dùng chỉnh sửa | `SponsorDAO.createSponsor` kiểm tra regex MST, SĐT, Email; check trùng MST/username trong `tblMember`/`tblSponsor`. Trả HTTP 409 Conflict nếu vi phạm | `"Thông tin nhà tài trợ không hợp lệ hoặc mã số thuế đã tồn tại"` |
| **13.1** | Tìm mùa giải không thấy | `SuggestInput` hiện dòng nhắc; nút "Thêm mùa giải mới" (`btnNewSeason`) mở popup `NewSeason` | Trả danh sách rỗng `[]` | `"Không tìm thấy mùa giải"` |
| **13.3** | Thêm mùa giải mới: ngày kết thúc <= ngày bắt đầu, hoặc trùng tên + năm | Kiểm tra ngày trên form (`startDate < endDate`); bắt lỗi từ backend, giữ nguyên dữ liệu để chỉnh sửa | `SeasonDAO.createSeason` kiểm tra năm (1950–2100), thứ tự ngày, check trùng tên + năm. Trả HTTP 409 Conflict nếu vi phạm | `"Ngày kết thúc phải sau ngày bắt đầu."` / `"Thông tin mùa giải không hợp lệ hoặc mùa giải đã tồn tại"` |
| **17.1** | Tìm điều khoản không thấy | `SuggestInput` hiện dòng nhắc; nút "Thêm điều khoản mới" (`btnNewTerm`) mở popup `NewTerm` | Trả danh sách rỗng `[]` | `"Không tìm thấy điều khoản phù hợp"` |
| **17.3** | Thêm điều khoản mới: thiếu thông tin hoặc trùng tiêu đề | Bắt lỗi từ backend, giữ nguyên dữ liệu trên form để chỉnh sửa | `TermDAO.createTerm` kiểm tra trường bắt buộc và check trùng tiêu đề (case-insensitive). Trả HTTP 409 Conflict | `"Thông tin điều khoản không hợp lệ hoặc điều khoản đã tồn tại"` |
| **21.1** | Tìm quyền lợi không thấy | `SuggestInput` hiện dòng nhắc; nút "Thêm quyền lợi mới" (`btnNewBenefit`) mở popup `NewBenefit` | Trả danh sách rỗng `[]` | `"Không tìm thấy quyền lợi phù hợp"` |
| **21.3** | Thêm quyền lợi mới: thiếu thông tin hoặc trùng tiêu đề | Bắt lỗi từ backend, giữ nguyên dữ liệu trên form để chỉnh sửa | `BenefitDAO.createBenefit` kiểm tra trường bắt buộc và check trùng tiêu đề. Trả HTTP 409 Conflict | `"Thông tin quyền lợi không hợp lệ hoặc quyền lợi đã tồn tại"` |
| **26.1** | Kiểm tra hợp đồng trước khi lưu: thiếu trường bắt buộc, giá trị <= 0, ngày hết hạn <= ngày bắt đầu, thiếu đại diện 2 bên, thiếu điều khoản (>= 1) hoặc thiếu quyền lợi (>= 1) | Hàm `validate()` kiểm tra toàn bộ điều kiện, hiện dialog liệt kê rõ từng nội dung cần sửa; bấm "OK" giữ nguyên form để sửa | `ContractDAO.createContract` kiểm tra chặt chẽ: sponsor hợp lệ ("Đang hợp tác"), season, organizer, giá trị dương, ngày hợp lệ, đại diện, terms (>=1), benefits (>=1), không trùng term/benefit. Trả HTTP 400 Bad Request nếu vi phạm | Liệt kê chi tiết các lỗi vi phạm qua popup `"Thông tin không hợp lệ"` |
| **27.1** | Lỗi lưu hoặc in hợp đồng | Bắt lỗi HTTP/mạng; giữ nguyên hợp đồng đang soạn ở client, không lưu id/barcode và **không chuyển sang trạng thái "Chờ ký"**. Cho phép bấm "Lưu và in hợp đồng" để thử lại | Giao dịch database rollback nếu có ngoại lệ | `"Thông tin hợp đồng không hợp lệ"` hoặc thông báo lỗi kết nối |
| **Ký** | Xác nhận đã ký: hợp đồng chưa lưu hoặc đã ký | Chặn nút "Xác nhận đã ký" nếu chưa lưu; kiểm tra trạng thái chỉ cho phép từ "Chờ ký" sang "Đã ký", không cho ký lần 2 | `ContractDAO.updateSignedStatus` chỉ cập nhật khi `status = 'Chờ ký'`. Trả HTTP 409 Conflict nếu sai trạng thái | `"Hợp đồng chưa được lưu"` / `"Hợp đồng không tồn tại hoặc không ở trạng thái Chờ ký"` |

---

### 3. UC 2.3: Trả thưởng cho tay đua theo chặng đua (Online)
| Bước | Điều kiện ngoại lệ | Xử lý Frontend (`RewardPayment.jsx`, `Invoice.jsx`) | Xử lý Backend / DB (`RewardInvoiceDAO`, `RaceDAO`, `DriverDAO`) | Thông báo hiển thị |
|---|---|---|---|---|
| **2.1** | Đăng nhập thất bại | Hiện popup lỗi. Bấm "OK" tiếp tục ở lại trang đăng nhập | Trả HTTP 401 Unauthorized | `"Tên đăng nhập hoặc mật khẩu không chính xác"` |
| **7.1** | Tìm mùa giải không có kết quả | Danh sách gợi ý trống, hiện dòng nhắc không tìm thấy mùa giải | Trả danh sách rỗng `[]` | `"Không tìm thấy mùa giải"` |
| **9.1** | Mùa giải chưa có chặng đua nào kết thúc / đủ điều kiện | Ô dropdown chặng đua để trống (hiện dòng "Chưa có chặng đua đủ điều kiện trả thưởng"); popup thông báo cho BTC | Lọc các chặng có `endTime <= NOW()`; nếu danh sách rỗng thì trả về `[]` | Popup: `"Chưa có chặng đua đủ điều kiện trả thưởng (Mùa giải ... chưa có chặng đua nào kết thúc)."` |
| **11.1** | Chặng đua chưa có kết quả thi đấu hoặc chưa cấu hình mức thưởng cơ sở (`basePrize`) | Popup cảnh báo dữ liệu còn thiếu; ẩn bảng tiền thưởng (hiện trạng thái trống) | `RaceDAO.getRaceInfo` chỉ tính thưởng khi `basePrize != null && basePrize > 0` | `"Chặng <tên> còn thiếu dữ liệu kết quả hoặc mức thưởng cơ sở."` |
| **11.2** | Kết quả đã được trả thưởng hoặc tiền thưởng = 0 VNĐ (DNF, DNS, Crash, hạng >= 11) | Checkbox chọn tay đua **bị khóa (disabled)**, hàng bị làm mờ (dim), không thể tích chọn | `RaceDAO.calculateReward` gán tiền thưởng = 0 cho tay đua không hoàn thành hoặc ngoài top 10 | Checkbox bị disabled |
| **13.1** | Tay đua được tích chưa có tài khoản nhận thưởng mặc định còn hoạt động | Bắt khi gọi API chi tiết tay đua; hiển thị alert popup, **bỏ tích checkbox và không thêm tay đua vào bảng thanh toán** | `RewardInvoiceDAO.createRewardInvoice` kiểm tra `listBankAccount` có tài khoản `isDefault = true && status = 'Hoạt động'`; nếu thiếu ném `PaymentException` | `"Thiếu thông tin nhận thưởng: <tên tay đua>"` |
| **13.2** | BTC bỏ tích một tay đua | Xóa dòng tương ứng khỏi bảng thanh toán (`tblPayment`), tự động trừ và cập nhật lại tổng tiền (`lblTotalAmount`) | Quản lý state `listRegisteredReceipt` ở client | Dòng bị xóa, tổng tiền cập nhật tức thì |
| **20.1** | Bảng thanh toán đang trống | Nút "Thanh toán" (`btnPay`) **bị vô hiệu hóa (disabled)**, không thể bấm | `RewardInvoiceDAO` kiểm tra `listRegisteredReceipt.isEmpty()` ném `PaymentException` | Nút Thanh toán disabled |
| **20.2** | BTC chọn "Hủy" | Hiện hộp thoại xác nhận hủy; nếu đồng ý thì xóa toàn bộ bảng thanh toán, dọn session và chuyển về trang chủ | Session dọn dẹp `invoice`, `season`, `race` | Hộp thoại xác nhận `"Xóa bảng thanh toán và quay về trang chủ?"` |
| **21.1** | Lỗi thanh toán: ngân hàng từ chối, mất kết nối hoặc timeout | Bắt lỗi `PaymentException` từ backend; **rollback toàn bộ giao dịch**, không tạo hóa đơn, **tất cả kết quả giữ nguyên trạng thái "Chưa trả"**, giữ nguyên bảng thanh toán | `@Transactional` trên `RewardInvoiceDAO.createRewardInvoice` rollback toàn bộ; không bản ghi hóa đơn nào được lưu; trả HTTP 409 Conflict | `"Thanh toán không thành công: <chi_tiết>\n\nBấm "Thanh toán" để thử lại hoặc "Hủy" để kết thúc."` |
| **21.2** | Sau lỗi thanh toán: Thử lại hoặc Hủy | Bảng thanh toán được giữ nguyên: BTC có thể bấm lại nút "Thanh toán" để thực hiện lại giao dịch (Thử lại) hoặc bấm "Hủy" để hủy bỏ | State thanh toán không bị mất khi gặp lỗi | BTC linh hoạt chọn "Thanh toán" (thử lại) hoặc "Hủy" |
| **21.3** | Gửi email hóa đơn thất bại | Hệ thống vẫn lưu hóa đơn và cập nhật trạng thái "Đã trả", mở giao diện hóa đơn (`Invoice.jsx`), cung cấp nút "Gửi lại email" để BTC gửi lại | Phân tách việc lưu hóa đơn thành công và tác vụ gửi email (không làm rollback hóa đơn) | Giao diện hóa đơn có nút `"Gửi lại email"`, thông báo `"Đã gửi lại thông tin hóa đơn đến email của từng tay đua."` |

