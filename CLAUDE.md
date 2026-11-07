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
cd backend && ./mvnw test                  # 28 test, cần database đang chạy, tự rollback
cd frontend && npm install && npm run dev  # :5173, proxy /api → :8080
```
Đã sửa code thì build lại image: `docker compose up -d --build backend` (hoặc `frontend`).

## Quy tắc chung
- Giao diện và thông báo viết tiếng Việt. Tên lớp, hàm, biến **giữ đúng tên trong báo cáo** (vd: `SponsorDAO.searchByName`, `txtKeyword`).
- Commit theo Conventional Commits (`feat:`, `fix:`, `docs:`...).
- Không thêm thư viện ngoài danh sách trong `backend/CLAUDE.md` / `frontend/CLAUDE.md` khi chưa hỏi.
- Cài đặt một chức năng mới: dùng skill `/implement-feature`.
