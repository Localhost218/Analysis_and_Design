-- Báo cáo tổng hợp, mục 8: Thiết kế CSDL. Chạy mỗi lần khởi động nên dùng IF NOT EXISTS.

CREATE TABLE IF NOT EXISTS tblMember (
    id       SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS tblOrganizer (
    tblMemberid INTEGER PRIMARY KEY REFERENCES tblMember (id),
    fullName    VARCHAR(255),
    address     VARCHAR(255),
    dateOfBirth DATE,
    email       VARCHAR(255),
    tel         VARCHAR(255),
    role        VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tblSponsor (
    tblMemberid INTEGER PRIMARY KEY REFERENCES tblMember (id),
    name        VARCHAR(255) NOT NULL,
    taxCode     VARCHAR(255) NOT NULL UNIQUE,
    field       VARCHAR(255),
    address     VARCHAR(255),
    email       VARCHAR(255),
    tel         VARCHAR(255),
    status      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tblDriver (
    tblMemberid  INTEGER PRIMARY KEY REFERENCES tblMember (id),
    fullName     VARCHAR(255),
    dateOfBirth  DATE,
    nationality  VARCHAR(255),
    racingNumber INTEGER
);

CREATE TABLE IF NOT EXISTS tblTeam (
    tblMemberid  INTEGER PRIMARY KEY REFERENCES tblMember (id),
    name         VARCHAR(255),
    manufacturer VARCHAR(255),
    description  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tblSeason (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    year      INTEGER,
    startDate DATE,
    endDate   DATE
);

CREATE TABLE IF NOT EXISTS tblRace (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255),
    lapCount    INTEGER,
    location    VARCHAR(255),
    startTime   TIMESTAMP,
    endTime     TIMESTAMP,
    description VARCHAR(255),
    basePrize   INTEGER,
    tblSeasonid INTEGER NOT NULL REFERENCES tblSeason (id)
);

CREATE TABLE IF NOT EXISTS tblContract (
    id                      SERIAL PRIMARY KEY,
    barcode                 VARCHAR(255),
    createdDate             DATE,
    effectiveFrom           DATE,
    effectiveTo             DATE,
    value                   INTEGER,
    sponsorRepresentative   VARCHAR(255),
    organizerRepresentative VARCHAR(255),
    signedDate              DATE,
    status                  VARCHAR(255),
    tblSponsorMemberid      INTEGER NOT NULL REFERENCES tblSponsor (tblMemberid),
    tblSeasonid             INTEGER NOT NULL REFERENCES tblSeason (id),
    tblOrganizerMemberid    INTEGER NOT NULL REFERENCES tblOrganizer (tblMemberid)
);

CREATE TABLE IF NOT EXISTS tblTerm (
    id         SERIAL PRIMARY KEY,
    title      VARCHAR(255) NOT NULL,
    details    VARCHAR(255),
    punishment VARCHAR(255),
    type       VARCHAR(255),
    status     VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tblContractTerm (
    id            SERIAL PRIMARY KEY,
    notes         VARCHAR(255),
    tblTermid     INTEGER NOT NULL REFERENCES tblTerm (id),
    tblContractid INTEGER NOT NULL REFERENCES tblContract (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_contract_term
ON tblContractTerm (tblContractid, tblTermid);

CREATE TABLE IF NOT EXISTS tblBenefit (
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    type        VARCHAR(255),
    status      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tblContractBenefit (
    id            SERIAL PRIMARY KEY,
    value         INTEGER,
    unit          VARCHAR(255),
    notes         VARCHAR(255),
    tblBenefitid  INTEGER NOT NULL REFERENCES tblBenefit (id),
    tblContractid INTEGER NOT NULL REFERENCES tblContract (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_contract_benefit
ON tblContractBenefit (tblContractid, tblBenefitid);

CREATE TABLE IF NOT EXISTS tblRegisteredDriver (
    id                SERIAL PRIMARY KEY,
    tblTeamMemberid   INTEGER NOT NULL REFERENCES tblTeam (tblMemberid),
    tblDriverMemberid INTEGER NOT NULL REFERENCES tblDriver (tblMemberid)
);

CREATE TABLE IF NOT EXISTS tblRewardInvoice (
    id                   SERIAL PRIMARY KEY,
    barcode              VARCHAR(255),
    paidAt               TIMESTAMP,
    amount               INTEGER,
    paymentMethod        VARCHAR(255),
    status               VARCHAR(255),
    tblOrganizerMemberid INTEGER NOT NULL REFERENCES tblOrganizer (tblMemberid)
);

CREATE TABLE IF NOT EXISTS tblRegisteredReceipt (
    id                    SERIAL PRIMARY KEY,
    rank                  INTEGER,
    score                 INTEGER,
    finishedTime          TIMESTAMP,
    completedLap          INTEGER,
    reward                INTEGER,
    completedStatus       VARCHAR(255),
    tblRaceid             INTEGER NOT NULL REFERENCES tblRace (id),
    tblRegisteredDriverid INTEGER NOT NULL REFERENCES tblRegisteredDriver (id),
    tblOrganizerMemberid  INTEGER REFERENCES tblOrganizer (tblMemberid),
    tblRewardInvoiceid    INTEGER REFERENCES tblRewardInvoice (id)
);

CREATE TABLE IF NOT EXISTS tblBank (
    id       SERIAL PRIMARY KEY,
    bankCode VARCHAR(255),
    name     VARCHAR(255),
    status   VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tblBankAccount (
    id                SERIAL PRIMARY KEY,
    accountNumber     VARCHAR(255),
    accountHolder     VARCHAR(255),
    isDefault         BOOLEAN NOT NULL DEFAULT FALSE,
    status            VARCHAR(255),
    tblDriverMemberid INTEGER NOT NULL REFERENCES tblDriver (tblMemberid),
    tblBankid         INTEGER NOT NULL REFERENCES tblBank (id)
);

-- Ràng buộc: Mỗi tay đua có thể có nhiều tài khoản, nhưng tối đa CHỈ 1 tài khoản là mặc định (isDefault = TRUE)
CREATE UNIQUE INDEX IF NOT EXISTS uq_driver_default_bank_account
ON tblBankAccount (tblDriverMemberid)
WHERE isDefault = TRUE;

