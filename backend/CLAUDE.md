# Backend

## Stack
- Java 21, Spring Boot 3.5 (Web, Data JPA), Maven wrapper (`./mvnw`, Maven 3.10)
- PostgreSQL 16; truy cập **chỉ qua `EntityManager`** trong lớp DAO (không dùng Spring Data `JpaRepository`)
- **Lombok**: entity dùng `@Getter @Setter @NoArgsConstructor`, không viết getter/setter tay
- **jackson-datatype-hibernate6**: bean `Hibernate6Module` trong `F1Application`; danh sách lazy chưa được DAO nạp sẽ ra `null` trong JSON thay vì lỗi `LazyInitializationException`
- Test: JUnit 5 + `@SpringBootTest` (+ MockMvc), chạy trên DB thật, mỗi test `@Transactional` nên tự rollback

## CSDL (docker-compose, service `database`)
- `docker compose up -d database`: chạy container `f1-database` (postgres:16-alpine), dữ liệu lưu ở `./database` (đã gitignore)
- Kết nối từ máy: `jdbc:postgresql://localhost:5432/f1`; từ container backend: `jdbc:postgresql://database:5432/f1`. User `f1`, password `f1`; múi giờ `Asia/Ho_Chi_Minh`
- Cấu hình qua biến môi trường `DB_URL`, `DB_USER`, `DB_PASSWORD` (mặc định trỏ localhost)
- `schema.sql` (`CREATE TABLE IF NOT EXISTS`) và `data.sql` (id cố định + `ON CONFLICT DO NOTHING`, rồi `setval` sequence) chạy **mỗi lần khởi động**, trước khi Hibernate `validate`
- Vào psql: `docker exec -it f1-database psql -U f1 -d f1`
- Xóa sạch dữ liệu: `docker compose down && sudo rm -rf database/*`

## Lệnh
```bash
./mvnw spring-boot:run   # chạy, cổng 8080 (cần container database đang chạy)
./mvnw test              # 28 test, cần container database đang chạy
./mvnw package           # build target/app.jar
```
Docker: `Dockerfile` build bằng maven:3.9.9-eclipse-temurin-21 (`-DskipTests`, vì test cần DB) rồi chạy bằng eclipse-temurin:21-jre-alpine với `-Duser.timezone=Asia/Ho_Chi_Minh`.

## Cấu trúc
```
src/main/java/com/ngtukien/f1/
  F1Application.java   main + bean Hibernate6Module
  model/        17 entity (liệt kê bên dưới)
  dao/          11 lớp DAO (liệt kê bên dưới) + PaymentException
  controller/   9 @RestController (Organizer, Sponsor, Season, Term, Benefit, Contract, Race, Driver, RewardInvoice)
                + ApiError (định dạng lỗi { message }); controller chỉ gọi DAO và trả kết quả/lỗi
src/test/java/com/ngtukien/f1/
  dao/          *DAOTest: kịch bản chuẩn + ngoại lệ theo báo cáo
  controller/   ApiTest: JSON trả về và mã lỗi HTTP
src/main/resources/
  schema.sql    17 bảng (liệt kê bên dưới)
  data.sql      dữ liệu mẫu
```

---

## Entity (`model/`)
Kiểu trong thiết kế ↔ Java: `int`→`int`/`Integer`, `String`→`String`, `DateTime`→`LocalDate` (ngày) hoặc `LocalDateTime` (có giờ), `Timestamp`→`LocalDateTime`, `boolean`→`boolean`.

**Member** (lớp cha, `@Inheritance(strategy = JOINED)`)
- id: int, username: String, password: String

**Organizer** extends Member
- fullName: String, address: String, dateOfBirth: LocalDate, email: String, tel: String, role: String

**Sponsor** extends Member
- name: String, taxCode: String, field: String, address: String, email: String, tel: String, status: String
- listContract: List\<Contract\>

**Driver** extends Member
- fullName: String, dateOfBirth: LocalDate, nationality: String, racingNumber: int
- listBankAccount: List\<BankAccount\>

**Team** extends Member
- name: String, manufacturer: String, description: String
- listRegisteredDriver: List\<RegisteredDriver\>

**Season**
- id: int, name: String, year: int, startDate: LocalDate, endDate: LocalDate
- listRace: List\<Race\>

**Race**
- id: int, name: String, lapCount: int, location: String, startTime: LocalDateTime, endTime: LocalDateTime, description: String, basePrize: int
- season: Season, listRegisteredReceipt: List\<RegisteredReceipt\>

**Contract**
- id: int, barcode: String, createdDate: LocalDate, effectiveFrom: LocalDate, effectiveTo: LocalDate, value: int
- sponsorRepresentative: String, organizerRepresentative: String, signedDate: LocalDate, status: String
- sponsor: Sponsor, season: Season, organizer: Organizer
- listContractTerm: List\<ContractTerm\>, listContractBenefit: List\<ContractBenefit\>

**Term**
- id: int, title: String, details: String, punishment: String, type: String, status: String

**ContractTerm**
- id: int, term: Term, notes: String

**Benefit**
- id: int, title: String, description: String, type: String, status: String

**ContractBenefit**
- id: int, benefit: Benefit, value: int, unit: String, notes: String

**RegisteredDriver**
- id: int, driver: Driver, team: Team

**RegisteredReceipt** (kết quả của một tay đua tại một chặng)
- id: int, registeredDriver: RegisteredDriver, race: Race
- rank: int, score: int, finishedTime: LocalDateTime, completedLap: int, reward: int
- completedStatus: String
- **Không có cột paymentStatus**: `rewardInvoice != null` nghĩa là `Đã trả`, ngược lại là `Chưa trả` (dùng `@Transient getPaymentStatus()` để trả ra JSON)
- organizer: Organizer, rewardInvoice: RewardInvoice (null khi chưa trả)

**RewardInvoice**
- id: int, barcode: String, paidAt: LocalDateTime, amount: int, paymentMethod: String, status: String
- organizer: Organizer, listRegisteredReceipt: List\<RegisteredReceipt\>

**Bank**
- id: int, bankCode: String, name: String, status: String

**BankAccount**
- id: int, accountNumber: String, accountHolder: String, isDefault: boolean, status: String, bank: Bank

---

## Bảng (`schema.sql`)
Tên bảng và cột giữ nguyên chữ hoa/thường như dưới. Cấu hình `PhysicalNamingStrategyStandardImpl` để Hibernate không đổi sang snake_case.
Kiểu: `id` là `SERIAL PRIMARY KEY`; chuỗi là `VARCHAR(255)`; số là `INTEGER`; ngày là `DATE`; ngày giờ là `TIMESTAMP`; `isDefault` là `BOOLEAN`.

| Bảng | Cột | Khóa ngoại → bảng |
|---|---|---|
| tblMember | id, username (UNIQUE), password | |
| tblOrganizer | **tblMemberid** (PK), fullName, address, dateOfBirth DATE, email, tel, role | tblMemberid → tblMember |
| tblSponsor | **tblMemberid** (PK), name, taxCode (UNIQUE), field, address, email, tel, status | tblMemberid → tblMember |
| tblDriver | **tblMemberid** (PK), fullName, dateOfBirth DATE, nationality, racingNumber | tblMemberid → tblMember |
| tblTeam | **tblMemberid** (PK), name, manufacturer, description | tblMemberid → tblMember |
| tblSeason | id, name, year, startDate DATE, endDate DATE | |
| tblRace | id, name, lapCount, location, startTime TIMESTAMP, endTime TIMESTAMP, description, basePrize, tblSeasonid | tblSeasonid → tblSeason |
| tblContract | id, barcode, createdDate DATE, effectiveFrom DATE, effectiveTo DATE, value, sponsorRepresentative, organizerRepresentative, signedDate DATE (NULL khi chưa ký), status, tblSponsorMemberid, tblSeasonid, tblOrganizerMemberid | → tblSponsor, tblSeason, tblOrganizer |
| tblTerm | id, title, details, punishment, type, status | |
| tblContractTerm | id, notes, tblTermid, tblContractid | → tblTerm, tblContract |
| tblBenefit | id, title, description, type, status | |
| tblContractBenefit | id, value, unit, notes, tblBenefitid, tblContractid | → tblBenefit, tblContract |
| tblRegisteredDriver | id, tblTeamMemberid, tblDriverMemberid | → tblTeam, tblDriver |
| tblRegisteredReceipt | id, rank, score, finishedTime TIMESTAMP, completedLap, reward, completedStatus, tblRaceid, tblRegisteredDriverid, tblOrganizerMemberid (NULL), tblRewardInvoiceid (NULL) | → tblRace, tblRegisteredDriver, tblOrganizer, tblRewardInvoice |
| tblRewardInvoice | id, barcode, paidAt TIMESTAMP, amount, paymentMethod, status, tblOrganizerMemberid | → tblOrganizer |
| tblBank | id, bankCode, name, status | |
| tblBankAccount | id, accountNumber, accountHolder, isDefault BOOLEAN, status, tblDriverMemberid, tblBankid | → tblDriver, tblBank |

Ánh xạ JPA:
- Bảng con của Member: `@PrimaryKeyJoinColumn(name = "tblMemberid")`
- `@ManyToOne` (mặc định EAGER) dùng `@JoinColumn(name = "<cột khóa ngoại>")`
- Danh sách con `@OneToMany` (LAZY). DAO nạp đúng danh sách báo cáo ghi "kèm ..." bằng `Hibernate.initialize(...)`; danh sách không nạp ra `null` trong JSON (Hibernate6Module)
- Danh sách **không** có thuộc tính ngược ở lớp con (theo Class Diagram) dùng `@OneToMany @JoinColumn(name = "<FK>")` một chiều: `Contract.listContractTerm`, `Contract.listContractBenefit` (cascade ALL), `Driver.listBankAccount`
- `Sponsor.listContract` có `@SQLRestriction("status = 'Đã ký'")`, tức chỉ gồm hợp đồng đã ký
- `spring.jpa.hibernate.ddl-auto=validate`, `spring.jpa.open-in-view=false`
- Chống vòng lặp JSON: `@JsonIgnoreProperties("<thuộc tính trỏ ngược>")` ở cả hai phía quan hệ (vd: `Race.listRegisteredReceipt` bỏ `race`). `password` để `@JsonProperty(access = WRITE_ONLY)`
- `BankAccount.isDefault` là `Boolean` để Lombok sinh `getIsDefault()`, nên JSON có key `isDefault`

---

## Giá trị trạng thái (lưu chuỗi tiếng Việt)
| Cột | Giá trị |
|---|---|
| Sponsor.status | `Đang hợp tác`, `Ngừng hợp tác` |
| Contract.status | `Chờ ký`, `Đã ký` |
| RegisteredReceipt.completedStatus | `Hoàn thành`, `Bỏ cuộc`, `Tai nạn`, `Không xuất phát`, `Không được phân hạng`, `Bị loại` |
| (trạng thái trả thưởng, tính ra) | `Chưa trả` khi tblRewardInvoiceid NULL, `Đã trả` khi khác NULL |
| RewardInvoice.status | `Thanh toán thành công`, `Thanh toán thất bại` |
| RewardInvoice.paymentMethod | `Chuyển khoản online` |
| Term.status, Benefit.status, Bank.status, BankAccount.status | `Hoạt động`, `Ngừng hoạt động` |

---

## DAO (`dao/`)
`DAO` là lớp cha, chứa `@PersistenceContext protected EntityManager em`. Các DAO con đánh dấu `@Repository`; phương thức ghi đánh dấu `@Transactional`.

| Lớp | Kế thừa | Phương thức (input → output) | Ghi chú |
|---|---|---|---|
| MemberDAO | DAO | `checkLogin(Member) → Member` | so khớp username + password |
| OrganizerDAO | MemberDAO | `checkLogin(Member) → Organizer` | null nếu sai |
| SponsorDAO | MemberDAO | `searchByName(String sponsorName) → List<Sponsor>` | `LIKE %name%`, không phân biệt hoa thường |
| | | `getSponsorInfo(int sponsorId) → Sponsor` | kèm listContract chỉ gồm hợp đồng `Đã ký` (kèm season) |
| | | `createSponsor(Sponsor) → boolean` | false nếu thiếu thông tin, sai định dạng (MST 10/13 số, SĐT `0` + 9–10 số, email) hoặc trùng `taxCode`; tạo cả dòng tblMember với username = password = MST, status `Đang hợp tác` |
| ContractDAO | DAO | `createContract(Contract) → boolean` | gán createdDate = hôm nay, status = `Chờ ký`, barcode = `C` + (100 + id); lưu kèm listContractTerm, listContractBenefit; false nếu không hợp lệ hoặc trùng điều khoản/quyền lợi |
| | | `updateSignedStatus(int contractId) → Contract` | chỉ khi đang `Chờ ký`: signedDate = hôm nay, status = `Đã ký` |
| SeasonDAO | DAO | `searchByName(String seasonName) → List<Season>` | |
| | | `createSeason(Season) → boolean` | false nếu trùng name + year |
| | | `getSeasonInfo(int seasonId) → Season` | kèm listRace (`JOIN FETCH`) |
| TermDAO | DAO | `searchByTitle(String termTitle) → List<Term>` | |
| | | `createTerm(Term) → boolean` | false nếu trùng title |
| BenefitDAO | DAO | `searchByTitle(String benefitTitle) → List<Benefit>` | |
| | | `createBenefit(Benefit) → boolean` | false nếu trùng title |
| RaceDAO | DAO | `getRaceInfo(int raceId) → Race` | kèm listRegisteredReceipt → registeredDriver → driver, team; kết quả có tblRewardInvoiceid khác NULL là đã trả |
| DriverDAO | MemberDAO | `getDriverInfo(int driverId) → Driver` | kèm listBankAccount → bank |
| RewardInvoiceDAO | DAO | `createRewardInvoice(RewardInvoice) → RewardInvoice` | **một giao dịch**: kiểm tra mọi kết quả trong listRegisteredReceipt còn `Chưa trả`, reward > 0, tay đua có tài khoản mặc định còn hoạt động; gán paidAt = now, barcode = `HD%03d`, amount = tổng reward, status = `Thanh toán thành công`; lưu hóa đơn rồi gán tblRewardInvoiceid + organizer cho từng kết quả. Không thỏa thì ném `PaymentException` và rollback toàn bộ |

- Không có `RegisteredReceiptDAO`, và Sponsor/Driver không có `checkLogin` (báo cáo tổng hợp không có).
- DAO **không** được ném `IllegalStateException`/`IllegalArgumentException` cho lỗi nghiệp vụ, vì `@Repository` sẽ bọc chúng thành `InvalidDataAccessApiUsageException` (lỗi 500). Dùng exception riêng như `PaymentException`.
- Tính hệ số xếp hạng nằm trong `RaceDAO.calculateReward` (static, có test riêng).

---

## Quy tắc tính thưởng
`reward = race.basePrize × hệ số`, làm tròn xuống về `int` (VNĐ).

| Hạng | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | ≥11 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Điểm | 25 | 18 | 15 | 12 | 10 | 8 | 6 | 4 | 2 | 1 | 0 |
| Hệ số | 1.00 | 0.72 | 0.60 | 0.48 | 0.40 | 0.32 | 0.24 | 0.16 | 0.08 | 0.04 | 0 |

- **Đồng hạng:** cộng hệ số của các vị trí bị chiếm rồi chia đều. Ví dụ hai tay đua cùng hạng 2 thì mỗi người được (0.72 + 0.60) / 2 = 0.66.
- `completedStatus` khác `Hoàn thành` thì hệ số = 0.
- Frontend không được sửa số tiền.

---

## API (REST, JSON, tiền tố `/api`)
| Method | Path | Body / Query | DAO | Lỗi |
|---|---|---|---|---|
| POST | /api/organizers/login | `{username, password}` | OrganizerDAO.checkLogin | 401 "Tên đăng nhập hoặc mật khẩu không chính xác" |
| GET | /api/sponsors | `?name=` | SponsorDAO.searchByName | (trả [] khi không có) |
| POST | /api/sponsors | Sponsor | SponsorDAO.createSponsor | 409 "Thông tin nhà tài trợ không hợp lệ hoặc mã số thuế đã tồn tại" |
| GET | /api/sponsors/{id} | | SponsorDAO.getSponsorInfo | 404 |
| GET | /api/seasons | `?name=` | SeasonDAO.searchByName | |
| POST | /api/seasons | Season | SeasonDAO.createSeason | 409 "Thông tin mùa giải không hợp lệ hoặc mùa giải đã tồn tại" |
| GET | /api/seasons/{id} | | SeasonDAO.getSeasonInfo | 404 |
| GET | /api/terms | `?title=` | TermDAO.searchByTitle | |
| POST | /api/terms | Term | TermDAO.createTerm | 409 "Thông tin điều khoản không hợp lệ hoặc điều khoản đã tồn tại" |
| GET | /api/benefits | `?title=` | BenefitDAO.searchByTitle | |
| POST | /api/benefits | Benefit | BenefitDAO.createBenefit | 409 "Thông tin quyền lợi không hợp lệ hoặc quyền lợi đã tồn tại" |
| POST | /api/contracts | Contract (kèm listContractTerm, listContractBenefit) | ContractDAO.createContract | 400 "Thông tin hợp đồng không hợp lệ" |
| PUT | /api/contracts/{id}/signed | | ContractDAO.updateSignedStatus | 409 "Hợp đồng không tồn tại hoặc không ở trạng thái Chờ ký" |
| GET | /api/races/{id} | | RaceDAO.getRaceInfo | 404 |
| GET | /api/drivers/{id} | | DriverDAO.getDriverInfo | 404 |
| POST | /api/invoices | `{organizer:{id}, paymentMethod, listRegisteredReceipt:[{id}, ...]}` | RewardInvoiceDAO.createRewardInvoice | 409 "Thanh toán không thành công: <lý do>" (vd: "Thiếu thông tin nhận thưởng: Lê Minh Cường") |

- Thành công: trả entity/list trực tiếp (POST tạo mới trả 201 kèm bản ghi có id). Lỗi: `{ "message": "<tiếng Việt>" }`.
- Hợp đồng hợp lệ khi: có sponsor và season, value > 0, effectiveFrom < effectiveTo, có cả hai người đại diện.

## Dữ liệu mẫu (`data.sql`)
- Organizer `a` / `a@123` (Nguyễn Văn A) và `b` / `b@123` (Nguyễn Thị B)
- Sponsor (id 3–7): Red Bull, **Red Blue** (MST 0123456789, "Truyền thông và quảng cáo", 18 Nguyễn Trãi, Hà Nội, chưa có hợp đồng), Mercedes-AMG Petronas (chỉ có hợp đồng `Chờ ký`), Oracle (`Ngừng hợp tác`), Heineken. Mã hiển thị = `S` + id 3 chữ số
- Season: F1 FIA 2025 (id 1), F1 FIA 2024 (id 2), **F1 FIA 2026** (id 3, cho kịch bản ký hợp đồng), F1 FIA 2027 (id 4, chưa có chặng nào kết thúc)
- Race (tên dạng "C03 – Australian"): 1 C01 – Bahrain 2025 (**đồng hạng 2**: Leclerc, Norris; Russell `Không xuất phát`), 2 C02 – Saudi Arabia 2025 (**chưa có basePrize, chưa có kết quả**), 3 **C03 – Australian 2025** (100.000.000; Max Verstappen **đã trả** qua hóa đơn HD001; Piastri `Bỏ cuộc`), 4 **C24 – Abu Dhabi 2024** (120.000.000; Norris hạng 2 → 86.400.000), 5 C01 – Bahrain 2026, 6–7 chưa diễn ra
- Tay đua **Lê Minh Cường** (#27) không có tài khoản mặc định, dùng để thử ngoại lệ "Thiếu thông tin nhận thưởng"
- 4 Term, 4 Benefit đúng tên trong kịch bản ("Thanh toán tài trợ theo chặng", "Cấp vé VIP theo chặng"...); 3 hợp đồng mẫu C101–C103

## Không được làm
- Không thêm tầng service hay Spring Data repository.
- Không viết getter/setter tay cho entity (dùng Lombok); không tự viết bộ lọc lazy cho Jackson (đã có Hibernate6Module).
- Test không được để lại dữ liệu trong DB: luôn `@Transactional` trên lớp test.
- Không đổi tên bảng, cột, lớp, phương thức so với các bảng ở trên.
- Không thêm endpoint ngoài bảng API khi chưa hỏi.

## Điểm báo cáo chưa nói (đã chọn tạm)
1. **Chặng "đã kết thúc":** dùng `endTime < now()`.
2. **Thanh toán ngân hàng là giả lập:** thành công nếu mọi tay đua có tài khoản mặc định còn hoạt động. Không gửi email hóa đơn thật.
