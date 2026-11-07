---
name: implement-feature
description: Cài đặt một chức năng của Module M3 (Quản lý thông tin nhà tài trợ, Ký hợp đồng tài trợ, Trả thưởng cho tay đua theo chặng đua) end-to-end từ CSDL → DAO → RestController → HTTPClient → trang JSX, bám đúng báo cáo Pha thiết kế. Dùng khi người dùng yêu cầu code, cài đặt hoặc hoàn thiện một trong các chức năng này hoặc một phần của chúng.
---

# Cài đặt một chức năng theo báo cáo thiết kế

Tham số: tên chức năng (vd: `ký hợp đồng`). Nếu không rõ là chức năng nào thì hỏi lại.

## 1. Đọc thiết kế của chức năng
Đọc trước phần của chức năng đó trong `backend/CLAUDE.md` và `frontend/CLAUDE.md`, vì chi tiết đã được trích sẵn ở đây.

Khi cần đối chiếu thêm, trích text từ `docs/report/B23DCCN465 - Nguyễn Tự Kiên - Báo cáo tổng hợp.pdf` (`pdftotext -layout`) rồi đọc các mục:
- **2.x Kịch bản chuẩn và ngoại lệ**: câu thông báo, dữ liệu mẫu
- **9.x Thiết kế GD và lớp chi tiết**: trang `.jsx`, DAO và chữ ký phương thức
- **10.x Biểu đồ hoạt động**: lưu/lấy gì từ session, nhánh lỗi
- **11.x Biểu đồ tuần tự**: thứ tự lời gọi

**Không dùng** các file PDF từng pha khi chúng khác báo cáo tổng hợp.

Lập danh sách **trang, DAO.method, entity** cần làm, đánh dấu những thứ đã có trong code, rồi trình bày cho người dùng trước khi code.

## 2. CSDL
- Bảng hoặc cột còn thiếu: thêm vào `backend/src/main/resources/schema.sql` theo bảng **"Bảng (`schema.sql`)"** trong `backend/CLAUDE.md` (cột, kiểu, khóa ngoại).
- Thêm dữ liệu mẫu theo mục **"Dữ liệu mẫu"** trong `backend/CLAUDE.md`.

## 3. Entity (`model/`)
Tạo hoặc bổ sung entity với thuộc tính và kiểu theo mục **"Entity (`model/`)"** trong `backend/CLAUDE.md`, ánh xạ JPA theo các ghi chú ngay dưới bảng CSDL. Dùng Lombok (`@Getter @Setter @NoArgsConstructor`), không viết getter/setter tay.

## 4. DAO (`dao/`)
- Kế thừa đúng báo cáo (`OrganizerDAO extends MemberDAO`...).
- Tên, input, output của phương thức **giống hệt báo cáo**.
- Mỗi phương thức DAO mới phải có test trong `src/test/.../dao/` (một ca thành công, một ca không tìm thấy/không hợp lệ).

## 5. RestController (`controller/`)
- Thêm endpoint theo bảng API trong `backend/CLAUDE.md`. Endpoint chưa có trong bảng thì đề xuất rồi cập nhật bảng.
- Controller chỉ nhận request, gọi DAO, trả kết quả hoặc lỗi `{ message }`. Không chứa truy vấn.

## 6. Frontend
- Gọi backend qua `HTTPClient.fetch(...)`; trạng thái chia sẻ giữa các trang dùng hook `useSession` (hoặc hook riêng trong `src/hooks/`), hàm thuần đặt ở `src/utils/`.
- Tạo trang `src/pages/<Tên>.jsx` theo đúng bảng của trang đó trong `frontend/CLAUDE.md` (tên phần tử, nhãn, cột bảng, hành vi, thông báo).
- Lưu/lấy thuộc tính ẩn qua `useSession(key)` đúng như biểu đồ hoạt động.
- Chỉ **trang chức năng chính** mới đăng ký route trong `App.jsx` và có thẻ ở `OrganizerHome`. Giao diện con mở từ trang chính (chi tiết, thêm mới, hóa đơn...) làm thành **cửa sổ nổi** (`Modal` / `FormModal`) ngay trong trang chính, không thêm route (xem mục "Route và cửa sổ nổi" trong `frontend/CLAUDE.md`).

## 7. Kiểm tra
```bash
docker compose up -d database
cd backend && ./mvnw test          # test DAO + ApiTest, tự rollback
cd frontend && npm run build
docker compose up -d --build       # chạy thử ở http://localhost:3000
```
Sau đó chạy cả hệ thống và đi hết **kịch bản chuẩn** cùng ít nhất một **kịch bản ngoại lệ** của chức năng (vd: tìm "Red" không ra kết quả). Báo lại kết quả thật, kể cả bước bị lỗi.

## Checklist hoàn thành
- [ ] Tên trang, phần tử, DAO, phương thức, bảng, cột khớp báo cáo
- [ ] Không có tầng service, không dùng `JpaRepository`
- [ ] Thông báo lỗi đúng câu trong báo cáo
- [ ] `./mvnw test` pass, `npm run build` pass
- [ ] Đã chạy thử kịch bản chuẩn và ngoại lệ
- [ ] Lệch nào so với báo cáo đều đã hỏi và được người dùng đồng ý
