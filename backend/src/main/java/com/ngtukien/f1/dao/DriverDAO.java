package com.ngtukien.f1.dao;

import com.ngtukien.f1.exception.DriverException;
import com.ngtukien.f1.model.Bank;
import com.ngtukien.f1.model.BankAccount;
import com.ngtukien.f1.model.Driver;
import com.ngtukien.f1.model.Member;
import com.ngtukien.f1.model.Race;
import com.ngtukien.f1.model.RegisteredReceipt;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class DriverDAO extends MemberDAO {

    /**
     * Đăng nhập thành viên Tay đua.
     * Trả về Driver nếu đúng, null nếu sai hoặc tài khoản không phải Tay đua.
     */
    @Override
    public Driver checkLogin(Member member) {
        if (member == null || isBlank(member.getUsername()) || isBlank(member.getPassword())) {
            return null;
        }
        return em.createQuery("SELECT d FROM Driver d WHERE d.username = :u AND d.password = :p", Driver.class)
                .setParameter("u", member.getUsername().trim())
                .setParameter("p", member.getPassword())
                .getResultStream().findFirst().orElse(null);
    }

    /** Tay đua kèm danh sách tài khoản ngân hàng (listBankAccount). */
    public Driver getDriverInfo(int driverId) {
        Driver driver = em.find(Driver.class, driverId);
        if (driver != null) {
            Hibernate.initialize(driver.getListBankAccount());
        }
        return driver;
    }

    /** Danh sách kết quả thi đấu & tiền thưởng của tay đua qua các chặng/mùa giải. */
    public List<RegisteredReceipt> getRewardsByDriver(int driverId) {
        List<RegisteredReceipt> list = em.createQuery(
                "SELECT r FROM RegisteredReceipt r " +
                "JOIN FETCH r.race rc " +
                "JOIN FETCH rc.season s " +
                "JOIN FETCH r.registeredDriver rd " +
                "JOIN FETCH rd.driver d " +
                "LEFT JOIN FETCH rd.team t " +
                "LEFT JOIN FETCH r.rewardInvoice inv " +
                "WHERE d.id = :did " +
                "ORDER BY rc.startTime DESC, r.id DESC", RegisteredReceipt.class)
                .setParameter("did", driverId)
                .getResultList();

        for (RegisteredReceipt r : list) {
            if (r.getRewardInvoice() == null && (r.getReward() == null || r.getReward() == 0)) {
                Race race = r.getRace();
                if (race != null && race.getBasePrize() != null && race.getBasePrize() > 0) {
                    Hibernate.initialize(race.getListRegisteredReceipt());
                    RaceDAO.calculateReward(race.getBasePrize(), race.getListRegisteredReceipt());
                }
            }
        }
        return list;
    }

    /**
     * Tay đua cập nhật thông tin cá nhân của chính mình: họ tên, ngày sinh, quốc tịch.
     * Không đổi tên đăng nhập, mật khẩu, số xe. Trả về null nếu không có tay đua.
     */
    @Transactional
    public Driver updateDriverInfo(int driverId, Driver input) {
        Driver driver = em.find(Driver.class, driverId);
        if (driver == null) {
            return null;
        }
        if (isBlank(input.getFullName())) {
            throw new DriverException("Vui lòng nhập họ và tên");
        }
        if (isBlank(input.getNationality())) {
            throw new DriverException("Vui lòng nhập quốc tịch");
        }
        LocalDate dob = input.getDateOfBirth();
        if (dob == null || dob.isAfter(LocalDate.now().minusYears(16))) {
            throw new DriverException("Ngày sinh không hợp lệ (tay đua phải từ 16 tuổi)");
        }
        driver.setFullName(input.getFullName().trim());
        driver.setNationality(input.getNationality().trim());
        driver.setDateOfBirth(dob);
        em.flush();
        Hibernate.initialize(driver.getListBankAccount());
        return driver;
    }

    /** Danh sách ngân hàng đang hoạt động (cho ô chọn ngân hàng). */
    public List<Bank> getAllBanks() {
        return em.createQuery("SELECT b FROM Bank b WHERE b.status = :s ORDER BY b.name", Bank.class)
                .setParameter("s", BankAccount.ACTIVE)
                .getResultList();
    }

    /**
     * Tay đua thêm tài khoản ngân hàng. Nếu tay đua chưa có tài khoản mặc định thì tài khoản mới là mặc định.
     * Trả về tay đua kèm listBankAccount mới, null nếu không có tay đua.
     */
    @Transactional
    public Driver addBankAccount(int driverId, BankAccount input) {
        Driver driver = em.find(Driver.class, driverId);
        if (driver == null) {
            return null;
        }
        BankAccount account = new BankAccount();
        applyInput(driver, account, input);
        account.setStatus(BankAccount.ACTIVE);
        account.setIsDefault(false);
        driver.getListBankAccount().add(account);
        em.persist(account);
        if (Boolean.TRUE.equals(input.getIsDefault()) || !hasDefault(driver)) {
            setDefault(driver, account);
        }
        em.flush();
        return driver;
    }

    /**
     * Tay đua sửa tài khoản ngân hàng của mình. Không được bỏ mặc định của tài khoản mặc định
     * (phải chọn tài khoản khác làm mặc định). Trả về null nếu không có tay đua hoặc tài khoản.
     */
    @Transactional
    public Driver updateBankAccount(int driverId, int accountId, BankAccount input) {
        Driver driver = em.find(Driver.class, driverId);
        BankAccount account = findAccount(driver, accountId);
        if (account == null) {
            return null;
        }
        boolean isDefault = Boolean.TRUE.equals(account.getIsDefault());
        if (isDefault && !Boolean.TRUE.equals(input.getIsDefault())) {
            throw new DriverException("Tay đua phải luôn có 1 tài khoản mặc định. Hãy đặt tài khoản khác làm mặc định trước.");
        }
        applyInput(driver, account, input);
        if (!isDefault && (Boolean.TRUE.equals(input.getIsDefault()) || !hasDefault(driver))) {
            setDefault(driver, account);
        }
        em.flush();
        return driver;
    }

    /**
     * Tay đua xóa tài khoản ngân hàng của mình. Không được xóa tài khoản mặc định.
     * Trả về null nếu không có tay đua hoặc tài khoản.
     */
    @Transactional
    public Driver deleteBankAccount(int driverId, int accountId) {
        Driver driver = em.find(Driver.class, driverId);
        BankAccount account = findAccount(driver, accountId);
        if (account == null) {
            return null;
        }
        if (Boolean.TRUE.equals(account.getIsDefault())) {
            throw new DriverException("Không thể xóa tài khoản mặc định. Hãy đặt tài khoản khác làm mặc định trước khi xóa.");
        }
        driver.getListBankAccount().remove(account);
        em.remove(account);
        em.flush();
        return driver;
    }

    private static BankAccount findAccount(Driver driver, int accountId) {
        if (driver == null) {
            return null;
        }
        return driver.getListBankAccount().stream()
                .filter(a -> a.getId() == accountId)
                .findFirst().orElse(null);
    }

    private static boolean hasDefault(Driver driver) {
        return driver.getListBankAccount().stream().anyMatch(a -> Boolean.TRUE.equals(a.getIsDefault()));
    }

    /** Đặt account làm mặc định duy nhất; bỏ mặc định cũ và flush trước vì CSDL chỉ cho 1 tài khoản mặc định. */
    private void setDefault(Driver driver, BankAccount account) {
        driver.getListBankAccount().forEach(a -> a.setIsDefault(false));
        em.flush();
        account.setIsDefault(true);
    }

    /** Kiểm tra và gán ngân hàng, số tài khoản, chủ tài khoản từ dữ liệu nhập. */
    private void applyInput(Driver driver, BankAccount account, BankAccount input) {
        Bank bank = input.getBank() == null || input.getBank().getId() == null
                ? null : em.find(Bank.class, input.getBank().getId());
        if (bank == null || !BankAccount.ACTIVE.equals(bank.getStatus())) {
            throw new DriverException("Vui lòng chọn ngân hàng");
        }
        String number = input.getAccountNumber() == null ? "" : input.getAccountNumber().trim();
        if (!number.matches("\\d{6,19}")) {
            throw new DriverException("Số tài khoản phải gồm 6–19 chữ số");
        }
        if (isBlank(input.getAccountHolder())) {
            throw new DriverException("Vui lòng nhập tên chủ tài khoản");
        }
        boolean duplicate = driver.getListBankAccount().stream()
                .anyMatch(a -> a != account && a.getBank().getId().equals(bank.getId()) && number.equals(a.getAccountNumber()));
        if (duplicate) {
            throw new DriverException("Tài khoản " + number + " tại " + bank.getName() + " đã tồn tại");
        }
        account.setBank(bank);
        account.setAccountNumber(number);
        account.setAccountHolder(input.getAccountHolder().trim().toUpperCase());
    }
}
