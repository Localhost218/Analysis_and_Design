-- Dữ liệu mẫu theo kịch bản trong Báo cáo tổng hợp (mục 2).
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
 (10, 'team.cyber', '123456'),
 (11, 'team.redbull', '123456'),
 (12, 'team.mclaren', '123456'),
 (13, 'team.ferrari', '123456'),
 (14, 'team.mercedes', '123456'),
 (20, 'driver.tranvanb', '123456'),
 (21, 'driver.nguyenvanan', '123456'),
 (22, 'driver.verstappen', '123456'),
 (23, 'driver.norris', '123456'),
 (24, 'driver.leclerc', '123456'),
 (25, 'driver.hamilton', '123456'),
 (26, 'driver.piastri', '123456'),
 (27, 'driver.leminhcuong', '123456'),
 (28, 'driver.russell', '123456')
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
 (7, 'Heineken', '0302223334', 'Đồ uống', 'Amsterdam, Hà Lan', 'heineken@gmail.com', '0944555666', 'Đang hợp tác')
ON CONFLICT DO NOTHING;

INSERT INTO tblTeam (tblMemberid, name, manufacturer, description) VALUES
 (10, 'Cyber', 'Cyber Motors', 'Đội đua Việt Nam'),
 (11, 'Red Bull', 'Honda RBPT', 'Oracle Red Bull Racing'),
 (12, 'McLaren', 'Mercedes', 'McLaren F1 Team'),
 (13, 'Ferrari', 'Ferrari', 'Scuderia Ferrari'),
 (14, 'Mercedes', 'Mercedes', 'Mercedes-AMG Petronas F1 Team')
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
 (28, 'George Russell', '1998-02-15', 'Anh', 63)
ON CONFLICT DO NOTHING;

INSERT INTO tblRegisteredDriver (id, tblTeamMemberid, tblDriverMemberid) VALUES
 (1, 10, 20), (2, 10, 21), (3, 11, 22), (4, 12, 23), (5, 13, 24),
 (6, 13, 25), (7, 12, 26), (8, 10, 27), (9, 14, 28)
ON CONFLICT DO NOTHING;

-- ===== Ngân hàng & tài khoản =====
INSERT INTO tblBank (id, bankCode, name, status) VALUES
 (1, 'VCB', 'Vietcombank', 'Hoạt động'),
 (2, 'TCB', 'Techcombank', 'Hoạt động'),
 (3, 'BIDV', 'BIDV', 'Hoạt động'),
 (4, 'MB', 'MB Bank', 'Hoạt động')
ON CONFLICT DO NOTHING;

-- Lê Minh Cường (27) không có tài khoản mặc định -> ngoại lệ "Thiếu thông tin nhận thưởng"
INSERT INTO tblBankAccount (id, accountNumber, accountHolder, isDefault, status, tblDriverMemberid, tblBankid) VALUES
 (1, '0123456789', 'TRAN VAN B', TRUE, 'Hoạt động', 20, 1),
 (2, '1903456789', 'NGUYEN VAN AN', TRUE, 'Hoạt động', 21, 2),
 (3, '0712345678', 'MAX VERSTAPPEN', TRUE, 'Hoạt động', 22, 1),
 (4, '2201345678', 'LANDO NORRIS', TRUE, 'Hoạt động', 23, 3),
 (5, '3301234567', 'CHARLES LECLERC', TRUE, 'Hoạt động', 24, 4),
 (6, '4401234567', 'LEWIS HAMILTON', TRUE, 'Hoạt động', 25, 2),
 (7, '8101234567', 'OSCAR PIASTRI', TRUE, 'Hoạt động', 26, 3),
 (8, '2701234567', 'LE MINH CUONG', FALSE, 'Hoạt động', 27, 1),
 (9, '6301234567', 'GEORGE RUSSELL', TRUE, 'Hoạt động', 28, 4)
ON CONFLICT DO NOTHING;

-- ===== Mùa giải & chặng đua =====
INSERT INTO tblSeason (id, name, year, startDate, endDate) VALUES
 (1, 'F1 FIA 2025', 2025, '2025-03-06', '2025-12-06'),
 (2, 'F1 FIA 2024', 2024, '2024-03-01', '2024-12-08'),
 (3, 'F1 FIA 2026', 2026, '2026-03-06', '2026-12-06'),
 (4, 'F1 FIA 2027', 2027, '2027-03-05', '2027-12-05')
ON CONFLICT DO NOTHING;

-- C02 2025: chưa cấu hình mức thưởng & chưa có kết quả -> ngoại lệ thiếu dữ liệu
-- F1 FIA 2027: chưa có chặng nào kết thúc
INSERT INTO tblRace (id, name, lapCount, location, startTime, endTime, description, basePrize, tblSeasonid) VALUES
 (1, 'C01 – Bahrain', 57, 'Sakhir, Bahrain', '2025-03-09 15:00:00', '2025-03-09 17:00:00', 'Chặng mở màn mùa giải 2025', 100000000, 1),
 (2, 'C02 – Saudi Arabia', 50, 'Jeddah, Ả Rập Xê Út', '2025-03-23 19:00:00', '2025-03-23 21:00:00', 'Đường phố Jeddah', NULL, 1),
 (3, 'C03 – Australian', 58, 'Melbourne, Úc', '2025-04-05 13:00:00', '2025-04-05 15:00:00', 'Albert Park', 100000000, 1),
 (4, 'C24 – Abu Dhabi', 58, 'Yas Marina, UAE', '2024-12-08 17:00:00', '2024-12-08 19:00:00', 'Chặng cuối mùa giải 2024', 120000000, 2),
 (5, 'C01 – Bahrain', 57, 'Sakhir, Bahrain', '2026-03-08 15:00:00', '2026-03-08 17:00:00', 'Chặng mở màn mùa giải 2026', 100000000, 3),
 (6, 'C24 – Abu Dhabi', 58, 'Yas Marina, UAE', '2026-12-06 17:00:00', '2026-12-06 19:00:00', 'Chặng cuối mùa giải 2026', 120000000, 3),
 (7, 'C01 – Bahrain', 57, 'Sakhir, Bahrain', '2027-03-07 15:00:00', '2027-03-07 17:00:00', 'Chặng mở màn mùa giải 2027', 100000000, 4)
ON CONFLICT DO NOTHING;

-- ===== Hóa đơn đã có (Max Verstappen chặng C03 đã được trả) =====
INSERT INTO tblRewardInvoice (id, barcode, paidAt, amount, paymentMethod, status, tblOrganizerMemberid) VALUES
 (1, 'HD001', '2025-04-06 10:00:00', 60000000, 'Chuyển khoản online', 'Thanh toán thành công', 2)
ON CONFLICT DO NOTHING;

-- ===== Kết quả thi đấu =====
-- reward của kết quả chưa trả được RaceDAO.getRaceInfo() tính lại theo hệ số xếp hạng
INSERT INTO tblRegisteredReceipt (id, rank, score, finishedTime, completedLap, reward, completedStatus, tblRaceid, tblRegisteredDriverid, tblOrganizerMemberid, tblRewardInvoiceid) VALUES
 -- C03 – Australian 2025
 (1, 1, 25, '2025-04-05 14:32:00', 58, 0, 'Hoàn thành', 3, 1, NULL, NULL),
 (2, 2, 18, '2025-04-05 14:32:05', 58, 0, 'Hoàn thành', 3, 2, NULL, NULL),
 (3, 3, 15, '2025-04-05 14:32:11', 58, 60000000, 'Hoàn thành', 3, 3, 2, 1),
 (4, 4, 12, '2025-04-05 14:32:20', 58, 0, 'Hoàn thành', 3, 4, NULL, NULL),
 (5, 5, 10, '2025-04-05 14:32:32', 58, 0, 'Hoàn thành', 3, 5, NULL, NULL),
 (6, 6, 8, '2025-04-05 14:32:40', 58, 0, 'Hoàn thành', 3, 6, NULL, NULL),
 (7, 7, 6, '2025-04-05 14:32:55', 58, 0, 'Hoàn thành', 3, 9, NULL, NULL),
 (8, 8, 4, '2025-04-05 14:33:10', 57, 0, 'Hoàn thành', 3, 8, NULL, NULL),
 (9, NULL, 0, NULL, 30, 0, 'Bỏ cuộc', 3, 7, NULL, NULL),
 -- C24 – Abu Dhabi 2024
 (10, 1, 25, '2024-12-08 18:30:00', 58, 0, 'Hoàn thành', 4, 3, NULL, NULL),
 (11, 2, 18, '2024-12-08 18:30:10', 58, 0, 'Hoàn thành', 4, 4, NULL, NULL),
 (12, 3, 15, '2024-12-08 18:30:20', 58, 0, 'Hoàn thành', 4, 5, NULL, NULL),
 (13, 4, 12, '2024-12-08 18:30:30', 58, 0, 'Hoàn thành', 4, 7, NULL, NULL),
 (14, NULL, 0, NULL, 12, 0, 'Tai nạn', 4, 6, NULL, NULL),
 -- C01 – Bahrain 2025: Leclerc và Norris đồng hạng 2
 (15, 1, 25, '2025-03-09 16:33:20', 57, 0, 'Hoàn thành', 1, 3, NULL, NULL),
 (16, 2, 18, '2025-03-09 16:33:30', 57, 0, 'Hoàn thành', 1, 5, NULL, NULL),
 (17, 2, 18, '2025-03-09 16:33:30', 57, 0, 'Hoàn thành', 1, 4, NULL, NULL),
 (18, 4, 12, '2025-03-09 16:33:50', 57, 0, 'Hoàn thành', 1, 6, NULL, NULL),
 (19, NULL, 0, NULL, 0, 0, 'Không xuất phát', 1, 9, NULL, NULL),
 -- C01 – Bahrain 2026
 (20, 1, 25, '2026-03-08 16:31:00', 57, 0, 'Hoàn thành', 5, 4, NULL, NULL),
 (21, 2, 18, '2026-03-08 16:31:04', 57, 0, 'Hoàn thành', 5, 7, NULL, NULL),
 (22, 3, 15, '2026-03-08 16:31:15', 57, 0, 'Hoàn thành', 5, 1, NULL, NULL),
 (23, 4, 12, '2026-03-08 16:31:22', 57, 0, 'Hoàn thành', 5, 2, NULL, NULL)
ON CONFLICT DO NOTHING;

-- ===== Điều khoản & quyền lợi =====
INSERT INTO tblTerm (id, title, details, punishment, type, status) VALUES
 (1, 'Thanh toán tài trợ theo chặng', 'Nhà tài trợ thanh toán giá trị tài trợ theo từng chặng đua', 'Phạt 0,05% mỗi ngày trên số tiền chậm thanh toán', 'Tài chính', 'Hoạt động'),
 (2, 'Cung cấp tư liệu quảng bá theo chặng', 'Nhà tài trợ cung cấp logo, hình ảnh, video quảng bá cho từng chặng', 'Ban tổ chức không chịu trách nhiệm nếu tư liệu gửi trễ', 'Quảng bá', 'Hoạt động'),
 (3, 'Điều chỉnh lịch thực hiện tài trợ theo chặng khi lịch đua thay đổi', 'Hai bên điều chỉnh lịch tài trợ khi FIA thay đổi lịch đua', 'Không áp dụng', 'Pháp lý', 'Hoạt động'),
 (4, 'Bảo mật thông tin hợp đồng', 'Hai bên không tiết lộ giá trị và nội dung hợp đồng cho bên thứ ba', 'Bồi thường thiệt hại theo quy định pháp luật', 'Pháp lý', 'Hoạt động')
ON CONFLICT DO NOTHING;

INSERT INTO tblBenefit (id, title, description, type, status) VALUES
 (1, 'Hiển thị logo nhà tài trợ theo chặng', 'Logo trên biển quảng cáo dọc đường đua', 'Quảng bá thương hiệu', 'Hoạt động'),
 (2, 'Cấp vé VIP theo chặng', 'Vé khu VIP Paddock Club', 'Sự kiện', 'Hoạt động'),
 (3, 'Giới thiệu nhà tài trợ tại sự kiện theo chặng', 'MC giới thiệu nhà tài trợ tại lễ khai mạc chặng', 'Hình ảnh – Truyền thông', 'Hoạt động'),
 (4, 'Logo trên thân xe', 'In logo nhà tài trợ trên thân xe đua', 'Quảng bá thương hiệu', 'Hoạt động')
ON CONFLICT DO NOTHING;

-- ===== Hợp đồng =====
INSERT INTO tblContract (id, barcode, createdDate, effectiveFrom, effectiveTo, value, sponsorRepresentative, organizerRepresentative, signedDate, status, tblSponsorMemberid, tblSeasonid, tblOrganizerMemberid) VALUES
 (1, 'C101', '2025-01-10', '2025-03-06', '2025-12-06', 500000000, 'Christian Horner', 'Nguyễn Văn A', '2025-01-15', 'Đã ký', 3, 1, 1),
 (2, 'C102', '2026-01-05', '2026-03-06', '2026-12-06', 300000000, 'Christian Horner', 'Nguyễn Thị B', '2026-01-08', 'Đã ký', 3, 3, 2),
 (3, 'C103', '2026-02-01', '2026-03-06', '2026-12-06', 200000000, 'Toto Wolff', 'Nguyễn Thị B', NULL, 'Chờ ký', 5, 3, 2)
ON CONFLICT DO NOTHING;

INSERT INTO tblContractTerm (id, notes, tblTermid, tblContractid) VALUES
 (1, 'Thanh toán trong vòng 7 ngày sau khi kết thúc chặng', 1, 1),
 (2, 'Gửi tư liệu trước ngày tổ chức chặng 14 ngày', 2, 1),
 (3, NULL, 1, 2),
 (4, NULL, 4, 3)
ON CONFLICT DO NOTHING;

INSERT INTO tblContractBenefit (id, value, unit, notes, tblBenefitid, tblContractid) VALUES
 (1, 2, 'vị trí', 'Hai bên sườn xe', 4, 1),
 (2, 20, 'vé', NULL, 2, 1),
 (3, 24, 'chặng', NULL, 1, 2),
 (4, 10, 'vé', NULL, 2, 3)
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
