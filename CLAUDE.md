# Hệ thống quản lý Mùa giải F1 — Module M3 (Tài trợ & Trả thưởng)

Bài tập lớn môn Phân tích & Thiết kế HTTT (PTIT, nhóm N07). Code là bản **cài đặt đúng theo báo cáo thiết kế**, không tự ý thêm tầng, lớp hay chức năng ngoài thiết kế.

## Phạm vi
Hệ thống hỗ trợ 4 vai trò người dùng:
1. **Ban tổ chức (Organizer)**:
   - **Quản lý nhà tài trợ (Module 1)**: Tìm kiếm, xem chi tiết, thêm mới, chỉnh sửa thông tin, và xóa nhà tài trợ (kiểm tra ràng buộc không được xóa khi đã có hợp đồng).
   - **Ký hợp đồng tài trợ**: Soạn thảo → "Chờ ký" → xác nhận → "Đã ký" (điều khoản có nội dung và hình phạt vi phạm - punishment).
   - **Trả thưởng cho tay đua (Module 3)**: Hỗ trợ 2 phương thức:
     - *Chuyển khoản online*: Kiểm tra tài khoản ngân hàng mặc định của tay đua, thanh toán, hiện popup hóa đơn.
     - *Trả thưởng trực tiếp*: Không bắt buộc tài khoản ngân hàng, lưu hóa đơn, cập nhật "Đã trả", và **không hiện popup hóa đơn**.
2. **Khách hàng (Customer) (Module 2)**:
   - Đăng nhập và tra cứu danh sách nhà tài trợ F1 ở chế độ chỉ đọc (trang chủ `/` của Khách hàng chính là danh sách nhà tài trợ, không có trang chủ riêng).
3. **Nhà tài trợ (Sponsor)**:
   - Đăng nhập bằng username hoặc mã số thuế (MST).
   - Xem thông tin doanh nghiệp, chỉnh sửa thông tin liên hệ (Email, SĐT, Địa chỉ).
   - Xem toàn bộ danh sách hợp đồng (cả "Chờ ký" và "Đã ký"), lọc theo trạng thái, xem thống kê giá trị tài trợ.
   - Xem chi tiết từng hợp đồng (đại diện 2 bên, bảng điều khoản có hình phạt, bảng quyền lợi).
4. **Tay đua (Driver)**:
   - Đăng nhập, xem hồ sơ cá nhân và tài khoản ngân hàng nhận thưởng mặc định.
   - Tự sửa thông tin cá nhân **của chính mình** (họ tên, ngày sinh, quốc tịch; không đổi tên đăng nhập, mật khẩu, số xe).
   - Thêm / sửa / xóa tài khoản ngân hàng của chính mình. Ràng buộc: **luôn có đúng 1 tài khoản mặc định** (tài khoản đầu tiên tự là mặc định, không bỏ mặc định hay xóa tài khoản mặc định; muốn đổi thì "Đặt mặc định" cho tài khoản khác).
   - Xem bảng tiền thưởng theo từng chặng đua và trạng thái trả thưởng ("Đã trả" / "Chưa trả").
   - Xem chi tiết hóa đơn trả thưởng tương ứng với các chặng đã được thanh toán (chỉ xem: không có nút "Gửi lại email", "Tiếp tục trả thưởng").

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
Tài khoản đăng nhập mẫu:
- **Ban tổ chức (ORGANIZER)**: `a` / `a@123` hoặc `b` / `b@123`
- **Nhà tài trợ (SPONSOR)**: `redblue` / `redblue@123` (hoặc đăng nhập bằng mã số thuế `0123456789` với mật khẩu `redblue@123`)
- **Tay đua (DRIVER)**: `driver.tranvanb` / `123456` (Trần Văn B), `driver.leminhcuong` (không có TK mặc định), `driver.sainz` (TK mặc định bị khóa), `driver.albon` (chưa có TK), `driver.doanminhkhoa` (chưa thi đấu); đều mật khẩu `123456`. Danh sách đầy đủ các tình huống dữ liệu: mục "Dữ liệu mẫu" trong `backend/CLAUDE.md`
- **Khách hàng (CUSTOMER)**: `customer` / `customer@123`

Khi phát triển (chỉ chạy DB trong Docker):
```bash
docker compose up -d database
cd backend && ./mvnw spring-boot:run       # :8080
cd backend && ./mvnw test                  # 61 test, cần database đang chạy, tự rollback
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

---

### 4. Bổ sung: Module 1 (Thêm, Sửa, Xóa thông tin Nhà tài trợ)
| Bước | Điều kiện ngoại lệ | Xử lý Frontend (`SponsorManagement.jsx`, `EditSponsor.jsx`, `NewSponsor.jsx`) | Xử lý Backend / DB (`SponsorDAO`) | Thông báo hiển thị |
|---|---|---|---|---|
| **M1.1** | Thêm mới NTT: Trùng MST hoặc sai định dạng email/SĐT/MST | Bắt lỗi từ server hoặc validate form client-side, giữ nguyên dữ liệu trên form | `SponsorDAO.createSponsor` kiểm tra regex, trùng MST trong `tblSponsor`/`tblMember`, ném HTTP 409 | `"Thông tin nhà tài trợ không hợp lệ hoặc mã số thuế đã tồn tại"` |
| **M1.2** | Sửa thông tin NTT: Trùng MST với một NTT khác | Hiển thị alert popup cảnh báo, giữ nguyên modal sửa để người dùng điều chỉnh | `SponsorDAO.updateSponsor` kiểm tra `taxCode` khác id hiện tại, ném `SponsorException` → HTTP 409 | `"Mã số thuế đã tồn tại cho nhà tài trợ khác"` |
| **M1.3** | Xóa NTT: Nhà tài trợ đã có hợp đồng tài trợ (cả Chờ ký hoặc Đã ký) | Bắt lỗi HTTP 400 từ backend, hiển thị popup lỗi | `SponsorDAO.deleteSponsor` kiểm tra danh sách hợp đồng của NTT; nếu `!contracts.isEmpty()` ném `SponsorException` → HTTP 400 | `"Không thể xóa nhà tài trợ đã có hợp đồng tài trợ"` |

---

### 5. Bổ sung: Module 2 (Tài khoản Khách hàng xem thông tin Nhà tài trợ)
| Bước | Tình huống | Xử lý Frontend (`SponsorManagement.jsx`, `SponsorDetail.jsx`) | Xử lý Backend / DB | Ghi chú |
|---|---|---|---|---|
| **M2.1** | Khách hàng đăng nhập | Đăng nhập tài khoản role `CUSTOMER`, về `/`: `RoleHome` hiển thị luôn `SponsorManagement` ở chế độ chỉ đọc | `MemberDAO.checkLoginUniversal` xác thực và trả role `CUSTOMER` | Không có trang chủ Khách hàng riêng |
| **M2.2** | Khách hàng xem danh sách và chi tiết NTT | Giao diện tự động chuyển chế độ chỉ đọc (`readOnly = true`): tiêu đề "Danh sách nhà tài trợ", **ẩn toàn bộ nút Thêm, Sửa, Xóa** | `SponsorDAO.searchByName` và `getSponsorInfo` nạp thông tin và danh sách hợp đồng đã ký | Bảo toàn dữ liệu, an toàn quyền truy cập |

---

### 6. Bổ sung: Module 3 (Phương thức Trả thưởng trực tiếp - Offline)
| Bước | Tình huống | Xử lý Frontend (`RewardPayment.jsx`) | Xử lý Backend / DB (`RewardInvoiceDAO`) | Ghi chú |
|---|---|---|---|---|
| **M3.1** | Chọn phương thức "Trả thưởng trực tiếp" | Dropdown chọn "Trả thưởng trực tiếp"; cho phép tích chọn mọi tay đua có tiền thưởng > 0 (không kiểm tra tài khoản ngân hàng mặc định) | Nhận `paymentMethod = "Trả thưởng trực tiếp"` | Hỗ trợ chi trả tiền mặt/trực tiếp |
| **M3.2** | Thực hiện thanh toán trực tiếp | Gửi yêu cầu thanh toán; khi thành công hiện alert thông báo, **hoàn toàn KHÔNG mở popup hóa đơn** (`Invoice.jsx`), làm mới kết quả sang "Đã trả" | `RewardInvoiceDAO.createRewardInvoice` bỏ qua kiểm tra tài khoản nhận thưởng, lưu hóa đơn và gán `tblRewardInvoiceid` cho các kết quả thi đấu | Đúng yêu cầu: không hiện hóa đơn khi trả trực tiếp |

---

### 7. Bổ sung: Cổng thông tin Nhà tài trợ & Tay đua
| Đối tượng | Chức năng | Xử lý Frontend | Xử lý Backend | Ghi chú |
|---|---|---|---|---|
| **Nhà tài trợ** | Đăng nhập linh hoạt | Nhập username (vd: `redblue`) HOẶC mã số thuế (vd: `0123456789`) | `MemberDAO.checkLoginUniversal` kiểm tra cả username trong `tblMember` lẫn `taxCode` trong `tblSponsor` | Rất tiện lợi cho doanh nghiệp |
| **Nhà tài trợ** | Xem & quản lý hợp đồng | `/sponsor-portal`: xem thống kê, lọc hợp đồng (Tất cả, Đã ký, Chờ ký), xem chi tiết hợp đồng gồm các điều khoản (có hình phạt vi phạm) và quyền lợi | `SponsorDAO.getContractsBySponsor` và `ContractDAO.getContractById` | Hợp đồng minh bạch đầy đủ thông tin |
| **Tay đua** | Tra cứu thưởng & hóa đơn | `/driver-portal`: xem thông tin cá nhân & tài khoản nhận thưởng mặc định; xem bảng tiền thưởng theo từng chặng và trạng thái; bấm "Xem hóa đơn" để mở modal xem hóa đơn | `DriverDAO.getRewardsByDriver` và `RewardInvoiceDAO.getInvoiceById` | Tay đua theo dõi chính xác thu nhập và hóa đơn chứng từ |
| **Tay đua** | Sửa thông tin cá nhân | Nút "Chỉnh sửa thông tin" mở modal `EditDriver` (họ tên, ngày sinh, quốc tịch; tên đăng nhập và số xe chỉ đọc) | `DriverDAO.updateDriverInfo` chỉ đổi 3 trường trên; ném `DriverException` nếu thiếu tên / quốc tịch hoặc dưới 16 tuổi → HTTP 400 | Chỉ sửa được thông tin của chính mình (id lấy từ phiên đăng nhập) |
| **Tay đua** | Quản lý tài khoản ngân hàng | Bảng `tblBankAccount`: "Thêm tài khoản", "Sửa", "Đặt mặc định", "Xóa" (khóa với tài khoản mặc định); modal `BankAccountForm` | `DriverDAO.addBankAccount / updateBankAccount / deleteBankAccount`; ném `DriverException` → HTTP 400 khi dữ liệu sai, trùng (cùng ngân hàng + số TK), bỏ mặc định hoặc xóa tài khoản mặc định | Luôn có đúng 1 tài khoản mặc định (CSDL có thêm unique index `uq_driver_default_bank_account`) |

