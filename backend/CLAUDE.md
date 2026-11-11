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
./mvnw test              # 63 test, cần container database đang chạy
./mvnw package           # build target/app.jar
```
Docker: `Dockerfile` build bằng maven:3.9.9-eclipse-temurin-21 (`-DskipTests`, vì test cần DB) rồi chạy bằng eclipse-temurin:21-jre-alpine với `-Duser.timezone=Asia/Ho_Chi_Minh`.

## Cấu trúc
```
src/main/java/com/ngtukien/f1/
  F1Application.java   main + bean Hibernate6Module
  model/        17 entity (liệt kê bên dưới)
  dao/          11 lớp DAO (liệt kê bên dưới)
  exception/    PaymentException, SponsorException, DriverException (ngoại lệ nghiệp vụ)
  controller/   10 @RestController (Organizer, Sponsor, Season, Term, Benefit, Contract, Race, Driver, RewardInvoice, Auth)
                + ApiError (định dạng lỗi { message }); controller chỉ gọi DAO và trả kết quả/lỗi
src/test/java/com/ngtukien/f1/
  dao/          *DAOTest: kịch bản chuẩn + ngoại lệ theo báo cáo
  controller/   ApiTest: JSON trả về và mã lỗi HTTP
src/main/resources/
  schema.sql    17 bảng (liệt kê bên dưới)
  data.sql      dữ liệu mẫu (sinh tự động, xem mục "Dữ liệu mẫu")
tools/f1-data/  script crawl formula1.com + sinh data.sql (không thuộc ứng dụng)
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
- organizer: Organizer (người đưa tay đua này vào/đăng ký kết quả thi đấu, **không phải người trả thưởng**; người trả thưởng lưu ở `RewardInvoice.organizer`), rewardInvoice: RewardInvoice (null khi chưa trả)

**RewardInvoice**
- id: int, barcode: String, paidAt: LocalDateTime, amount: int, paymentMethod: String, status: String
- organizer: Organizer (thành viên BTC thực hiện thanh toán hóa đơn), listRegisteredReceipt: List\<RegisteredReceipt\>

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
| tblRegisteredReceipt | id, rank, score, finishedTime TIMESTAMP, completedLap, reward, completedStatus, tblRaceid, tblRegisteredDriverid, tblOrganizerMemberid (người đưa tay đua vào), tblRewardInvoiceid (NULL khi chưa trả) | → tblRace, tblRegisteredDriver, tblOrganizer, tblRewardInvoice |
| tblRewardInvoice | id, barcode, paidAt TIMESTAMP, amount, paymentMethod, status, tblOrganizerMemberid (người thực hiện trả thưởng) | → tblOrganizer |
| tblBank | id, bankCode, name, status | |
| tblBankAccount | id, accountNumber, accountHolder, isDefault BOOLEAN, status, tblDriverMemberid, tblBankid | → tblDriver, tblBank |

Ánh xạ JPA:
- Bảng con của Member: `@PrimaryKeyJoinColumn(name = "tblMemberid")`
- `@ManyToOne` (mặc định EAGER) dùng `@JoinColumn(name = "<cột khóa ngoại>")`
- Danh sách con `@OneToMany` (LAZY). DAO nạp đúng danh sách báo cáo ghi "kèm ..." bằng `Hibernate.initialize(...)`; danh sách không nạp ra `null` trong JSON (Hibernate6Module)
- Danh sách **không** có thuộc tính ngược ở lớp con (theo Class Diagram) dùng `@OneToMany @JoinColumn(name = "<FK>")` một chiều: `Contract.listContractTerm`, `Contract.listContractBenefit` (cascade ALL), `Driver.listBankAccount` (`nullable = false` vì `tblDriverMemberid` NOT NULL, để Hibernate ghi FK ngay trong câu INSERT)
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
| RewardInvoice.paymentMethod | `Chuyển khoản online`, `Trả thưởng trực tiếp` |
| Term.status, Benefit.status, Bank.status, BankAccount.status | `Hoạt động`, `Ngừng hoạt động` |

---

## DAO (`dao/`)
`DAO` là lớp cha, chứa `@PersistenceContext protected EntityManager em`. Các DAO con đánh dấu `@Repository`; phương thức ghi đánh dấu `@Transactional`.

| Lớp | Kế thừa | Phương thức (input → output) | Ghi chú |
|---|---|---|---|
| MemberDAO | DAO | `checkLogin(Member) → Member` | so khớp username + password |
| | | `checkLoginUniversal(String identifier, String password) → Map<String, Object>` | hỗ trợ đăng nhập 4 vai trò: `ORGANIZER`, `SPONSOR` (qua username hoặc `taxCode`), `DRIVER`, `CUSTOMER` |
| OrganizerDAO | MemberDAO | `checkLogin(Member) → Organizer` | null nếu sai hoặc không phải Ban tổ chức |
| SponsorDAO | MemberDAO | `checkLogin(Member) → Sponsor` | null nếu sai; hỗ trợ đăng nhập bằng `username` hoặc `taxCode` |
| | | `searchByName(String sponsorName) → List<Sponsor>` | `LIKE %name%`, không phân biệt hoa thường |
| | | `getSponsorInfo(int sponsorId) → Sponsor` | kèm listContract chỉ gồm hợp đồng `Đã ký` (kèm season) |
| | | `createSponsor(Sponsor) → boolean` | false nếu thiếu thông tin, sai định dạng (MST 10/13 số, SĐT `0` + 9–10 số, email) hoặc trùng `taxCode`; tạo cả dòng tblMember với username = password = MST, status `Đang hợp tác` |
| | | `updateSponsor(Sponsor) → Sponsor` | cập nhật thông tin NTT; ném `SponsorException` nếu trùng MST với NTT khác hoặc không tìm thấy |
| | | `deleteSponsor(int sponsorId) → boolean` | xóa NTT; ném `SponsorException` nếu NTT đã có hợp đồng tài trợ ("Không thể xóa nhà tài trợ đã có hợp đồng tài trợ") |
| | | `getContractsBySponsor(int sponsorId) → List<Contract>` | trả về danh sách tất cả hợp đồng của NTT (gồm cả `Chờ ký` và `Đã ký`), kèm mùa giải, điều khoản, quyền lợi |
| ContractDAO | DAO | `createContract(Contract) → boolean` | gán createdDate = hôm nay, status = `Chờ ký`, barcode = `C` + (100 + id); lưu kèm listContractTerm, listContractBenefit; false nếu không hợp lệ hoặc trùng điều khoản/quyền lợi |
| | | `updateSignedStatus(int contractId) → Contract` | chỉ khi đang `Chờ ký`: signedDate = hôm nay, status = `Đã ký` |
| | | `getContractById(int contractId) → Contract` | lấy chi tiết hợp đồng kèm mùa giải, nhà tài trợ, các điều khoản (có hình phạt) và quyền lợi |
| SeasonDAO | DAO | `searchByName(String seasonName) → List<Season>` | |
| | | `createSeason(Season) → boolean` | false nếu trùng name + year |
| | | `getSeasonInfo(int seasonId) → Season` | kèm listRace (`JOIN FETCH`) |
| TermDAO | DAO | `searchByTitle(String termTitle) → List<Term>` | |
| | | `createTerm(Term) → boolean` | false nếu trùng title |
| BenefitDAO | DAO | `searchByTitle(String benefitTitle) → List<Benefit>` | |
| | | `createBenefit(Benefit) → boolean` | false nếu trùng title |
| RaceDAO | DAO | `getRaceInfo(int raceId) → Race` | kèm listRegisteredReceipt → registeredDriver → driver, team; kết quả có tblRewardInvoiceid khác NULL là đã trả |
| DriverDAO | MemberDAO | `checkLogin(Member) → Driver` | null nếu sai hoặc không phải Tay đua |
| | | `getDriverInfo(int driverId) → Driver` | kèm listBankAccount → bank |
| | | `getRewardsByDriver(int driverId) → List<RegisteredReceipt>` | danh sách toàn bộ kết quả thi đấu, số tiền thưởng và trạng thái trả thưởng của tay đua theo từng chặng, kèm `rewardInvoice` |
| | | `updateDriverInfo(int driverId, Driver) → Driver` | tay đua tự sửa fullName, dateOfBirth, nationality (bỏ qua username, password, racingNumber); ném `DriverException` nếu thiếu tên / quốc tịch hoặc dưới 16 tuổi; null nếu không có tay đua |
| | | `getAllBanks() → List<Bank>` | ngân hàng `Hoạt động`, sắp theo tên |
| | | `addBankAccount(int driverId, BankAccount) → Driver` | kiểm tra ngân hàng, số TK 6–19 chữ số, chủ TK (lưu in hoa), không trùng ngân hàng + số TK; status = `Hoạt động`; là mặc định nếu chọn hoặc tay đua chưa có tài khoản mặc định |
| | | `updateBankAccount(int driverId, int accountId, BankAccount) → Driver` | như trên; không cho bỏ mặc định của tài khoản mặc định; đặt mặc định thì bỏ mặc định cũ (flush trước vì unique index) |
| | | `deleteBankAccount(int driverId, int accountId) → Driver` | không cho xóa tài khoản mặc định. Ba hàm tài khoản trả null nếu tài khoản không thuộc tay đua đó |
| RewardInvoiceDAO | DAO | `createRewardInvoice(RewardInvoice) → RewardInvoice` | **một giao dịch**: kiểm tra mọi kết quả trong listRegisteredReceipt còn `Chưa trả`, reward > 0; nếu `paymentMethod = "Chuyển khoản online"` thì yêu cầu tay đua có tài khoản mặc định còn hoạt động (nếu `paymentMethod = "Trả thưởng trực tiếp"` thì bỏ qua kiểm tra tài khoản nhận thưởng); gán paidAt = now, barcode = `HD%03d`, amount = tổng reward, status = `Thanh toán thành công`; lưu hóa đơn rồi gán tblRewardInvoiceid + organizer cho từng kết quả. Không thỏa thì ném `PaymentException` và rollback toàn bộ |
| | | `getInvoiceById(int id) → RewardInvoice` | chi tiết hóa đơn kèm danh sách các khoản trả thưởng, tay đua và chặng đua |

- Không có `RegisteredReceiptDAO`.
- DAO **không** được ném `IllegalStateException`/`IllegalArgumentException` cho lỗi nghiệp vụ, vì `@Repository` sẽ bọc chúng thành `InvalidDataAccessApiUsageException` (lỗi 500). Dùng exception riêng trong package `exception/` như `PaymentException`, `SponsorException`, `DriverException`.
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
| POST | /api/auth/login | `{username, password}` | AuthController phối hợp các DAO | 401 "Tên đăng nhập hoặc mật khẩu không chính xác" (trả role: ORGANIZER, SPONSOR, DRIVER, CUSTOMER + thông tin user) |
| POST | /api/organizers/login | `{username, password}` | OrganizerDAO.checkLogin | 401 "Tên đăng nhập hoặc mật khẩu không chính xác" |
| POST | /api/sponsors/login | `{username, password}` | SponsorDAO.checkLogin | 401 "Tên đăng nhập hoặc mật khẩu không chính xác" (hỗ trợ username hoặc taxCode) |
| POST | /api/drivers/login | `{username, password}` | DriverDAO.checkLogin | 401 "Tên đăng nhập hoặc mật khẩu không chính xác" |
| GET | /api/sponsors | `?name=` | SponsorDAO.searchByName | (trả [] khi không có) |
| POST | /api/sponsors | Sponsor | SponsorDAO.createSponsor | 409 "Thông tin nhà tài trợ không hợp lệ hoặc mã số thuế đã tồn tại" |
| GET | /api/sponsors/{id} | | SponsorDAO.getSponsorInfo | 404 |
| PUT | /api/sponsors/{id} | Sponsor | SponsorDAO.updateSponsor | 400 "Thông tin không hợp lệ" / 409 "Mã số thuế đã tồn tại" |
| DELETE | /api/sponsors/{id} | | SponsorDAO.deleteSponsor | 400 "Không thể xóa nhà tài trợ đã có hợp đồng tài trợ" / 404 |
| GET | /api/sponsors/{id}/contracts | | SponsorDAO.getContractsBySponsor | 404 (trả danh sách tất cả hợp đồng: cả Chờ ký và Đã ký) |
| GET | /api/seasons | `?name=` | SeasonDAO.searchByName | |
| POST | /api/seasons | Season | SeasonDAO.createSeason | 409 "Thông tin mùa giải không hợp lệ hoặc mùa giải đã tồn tại" |
| GET | /api/seasons/{id} | | SeasonDAO.getSeasonInfo | 404 |
| GET | /api/terms | `?title=` | TermDAO.searchByTitle | |
| POST | /api/terms | Term | TermDAO.createTerm | 409 "Thông tin điều khoản không hợp lệ hoặc điều khoản đã tồn tại" |
| GET | /api/benefits | `?title=` | BenefitDAO.searchByTitle | |
| POST | /api/benefits | Benefit | BenefitDAO.createBenefit | 409 "Thông tin quyền lợi không hợp lệ hoặc quyền lợi đã tồn tại" |
| POST | /api/contracts | Contract (kèm listContractTerm, listContractBenefit) | ContractDAO.createContract | 400 "Thông tin hợp đồng không hợp lệ" |
| PUT | /api/contracts/{id}/signed | | ContractDAO.updateSignedStatus | 409 "Hợp đồng không tồn tại hoặc không ở trạng thái Chờ ký" |
| GET | /api/contracts/{id} | | ContractDAO.getContractById | 404 (chi tiết hợp đồng kèm terms & benefits) |
| GET | /api/races/{id} | | RaceDAO.getRaceInfo | 404 |
| GET | /api/drivers/{id} | | DriverDAO.getDriverInfo | 404 |
| GET | /api/drivers/{id}/rewards | | DriverDAO.getRewardsByDriver | 404 (danh sách kết quả & tiền thưởng của tay đua) |
| PUT | /api/drivers/{id} | `{fullName, dateOfBirth, nationality}` | DriverDAO.updateDriverInfo | 400 `DriverException`, 404 |
| GET | /api/drivers/banks | | DriverDAO.getAllBanks | |
| POST | /api/drivers/{id}/bank-accounts | `{bank:{id}, accountNumber, accountHolder, isDefault}` | DriverDAO.addBankAccount | 400 `DriverException`, 404 (trả Driver kèm listBankAccount) |
| PUT | /api/drivers/{id}/bank-accounts/{accountId} | như trên | DriverDAO.updateBankAccount | 400, 404 |
| DELETE | /api/drivers/{id}/bank-accounts/{accountId} | | DriverDAO.deleteBankAccount | 400, 404 |
| POST | /api/invoices | `{organizer:{id}, paymentMethod, listRegisteredReceipt:[{id}, ...]}` | RewardInvoiceDAO.createRewardInvoice | 409 "Thanh toán không thành công: <lý do>" (hỗ trợ online và trực tiếp) |
| GET | /api/invoices/{id} | | RewardInvoiceDAO.getInvoiceById | 404 (chi tiết hóa đơn kèm receipts) |

- Thành công: trả entity/list trực tiếp (POST tạo mới trả 201 kèm bản ghi có id). Lỗi: `{ "message": "<tiếng Việt>" }`.
- Hợp đồng hợp lệ khi: có sponsor đang hợp tác (`status = 'Đang hợp tác'`), season, organizer, `value > 0`, `effectiveFrom < effectiveTo`, có cả hai người đại diện, có ít nhất một điều khoản (`listContractTerm` không rỗng), có ít nhất một quyền lợi (`listContractBenefit` không rỗng), không trùng lặp điều khoản hay quyền lợi.
- Xem chi tiết bảng ma trận kịch bản ngoại lệ đầy đủ tại `CLAUDE.md` ở thư mục gốc.

## Dữ liệu mẫu (`data.sql`)
**Sinh tự động** bằng `tools/f1-data/gen_data.py` (xem README ở đó), không sửa tay: sửa script rồi sinh lại. Id cố định + `ON CONFLICT DO NOTHING` nên sinh lại xong phải làm trống CSDL rồi khởi động lại backend. Test chạy trên CSDL thật nên không giả định trạng thái người dùng có thể đổi trên giao diện (vd tài khoản mặc định), mà đọc lại từ DAO.

- **Thật (crawl formula1.com ngày 11/11/2026)**: mùa F1 FIA 2025 (id 1, 24 chặng), F1 FIA 2026 (id 2, 23 chặng); tên chặng dạng "C01 – Australia", số vòng, trường đua, giờ xuất phát **quy đổi sang giờ Việt Nam**, mô tả = tên chính thức của GP; kết quả (hạng, điểm, số vòng, thời gian về đích, trạng thái `Hoàn thành` / `Bỏ cuộc` / `Không xuất phát` / `Bị truất quyền` / `Không được xếp hạng`); 26 tay đua (họ tên, ngày sinh, quốc tịch, số xe mới nhất); 12 đội (Kick Sauber 2025 và Audi 2026 là 2 đội); tên 14 nhà tài trợ lấy từ danh sách đối tác F1. Tay đua đổi đội giữa chừng (Tsunoda, Lawson, Hadjar, Hulkenberg, Bortoleto) có nhiều dòng `tblRegisteredDriver`. Kết quả thật không có đồng hạng: trường hợp đồng hạng chỉ test bằng `RaceDAO.calculateReward`.
- **Giả lập**: tài khoản đăng nhập, mức thưởng cơ sở (100.000.000; Monaco, Abu Dhabi 150.000.000; Las Vegas 120.000.000), hóa đơn, tài khoản ngân hàng, MST / liên hệ NTT, hợp đồng, mùa 2027 (id 3, chưa có chặng, ngày dự kiến).
- **Tài khoản**: `a` Organizer Nguyễn Văn A (id 1, organizer duy nhất); `b` Sponsor **Heineken** (id 2, MST 0123456789); `c` Driver **Lando Norris** (id 3, số xe 1, 3 tài khoản ngân hàng); `d` Khách hàng (id 4). Thành viên khác: đội `team.<tên>`, tay đua `driver.<họ>` / `123456`, NTT `<tên>` / `<tên>@123` (id NTT 61–73).
- **Chặng đặc biệt (2026)**: 41 C17 Singapore (đã trả, trừ Lawson hạng 7); **42 C18 United States (chưa trả, dùng cho kịch bản trả thưởng)**; 43 C19 Mexico (**có kết quả, chưa có basePrize**); 44 C20 Brazil (**có basePrize, chưa nhập kết quả**); 45–47 Las Vegas, Qatar, Abu Dhabi chưa diễn ra. **35–40 C11 Hungary → C16 Bahrain (2026) chưa trả** (để thao tác trả thưởng); mọi chặng khác đã kết thúc đều đã trả.
- **Kết quả US 2026 (race 42)**: 854 Antonelli hạng 1, 855 Leclerc 2, 858 Norris 5, 859 **Isack Hadjar** 6 (**chưa có tài khoản ngân hàng**), 862 **Liam Lawson** 9 (**có tài khoản nhưng không có mặc định**), 863 **Pierre Gasly** 10 (**tài khoản mặc định bị `Tạm khóa`**), 864 Hulkenberg 11 (thưởng 0), 873 Bottas `Bỏ cuộc`.
- **Hóa đơn** HD001–HD053, HD065, HD066 (đã bỏ HD054–HD064 của C11–C16 2026): mỗi chặng một hóa đơn `Chuyển khoản online` + một hóa đơn `Trả thưởng trực tiếp` cho Hadjar / Lawson nếu có thưởng; Las Vegas + Qatar + Abu Dhabi 2025 gộp chung một hóa đơn.
- **Ngân hàng**: 6 ngân hàng `Hoạt động` + SCB (id 7) `Ngừng hoạt động` (Russell có tài khoản phụ ở đây).
- **Nhà tài trợ / hợp đồng** (barcode `C` + (100 + id)): Heineken (b) Đã ký 2025, 2026 + Chờ ký 2027; Aramco (61) Đã ký 2025, 2026; AWS (62) chưa có HĐ (xóa được); Lenovo (64), Crypto.com (71) chỉ có Chờ ký; Qatar Airways (68) chỉ có HĐ 2025 đã hết hạn; **Salesforce (69) `Ngừng hợp tác`** có HĐ cũ (không ký mới, không xóa được); Pirelli, DHL, LVMH có HĐ 2026.
- **Term / Benefit**: 5 + 5 đang hoạt động; term 6 "Độc quyền ngành hàng", benefit 6 "Quảng cáo trên vé giấy" `Ngừng hoạt động` (không hiện trong gợi ý, vẫn hiện trong HĐ Salesforce).

## Không được làm
- Không thêm tầng service hay Spring Data repository.
- Không viết getter/setter tay cho entity (dùng Lombok); không tự viết bộ lọc lazy cho Jackson (đã có Hibernate6Module).
- Test không được để lại dữ liệu trong DB: luôn `@Transactional` trên lớp test.
- Không đổi tên bảng, cột, lớp, phương thức so với các bảng ở trên.
- Không thêm endpoint ngoài bảng API khi chưa hỏi.

## Điểm báo cáo chưa nói (đã chọn tạm)
1. **Chặng "đã kết thúc":** dùng `endTime < now()`.
2. **Thanh toán ngân hàng là giả lập:** thành công nếu mọi tay đua có tài khoản mặc định còn hoạt động. Không gửi email hóa đơn thật.
