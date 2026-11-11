-- Dữ liệu mẫu theo kịch bản trong Báo cáo tổng hợp (mục 2) & các ca kiểm thử mở rộng.
-- Chạy mỗi lần khởi động: id cố định + ON CONFLICT DO NOTHING nên không bị nhân bản,
-- dữ liệu người dùng thêm sau (id từ sequence) được giữ nguyên.

-- ===== Thành viên =====
INSERT INTO tblMember (id, username, password) VALUES
 (1, 'a', 'a@123'),
 (2, 'b', 'b@123'),
 (3, 'redbull', 'redbull@123'),
 (4, 'redblue', 'redblue@123'),
 (5, 'mercedes', 'mercedes@123'),
 (6, 'oracle', 'oracle@123'),
 (7, 'heineken', 'heineken@123'),
 (8, 'honda', 'honda@123'),
 (9, 'vietnamairlines', 'vna@123'),
 (10, 'team.cyber', '123456'),
 (11, 'team.redbull', '123456'),
 (12, 'team.mclaren', '123456'),
 (13, 'team.ferrari', '123456'),
 (14, 'team.mercedes', '123456'),
 (15, 'team.williams', '123456'),
 (20, 'driver.tranvanb', '123456'),
 (21, 'driver.nguyenvanan', '123456'),
 (22, 'driver.verstappen', '123456'),
 (23, 'driver.norris', '123456'),
 (24, 'driver.leclerc', '123456'),
 (25, 'driver.hamilton', '123456'),
 (26, 'driver.piastri', '123456'),
 (27, 'driver.leminhcuong', '123456'),
 (28, 'driver.russell', '123456'),
 (29, 'driver.sainz', '123456'),
 (30, 'driver.albon', '123456'),
 (31, 'khachhang', '123456'),
 (32, 'customer', 'customer@123')
ON CONFLICT DO NOTHING;

INSERT INTO tblOrganizer (tblMemberid, fullName, address, dateOfBirth, email, tel, role) VALUES
 (1, 'Nguyễn Văn A', '96A Trần Phú, Hà Đông, Hà Nội', '1995-04-12', 'nguyenvana@f1.vn', '0912345678', 'Nhân viên Ban tổ chức'),
 (2, 'Nguyễn Thị B', '122 Hoàng Quốc Việt, Cầu Giấy, Hà Nội', '1997-08-20', 'nguyenthib@f1.vn', '0923456789', 'Nhân viên Ban tổ chức')
ON CONFLICT DO NOTHING;

INSERT INTO tblSponsor (tblMemberid, name, taxCode, field, address, email, tel, status) VALUES
 (3, 'Red Bull', '0301234567', 'Đồ uống', 'Fuschl am See, Áo', 'redbull@gmail.com', '0901234567', 'Đang hợp tác'),
 (4, 'Red Blue', '0123456789', 'Truyền thông và quảng cáo', '18 Nguyễn Trãi, Hà Nội', 'redblue@gmail.com', '0987654321', 'Đang hợp tác'),
 (5, 'Mercedes-AMG Petronas', '0309876543', 'Ô tô', 'Brackley, Vương quốc Anh', 'mercedes@gmail.com', '0911222333', 'Đang hợp tác'),
 (6, 'Oracle', '0105556667', 'Công nghệ', 'Austin, Texas, Hoa Kỳ', 'oracle@gmail.com', '0933444555', 'Ngừng hợp tác'),
 (7, 'Heineken', '0302223334', 'Đồ uống', 'Amsterdam, Hà Lan', 'heineken@gmail.com', '0944555666', 'Đang hợp tác'),
 -- Tình huống: Nhà tài trợ mới, chưa từng ký hợp đồng nào
 (8, 'Honda Racing', '0307778899', 'Động cơ & Kỹ thuật', 'Tokyo, Nhật Bản', 'honda@f1.com', '0977889900', 'Đang hợp tác'),
 -- Tình huống: Nhà tài trợ có hợp đồng cũ đã hết hạn ở mùa giải trước (2024)
 (9, 'Vietnam Airlines', '0101234999', 'Hàng không', 'Long Biên, Hà Nội', 'vna@f1.vn', '0988776655', 'Đang hợp tác')
ON CONFLICT DO NOTHING;

INSERT INTO tblTeam (tblMemberid, name, manufacturer, description) VALUES
 (10, 'Cyber', 'Cyber Motors', 'Đội đua Việt Nam'),
 (11, 'Red Bull', 'Honda RBPT', 'Oracle Red Bull Racing'),
 (12, 'McLaren', 'Mercedes', 'McLaren F1 Team'),
 (13, 'Ferrari', 'Ferrari', 'Scuderia Ferrari'),
 (14, 'Mercedes', 'Mercedes', 'Mercedes-AMG Petronas F1 Team'),
 (15, 'Williams', 'Mercedes', 'Williams Racing - Đội đua giàu truyền thống')
ON CONFLICT DO NOTHING;

INSERT INTO tblDriver (tblMemberid, fullName, dateOfBirth, nationality, racingNumber) VALUES
 (20, 'Trần Văn B', '1998-05-12', 'Việt Nam', 50),
 (21, 'Nguyễn Văn An', '2000-09-01', 'Việt Nam', 13),
 (22, 'Max Verstappen', '1997-09-30', 'Hà Lan', 10),
 (23, 'Lando Norris', '1999-11-13', 'Anh', 4),
 (24, 'Charles Leclerc', '1997-10-16', 'Monaco', 16),
 (25, 'Lewis Hamilton', '1985-01-07', 'Anh', 44),
 (26, 'Oscar Piastri', '2001-04-06', 'Úc', 81),
 (27, 'Lê Minh Cường', '2001-02-20', 'Việt Nam', 27),
 (28, 'George Russell', '1998-02-15', 'Anh', 63),
 (29, 'Carlos Sainz', '1994-09-01', 'Tây Ban Nha', 55),
 (30, 'Alexander Albon', '1996-03-23', 'Thái Lan', 23)
ON CONFLICT DO NOTHING;

INSERT INTO tblRegisteredDriver (id, tblTeamMemberid, tblDriverMemberid) VALUES
 (1, 10, 20), (2, 10, 21), (3, 11, 22), (4, 12, 23), (5, 13, 24),
 (6, 13, 25), (7, 12, 26), (8, 10, 27), (9, 14, 28),
 (10, 13, 29), (11, 15, 30)
ON CONFLICT DO NOTHING;

-- ===== Ngân hàng & tài khoản =====
INSERT INTO tblBank (id, bankCode, name, status) VALUES
 (1, 'VCB', 'Vietcombank', 'Hoạt động'),
 (2, 'TCB', 'Techcombank', 'Hoạt động'),
 (3, 'BIDV', 'BIDV', 'Hoạt động'),
 (4, 'MB', 'MB Bank', 'Hoạt động'),
 (5, 'ACB', 'Á Châu (ACB)', 'Hoạt động'),
 (6, 'VPB', 'VPBank', 'Hoạt động')
ON CONFLICT DO NOTHING;

-- Dữ liệu tài khoản ngân hàng bao quát các tình huống:
-- 1. Tay đua có 1 tài khoản mặc định duy nhất: Nguyễn Văn An (21), Lewis Hamilton (25), Oscar Piastri (26), George Russell (28)
-- 2. TÌNH HUỐNG TRỌNG TÂM: 1 user có NHIỀU tài khoản ngân hàng, nhưng CHỈ ĐÚNG 1 tài khoản là mặc định (isDefault = TRUE):
--    - Trần Văn B (20): có 3 tài khoản (VCB default=TRUE, TCB default=FALSE, MB default=FALSE)
--    - Max Verstappen (22): có 2 tài khoản (VCB default=TRUE, TCB default=FALSE)
--    - Lando Norris (23): có 3 tài khoản (BIDV default=TRUE, VCB default=FALSE, ACB default=FALSE)
--    - Charles Leclerc (24): có 2 tài khoản (MB default=TRUE, BIDV default=FALSE)
-- 3. TÌNH HUỐNG: 1 user có NHIỀU tài khoản nhưng KHÔNG CÓ tài khoản nào là mặc định (toàn bộ FALSE):
--    - Lê Minh Cường (27): có 2 tài khoản (VCB FALSE, BIDV FALSE) -> ngoại lệ "Thiếu thông tin nhận thưởng"
-- 4. TÌNH HUỐNG: 1 user có tài khoản mặc định (TRUE) nhưng tài khoản đó đã bị KHÓA ('Tạm khóa'):
--    - Carlos Sainz (29): TK default bị 'Tạm khóa', TK phụ thì 'Hoạt động' -> ngoại lệ không đủ điều kiện thanh toán
-- 5. TÌNH HUỐNG: 1 user HOÀN TOÀN CHƯA CÓ tài khoản ngân hàng nào trong hệ thống:
--    - Alexander Albon (30): không có bản ghi nào trong tblBankAccount -> ngoại lệ "Thiếu thông tin nhận thưởng"
INSERT INTO tblBankAccount (id, accountNumber, accountHolder, isDefault, status, tblDriverMemberid, tblBankid) VALUES
 -- [Nhóm 1] Tài khoản mặc định ban đầu:
 (1, '0123456789', 'TRAN VAN B', TRUE, 'Hoạt động', 20, 1),
 (2, '1903456789', 'NGUYEN VAN AN', TRUE, 'Hoạt động', 21, 2),
 (3, '0712345678', 'MAX VERSTAPPEN', TRUE, 'Hoạt động', 22, 1),
 (4, '2201345678', 'LANDO NORRIS', TRUE, 'Hoạt động', 23, 3),
 (5, '3301234567', 'CHARLES LECLERC', TRUE, 'Hoạt động', 24, 4),
 (6, '4401234567', 'LEWIS HAMILTON', TRUE, 'Hoạt động', 25, 2),
 (7, '8101234567', 'OSCAR PIASTRI', TRUE, 'Hoạt động', 26, 3),
 (8, '2701234567', 'LE MINH CUONG', FALSE, 'Hoạt động', 27, 1),
 (9, '6301234567', 'GEORGE RUSSELL', TRUE, 'Hoạt động', 28, 4),

 -- [Nhóm 2] Thêm tài khoản phụ (isDefault = FALSE) cho user có nhiều tài khoản:
 -- Trần Văn B (20) có thêm 2 TK phụ:
 (10, '1908765432', 'TRAN VAN B', FALSE, 'Hoạt động', 20, 2),
 (11, '0987654321', 'TRAN VAN B', FALSE, 'Hoạt động', 20, 4),
 -- Max Verstappen (22) có thêm 1 TK phụ:
 (12, '1902223334', 'MAX VERSTAPPEN', FALSE, 'Hoạt động', 22, 2),
 -- Lando Norris (23) có thêm 2 TK phụ:
 (13, '0012345678', 'LANDO NORRIS', FALSE, 'Hoạt động', 23, 1),
 (14, '5678901234', 'LANDO NORRIS', FALSE, 'Hoạt động', 23, 5),
 -- Charles Leclerc (24) có thêm 1 TK phụ:
 (15, '2151234567', 'CHARLES LECLERC', FALSE, 'Hoạt động', 24, 3),

 -- [Nhóm 3] Lê Minh Cường (27) có thêm TK thứ hai nhưng cũng là FALSE (không có TK default nào):
 (16, '2159998888', 'LE MINH CUONG', FALSE, 'Hoạt động', 27, 3),

 -- [Nhóm 4] Carlos Sainz (29) có TK default bị Tạm khóa:
 (17, '1905556667', 'CARLOS SAINZ', TRUE, 'Tạm khóa', 29, 2),
 (18, '0077778888', 'CARLOS SAINZ', FALSE, 'Hoạt động', 29, 1)
ON CONFLICT DO NOTHING;

-- ===== Mùa giải & chặng đua =====
INSERT INTO tblSeason (id, name, year, startDate, endDate) VALUES
 (1, 'F1 FIA 2025', 2025, '2025-03-06', '2025-12-06'),
 (2, 'F1 FIA 2024', 2024, '2024-03-01', '2024-12-08'),
 (3, 'F1 FIA 2026', 2026, '2026-03-06', '2026-12-06'),
 (4, 'F1 FIA 2027', 2027, '2027-03-05', '2027-12-05')
ON CONFLICT DO NOTHING;

-- C02 2025: chưa cấu hình mức thưởng & chưa có kết quả -> ngoại lệ thiếu dữ liệu
-- F1 FIA 2027: mùa giải tương lai chưa có chặng nào kết thúc
INSERT INTO tblRace (id, name, lapCount, location, startTime, endTime, description, basePrize, tblSeasonid) VALUES
 (1, 'C01 – Bahrain', 57, 'Sakhir, Bahrain', '2025-03-09 15:00:00', '2025-03-09 17:00:00', 'Chặng mở màn mùa giải 2025', 100000000, 1),
 (2, 'C02 – Saudi Arabia', 50, 'Jeddah, Ả Rập Xê Út', '2025-03-23 19:00:00', '2025-03-23 21:00:00', 'Đường phố Jeddah', NULL, 1),
 (3, 'C03 – Australian', 58, 'Melbourne, Úc', '2025-04-05 13:00:00', '2025-04-05 15:00:00', 'Albert Park', 100000000, 1),
 (4, 'C24 – Abu Dhabi', 58, 'Yas Marina, UAE', '2024-12-08 17:00:00', '2024-12-08 19:00:00', 'Chặng cuối mùa giải 2024', 120000000, 2),
 (5, 'C01 – Bahrain', 57, 'Sakhir, Bahrain', '2026-03-08 15:00:00', '2026-03-08 17:00:00', 'Chặng mở màn mùa giải 2026', 100000000, 3),
 (6, 'C24 – Abu Dhabi', 58, 'Yas Marina, UAE', '2026-12-06 17:00:00', '2026-12-06 19:00:00', 'Chặng cuối mùa giải 2026', 120000000, 3),
 (7, 'C01 – Bahrain', 57, 'Sakhir, Bahrain', '2027-03-07 15:00:00', '2027-03-07 17:00:00', 'Chặng mở màn mùa giải 2027', 100000000, 4)
ON CONFLICT DO NOTHING;

-- ===== Hóa đơn đã lập =====
-- HD001: Trả thưởng cho Max Verstappen ở chặng C03 2025 (Chuyển khoản online)
-- HD002: Trả thưởng toàn bộ chặng C24 Abu Dhabi 2026 (Ủy nhiệm chi - phương thức thanh toán khác)
INSERT INTO tblRewardInvoice (id, barcode, paidAt, amount, paymentMethod, status, tblOrganizerMemberid) VALUES
 (1, 'HD001', '2025-04-06 10:00:00', 60000000, 'Chuyển khoản online', 'Thanh toán thành công', 2),
 (2, 'HD002', '2026-12-07 10:00:00', 278400000, 'Ủy nhiệm chi', 'Thanh toán thành công', 1)
ON CONFLICT DO NOTHING;

-- ===== Kết quả thi đấu =====
-- reward của kết quả chưa trả được RaceDAO.getRaceInfo() tính lại theo hệ số xếp hạng
INSERT INTO tblRegisteredReceipt (id, rank, score, finishedTime, completedLap, reward, completedStatus, tblRaceid, tblRegisteredDriverid, tblOrganizerMemberid, tblRewardInvoiceid) VALUES
 -- C03 – Australian 2025 (Do BTC Nguyễn Văn A - id 1 đưa vào hệ thống; có kết quả chưa trả, đã trả, bỏ cuộc)
 (1, 1, 25, '2025-04-05 14:32:00', 58, 0, 'Hoàn thành', 3, 1, 1, NULL),
 (2, 2, 18, '2025-04-05 14:32:05', 58, 0, 'Hoàn thành', 3, 2, 1, NULL),
 (3, 3, 15, '2025-04-05 14:32:11', 58, 60000000, 'Hoàn thành', 3, 3, 1, 1),
 (4, 4, 12, '2025-04-05 14:32:20', 58, 0, 'Hoàn thành', 3, 4, 1, NULL),
 (5, 5, 10, '2025-04-05 14:32:32', 58, 0, 'Hoàn thành', 3, 5, 1, NULL),
 (6, 6, 8, '2025-04-05 14:32:40', 58, 0, 'Hoàn thành', 3, 6, 1, NULL),
 (7, 7, 6, '2025-04-05 14:32:55', 58, 0, 'Hoàn thành', 3, 9, 1, NULL),
 (8, 8, 4, '2025-04-05 14:33:10', 57, 0, 'Hoàn thành', 3, 8, 1, NULL),
 (9, NULL, 0, NULL, 30, 0, 'Bỏ cuộc', 3, 7, 1, NULL),

 -- C24 – Abu Dhabi 2024 (Do BTC Nguyễn Văn A - id 1 đưa vào hệ thống; chặng có tai nạn)
 (10, 1, 25, '2024-12-08 18:30:00', 58, 0, 'Hoàn thành', 4, 3, 1, NULL),
 (11, 2, 18, '2024-12-08 18:30:10', 58, 0, 'Hoàn thành', 4, 4, 1, NULL),
 (12, 3, 15, '2024-12-08 18:30:20', 58, 0, 'Hoàn thành', 4, 5, 1, NULL),
 (13, 4, 12, '2024-12-08 18:30:30', 58, 0, 'Hoàn thành', 4, 7, 1, NULL),
 (14, NULL, 0, NULL, 12, 0, 'Tai nạn', 4, 6, 1, NULL),

 -- C01 – Bahrain 2025 (Do BTC Nguyễn Thị B - id 2 đưa vào hệ thống; Leclerc và Norris đồng hạng 2; Russell không xuất phát)
 (15, 1, 25, '2025-03-09 16:33:20', 57, 0, 'Hoàn thành', 1, 3, 2, NULL),
 (16, 2, 18, '2025-03-09 16:33:30', 57, 0, 'Hoàn thành', 1, 5, 2, NULL),
 (17, 2, 18, '2025-03-09 16:33:30', 57, 0, 'Hoàn thành', 1, 4, 2, NULL),
 (18, 4, 12, '2025-03-09 16:33:50', 57, 0, 'Hoàn thành', 1, 6, 2, NULL),
 (19, NULL, 0, NULL, 0, 0, 'Không xuất phát', 1, 9, 2, NULL),

 -- C01 – Bahrain 2026 (Do BTC Nguyễn Thị B - id 2 đưa vào hệ thống; đã có kết quả chặng mở màn 2026, chưa chi trả)
 (20, 1, 25, '2026-03-08 16:31:00', 57, 0, 'Hoàn thành', 5, 4, 2, NULL),
 (21, 2, 18, '2026-03-08 16:31:04', 57, 0, 'Hoàn thành', 5, 7, 2, NULL),
 (22, 3, 15, '2026-03-08 16:31:15', 57, 0, 'Hoàn thành', 5, 1, 2, NULL),
 (23, 4, 12, '2026-03-08 16:31:22', 57, 0, 'Hoàn thành', 5, 2, 2, NULL),

 -- C24 – Abu Dhabi 2026 (Do BTC Nguyễn Văn A - id 1 đưa vào hệ thống; đã được chi trả qua hóa đơn HD002)
 -- Tay đua hạng 11 ngoài top có thưởng (reward = 0) và tay đua bỏ cuộc
 (24, 1, 25, '2026-12-06 18:35:10', 58, 120000000, 'Hoàn thành', 6, 4, 1, 2),
 (25, 2, 18, '2026-12-06 18:35:22', 58, 86400000, 'Hoàn thành', 6, 5, 1, 2),
 (26, 3, 15, '2026-12-06 18:35:35', 58, 72000000, 'Hoàn thành', 6, 3, 1, 2),
 (27, 11, 0, '2026-12-06 18:37:00', 57, 0, 'Hoàn thành', 6, 11, 1, NULL),
 (28, NULL, 0, NULL, 22, 0, 'Bỏ cuộc', 6, 10, 1, NULL)
ON CONFLICT DO NOTHING;

-- ===== Điều khoản & quyền lợi =====
INSERT INTO tblTerm (id, title, details, punishment, type, status) VALUES
 (1, 'Thanh toán tài trợ theo chặng', 'Nhà tài trợ thanh toán giá trị tài trợ theo từng chặng đua', 'Phạt 0,05% mỗi ngày trên số tiền chậm thanh toán', 'Tài chính', 'Hoạt động'),
 (2, 'Cung cấp tư liệu quảng bá theo chặng', 'Nhà tài trợ cung cấp logo, hình ảnh, video quảng bá cho từng chặng', 'Ban tổ chức không chịu trách nhiệm nếu tư liệu gửi trễ', 'Quảng bá', 'Hoạt động'),
 (3, 'Điều chỉnh lịch thực hiện tài trợ theo chặng khi lịch đua thay đổi', 'Hai bên điều chỉnh lịch tài trợ khi FIA thay đổi lịch đua', 'Không áp dụng', 'Pháp lý', 'Hoạt động'),
 (4, 'Bảo mật thông tin hợp đồng', 'Hai bên không tiết lộ giá trị và nội dung hợp đồng cho bên thứ ba', 'Bồi thường thiệt hại theo quy định pháp luật', 'Pháp lý', 'Hoạt động'),
 (5, 'Trưng bày sản phẩm tại paddock', 'Nhà tài trợ có quyền đặt gian hàng trưng bày sản phẩm trong khu vực paddock', 'Ban tổ chức nhắc nhở và có quyền đình chỉ gian hàng nếu vi phạm an toàn', 'Quảng bá', 'Hoạt động')
ON CONFLICT DO NOTHING;

INSERT INTO tblBenefit (id, title, description, type, status) VALUES
 (1, 'Hiển thị logo nhà tài trợ theo chặng', 'Logo trên biển quảng cáo dọc đường đua', 'Quảng bá thương hiệu', 'Hoạt động'),
 (2, 'Cấp vé VIP theo chặng', 'Vé khu VIP Paddock Club', 'Sự kiện', 'Hoạt động'),
 (3, 'Giới thiệu nhà tài trợ tại sự kiện theo chặng', 'MC giới thiệu nhà tài trợ tại lễ khai mạc chặng', 'Hình ảnh – Truyền thông', 'Hoạt động'),
 (4, 'Logo trên thân xe', 'In logo nhà tài trợ trên thân xe đua', 'Quảng bá thương hiệu', 'Hoạt động'),
 (5, 'Quảng cáo trên màn hình LED trường đua', 'Phát video quảng cáo 30s giữa các phiên đua', 'Hình ảnh – Truyền thông', 'Hoạt động')
ON CONFLICT DO NOTHING;

-- ===== Hợp đồng =====
INSERT INTO tblContract (id, barcode, createdDate, effectiveFrom, effectiveTo, value, sponsorRepresentative, organizerRepresentative, signedDate, status, tblSponsorMemberid, tblSeasonid, tblOrganizerMemberid) VALUES
 (1, 'C101', '2025-01-10', '2025-03-06', '2025-12-06', 500000000, 'Christian Horner', 'Nguyễn Văn A', '2025-01-15', 'Đã ký', 3, 1, 1),
 (2, 'C102', '2026-01-05', '2026-03-06', '2026-12-06', 300000000, 'Christian Horner', 'Nguyễn Thị B', '2026-01-08', 'Đã ký', 3, 3, 2),
 (3, 'C103', '2026-02-01', '2026-03-06', '2026-12-06', 200000000, 'Toto Wolff', 'Nguyễn Thị B', NULL, 'Chờ ký', 5, 3, 2),
 -- Hợp đồng cũ đã hết hạn từ mùa giải 2024
 (4, 'C104', '2024-01-10', '2024-03-01', '2024-12-08', 400000000, 'Lê Hồng Hà', 'Nguyễn Văn A', '2024-01-15', 'Đã ký', 9, 2, 1),
 -- Hợp đồng của Red Blue (S018 - id 4): 1 Chờ ký (2026), 1 Đã ký (2025)
 (5, 'C105', '2026-02-15', '2026-03-08', '2026-12-06', 350000000, 'Nguyễn Văn Cường', 'Nguyễn Thị B', NULL, 'Chờ ký', 4, 3, 2),
 (6, 'C106', '2025-02-10', '2025-03-09', '2025-12-07', 250000000, 'Nguyễn Văn Cường', 'Nguyễn Văn A', '2025-02-14', 'Đã ký', 4, 1, 1)
ON CONFLICT DO NOTHING;

INSERT INTO tblContractTerm (id, notes, tblTermid, tblContractid) VALUES
 (1, 'Thanh toán trong vòng 7 ngày sau khi kết thúc chặng', 1, 1),
 (2, 'Gửi tư liệu trước ngày tổ chức chặng 14 ngày', 2, 1),
 (3, NULL, 1, 2),
 (4, NULL, 4, 3),
 (5, 'Thanh toán trọn gói đầu mùa giải 2024', 1, 4),
 (6, 'Thanh toán tài trợ theo tiến độ 3 đợt', 1, 5),
 (7, 'Cung cấp banner và video trước mỗi chặng', 2, 5),
 (8, 'Thanh toán theo từng chặng hoàn thành', 1, 6)
ON CONFLICT DO NOTHING;

INSERT INTO tblContractBenefit (id, value, unit, notes, tblBenefitid, tblContractid) VALUES
 (1, 2, 'vị trí', 'Hai bên sườn xe', 4, 1),
 (2, 20, 'vé', NULL, 2, 1),
 (3, 24, 'chặng', NULL, 1, 2),
 (4, 10, 'vé', NULL, 2, 3),
 (5, 50, 'vé', 'Vé khán đài Grandstand mùa 2024', 2, 4),
 (6, 12, 'chặng', 'Đặt logo tại paddock', 1, 5),
 (7, 15, 'vé', 'Vé VIP cho ban lãnh đạo', 2, 5),
 (8, 20, 'vé', 'Vé VIP mùa 2025', 2, 6)
ON CONFLICT DO NOTHING;

-- =====================================================================
-- ===== Bổ sung: dữ liệu bao quát các tình huống còn thiếu =====
-- Id mới đặt sau các id đã dùng (cả dữ liệu người dùng đã tạo) để chạy được trên CSDL đang có.
-- =====================================================================

-- Tay đua mới chưa thi đấu chặng nào, chưa thuộc đội nào -> trang chủ Tay đua hiện bảng tiền thưởng trống
INSERT INTO tblMember (id, username, password) VALUES
 (33, 'driver.doanminhkhoa', '123456')
ON CONFLICT DO NOTHING;
INSERT INTO tblDriver (tblMemberid, fullName, dateOfBirth, nationality, racingNumber) VALUES
 (33, 'Đoàn Minh Khoa', '2004-07-15', 'Việt Nam', 88)
ON CONFLICT DO NOTHING;

-- Ngân hàng ngừng hoạt động: không hiện trong ô chọn ngân hàng khi thêm / sửa tài khoản
INSERT INTO tblBank (id, bankCode, name, status) VALUES
 (7, 'SCB', 'Sài Gòn (SCB)', 'Ngừng hoạt động')
ON CONFLICT DO NOTHING;

INSERT INTO tblBankAccount (id, accountNumber, accountHolder, isDefault, status, tblDriverMemberid, tblBankid) VALUES
 -- George Russell (28) có thêm TK phụ ở ngân hàng đã ngừng hoạt động
 (19, '7701234567', 'GEORGE RUSSELL', FALSE, 'Hoạt động', 28, 7),
 -- Đoàn Minh Khoa (33): 1 tài khoản mặc định duy nhất (không xóa được, không bỏ mặc định được)
 (20, '8801234567', 'DOAN MINH KHOA', TRUE, 'Hoạt động', 33, 6)
ON CONFLICT DO NOTHING;

-- Mùa giải tương lai chưa có chặng đua nào
INSERT INTO tblSeason (id, name, year, startDate, endDate) VALUES
 (5, 'F1 FIA 2028', 2028, '2028-03-03', '2028-12-03')
ON CONFLICT DO NOTHING;

-- Thêm chặng đã kết thúc của mùa 2026:
--  C02 Saudi Arabia: đủ dữ liệu, có kết quả cho các tay đua ở mọi ngoại lệ tài khoản (Sainz TK mặc định bị khóa,
--                    Albon chưa có TK, Cường không có TK mặc định), có kết quả đã trả trực tiếp, bỏ cuộc, bị truất quyền
--  C03 Australian:   có mức thưởng nhưng CHƯA có kết quả -> ngoại lệ thiếu dữ liệu kết quả
--  C04 Japan:        có kết quả nhưng CHƯA có mức thưởng -> ngoại lệ thiếu mức thưởng cơ sở
--  C05 Miami:        mức thưởng khác (80 triệu), một kết quả đã trả chung hóa đơn với C02
INSERT INTO tblRace (id, name, lapCount, location, startTime, endTime, description, basePrize, tblSeasonid) VALUES
 (8, 'C02 – Saudi Arabia', 50, 'Jeddah, Ả Rập Xê Út', '2026-03-22 19:00:00', '2026-03-22 21:00:00', 'Đường phố Jeddah', 100000000, 3),
 (9, 'C03 – Australian', 58, 'Melbourne, Úc', '2026-04-05 13:00:00', '2026-04-05 15:00:00', 'Albert Park', 100000000, 3),
 (10, 'C04 – Japan', 53, 'Suzuka, Nhật Bản', '2026-04-19 14:00:00', '2026-04-19 16:00:00', 'Suzuka Circuit', NULL, 3),
 (11, 'C05 – Miami', 57, 'Miami, Hoa Kỳ', '2026-05-03 20:00:00', '2026-05-03 22:00:00', 'Miami International Autodrome', 80000000, 3)
ON CONFLICT DO NOTHING;

-- HD012: Trả thưởng trực tiếp (không cần tài khoản ngân hàng)
-- HD013: một hóa đơn gộp nhiều chặng (C02 + C05 2026) của cùng tay đua
INSERT INTO tblRewardInvoice (id, barcode, paidAt, amount, paymentMethod, status, tblOrganizerMemberid) VALUES
 (12, 'HD012', '2026-03-24 09:30:00', 24000000, 'Trả thưởng trực tiếp', 'Thanh toán thành công', 2),
 (13, 'HD013', '2026-05-05 10:15:00', 180000000, 'Chuyển khoản online', 'Thanh toán thành công', 1)
ON CONFLICT DO NOTHING;

INSERT INTO tblRegisteredReceipt (id, rank, score, finishedTime, completedLap, reward, completedStatus, tblRaceid, tblRegisteredDriverid, tblOrganizerMemberid, tblRewardInvoiceid) VALUES
 -- C02 – Saudi Arabia 2026 (BTC Nguyễn Thị B đưa vào)
 (29, 1, 25, '2026-03-22 20:21:10', 50, 100000000, 'Hoàn thành', 8, 6, 2, 13),   -- Hamilton: đã trả (HD013)
 (30, 2, 18, '2026-03-22 20:21:14', 50, 0, 'Hoàn thành', 8, 9, 2, NULL),          -- Russell: trả online được
 (31, 3, 15, '2026-03-22 20:21:30', 50, 0, 'Hoàn thành', 8, 10, 2, NULL),         -- Sainz: TK mặc định bị khóa -> online lỗi, trực tiếp được
 (32, 4, 12, '2026-03-22 20:21:41', 50, 0, 'Hoàn thành', 8, 11, 2, NULL),         -- Albon: chưa có TK -> online lỗi, trực tiếp được
 (33, 5, 10, '2026-03-22 20:21:55', 50, 0, 'Hoàn thành', 8, 8, 2, NULL),          -- Cường: không có TK mặc định -> online lỗi
 (34, 6, 8, '2026-03-22 20:22:03', 50, 0, 'Hoàn thành', 8, 3, 2, NULL),
 (35, 7, 6, '2026-03-22 20:22:20', 50, 24000000, 'Hoàn thành', 8, 5, 2, 12),      -- Leclerc: đã trả trực tiếp (HD012)
 (36, 8, 4, '2026-03-22 20:22:41', 49, 0, 'Hoàn thành', 8, 1, 2, NULL),
 (37, NULL, 0, NULL, 41, 0, 'Bỏ cuộc', 8, 4, 2, NULL),
 (38, NULL, 0, NULL, 50, 0, 'Bị truất quyền', 8, 7, 2, NULL),

 -- C04 – Japan 2026: có kết quả, chưa có mức thưởng
 (39, 1, 25, '2026-04-19 15:28:00', 53, 0, 'Hoàn thành', 10, 3, 1, NULL),
 (40, 2, 18, '2026-04-19 15:28:09', 53, 0, 'Hoàn thành', 10, 4, 1, NULL),

 -- C05 – Miami 2026
 (41, 1, 25, '2026-05-03 21:30:00', 57, 80000000, 'Hoàn thành', 11, 6, 1, 13),   -- Hamilton: đã trả (HD013)
 (42, 2, 18, '2026-05-03 21:30:06', 57, 0, 'Hoàn thành', 11, 3, 1, NULL),
 (43, 3, 15, '2026-05-03 21:30:12', 57, 0, 'Hoàn thành', 11, 2, 1, NULL)
ON CONFLICT DO NOTHING;

-- Điều khoản & quyền lợi ngừng hoạt động: không hiện trong gợi ý khi soạn hợp đồng, vẫn hiện trong hợp đồng cũ
INSERT INTO tblTerm (id, title, details, punishment, type, status) VALUES
 (6, 'Độc quyền ngành hàng', 'Ban tổ chức không ký với nhà tài trợ khác cùng ngành hàng trong mùa giải', 'Hoàn trả 20% giá trị hợp đồng', 'Pháp lý', 'Ngừng hoạt động')
ON CONFLICT DO NOTHING;

INSERT INTO tblBenefit (id, title, description, type, status) VALUES
 (6, 'Quảng cáo trên vé giấy', 'In logo nhà tài trợ trên vé giấy (đã chuyển sang vé điện tử)', 'Quảng bá thương hiệu', 'Ngừng hoạt động')
ON CONFLICT DO NOTHING;

-- Nhà tài trợ "Ngừng hợp tác" (Oracle) có hợp đồng cũ: không ký mới được, không xóa được
INSERT INTO tblContract (id, barcode, createdDate, effectiveFrom, effectiveTo, value, sponsorRepresentative, organizerRepresentative, signedDate, status, tblSponsorMemberid, tblSeasonid, tblOrganizerMemberid) VALUES
 (7, 'C107', '2024-01-20', '2024-03-01', '2024-12-08', 600000000, 'Safra Catz', 'Nguyễn Văn A', '2024-01-25', 'Đã ký', 6, 2, 1)
ON CONFLICT DO NOTHING;

INSERT INTO tblContractTerm (id, notes, tblTermid, tblContractid) VALUES
 (9, 'Độc quyền ngành công nghệ đám mây', 6, 7),
 (10, NULL, 4, 7)
ON CONFLICT DO NOTHING;

INSERT INTO tblContractBenefit (id, value, unit, notes, tblBenefitid, tblContractid) VALUES
 (9, 24, 'chặng', NULL, 6, 7),
 (10, 1, 'vị trí', 'Cánh gió sau', 4, 7)
ON CONFLICT DO NOTHING;

-- ===== Đồng bộ sequence sau khi chèn id cố định =====
SELECT setval(pg_get_serial_sequence('tblmember', 'id'), (SELECT MAX(id) FROM tblMember));
SELECT setval(pg_get_serial_sequence('tblseason', 'id'), (SELECT MAX(id) FROM tblSeason));
SELECT setval(pg_get_serial_sequence('tblrace', 'id'), (SELECT MAX(id) FROM tblRace));
SELECT setval(pg_get_serial_sequence('tblcontract', 'id'), (SELECT MAX(id) FROM tblContract));
SELECT setval(pg_get_serial_sequence('tblterm', 'id'), (SELECT MAX(id) FROM tblTerm));
SELECT setval(pg_get_serial_sequence('tblcontractterm', 'id'), (SELECT MAX(id) FROM tblContractTerm));
SELECT setval(pg_get_serial_sequence('tblbenefit', 'id'), (SELECT MAX(id) FROM tblBenefit));
SELECT setval(pg_get_serial_sequence('tblcontractbenefit', 'id'), (SELECT MAX(id) FROM tblContractBenefit));
SELECT setval(pg_get_serial_sequence('tblregistereddriver', 'id'), (SELECT MAX(id) FROM tblRegisteredDriver));
SELECT setval(pg_get_serial_sequence('tblregisteredreceipt', 'id'), (SELECT MAX(id) FROM tblRegisteredReceipt));
SELECT setval(pg_get_serial_sequence('tblrewardinvoice', 'id'), (SELECT MAX(id) FROM tblRewardInvoice));
SELECT setval(pg_get_serial_sequence('tblbank', 'id'), (SELECT MAX(id) FROM tblBank));
SELECT setval(pg_get_serial_sequence('tblbankaccount', 'id'), (SELECT MAX(id) FROM tblBankAccount));
