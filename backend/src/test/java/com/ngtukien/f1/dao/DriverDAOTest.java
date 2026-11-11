package com.ngtukien.f1.dao;

import com.ngtukien.f1.exception.DriverException;
import com.ngtukien.f1.model.Bank;
import com.ngtukien.f1.model.BankAccount;
import com.ngtukien.f1.model.Driver;
import com.ngtukien.f1.model.Member;
import com.ngtukien.f1.model.RegisteredReceipt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DriverDAOTest {
    @Autowired
    DriverDAO driverDAO;

    @Test
    void getDriverInfoSuccess() {
        Driver driver = driverDAO.getDriverInfo(3); // Lando Norris (tài khoản c)
        assertNotNull(driver);
        assertEquals("Lando Norris", driver.getFullName());
        assertFalse(driver.getListBankAccount().isEmpty());
    }

    @Test
    void getRewardsByDriverSuccess() {
        // Lando Norris (id 3)
        List<RegisteredReceipt> rewards = driverDAO.getRewardsByDriver(3);
        assertNotNull(rewards);
        assertFalse(rewards.isEmpty());
        for (RegisteredReceipt r : rewards) {
            assertEquals("Lando Norris", r.getRegisteredDriver().getDriver().getFullName());
            assertNotNull(r.getRace());
            assertNotNull(r.getPaymentStatus());
        }
    }

    @Test
    void checkLoginDriverSuccess() {
        Member m = new Member();
        m.setUsername("c");
        m.setPassword("c@123");
        Driver d = driverDAO.checkLogin(m);
        assertNotNull(d);
        assertEquals("Lando Norris", d.getFullName());
        assertEquals(1, d.getRacingNumber());
    }

    @Test
    void checkLoginWrongPasswordReturnsNull() {
        Member m = new Member();
        m.setUsername("c");
        m.setPassword("wrong");
        assertNull(driverDAO.checkLogin(m));
    }

    @Test
    void checkLoginOrganizerAccountReturnsNull() {
        Member m = new Member();
        m.setUsername("a");
        m.setPassword("a@123");
        assertNull(driverDAO.checkLogin(m));
    }

    private static BankAccount input(int bankId, String number, boolean isDefault) {
        BankAccount a = new BankAccount();
        Bank b = new Bank();
        b.setId(bankId);
        a.setBank(b);
        a.setAccountNumber(number);
        a.setAccountHolder("Lando Norris");
        a.setIsDefault(isDefault);
        return a;
    }

    private static long defaultCount(Driver d) {
        return d.getListBankAccount().stream().filter(a -> Boolean.TRUE.equals(a.getIsDefault())).count();
    }

    @Test
    void addBankAccountAsDefaultMovesDefault() {
        Driver d = driverDAO.addBankAccount(3, input(5, "99887766", true));
        assertEquals(1, defaultCount(d));
        BankAccount added = d.getListBankAccount().stream()
                .filter(a -> "99887766".equals(a.getAccountNumber())).findFirst().orElseThrow();
        assertTrue(added.getIsDefault());
        assertEquals("LANDO NORRIS", added.getAccountHolder());
    }

    @Test
    void addFirstBankAccountBecomesDefault() {
        // Isack Hadjar (48) chưa có tài khoản nào
        Driver d = driverDAO.addBankAccount(48, input(1, "30303030", false));
        assertEquals(1, d.getListBankAccount().size());
        assertTrue(d.getListBankAccount().get(0).getIsDefault());
    }

    @Test
    void addBankAccountInvalidOrDuplicateRejected() {
        assertThrows(DriverException.class, () -> driverDAO.addBankAccount(3, input(1, "12ab", false)));
        assertThrows(DriverException.class, () -> driverDAO.addBankAccount(3, input(1, "011000001", false)));
    }

    /** Tài khoản mặc định / không mặc định hiện tại của Lando Norris (người dùng có thể đã đổi trên giao diện). */
    private BankAccount accountOf(int driverId, boolean isDefault) {
        return driverDAO.getDriverInfo(driverId).getListBankAccount().stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()) == isDefault)
                .findFirst().orElseThrow();
    }

    @Test
    void updateSetsNewDefaultAndCannotUnsetDefault() {
        BankAccount other = accountOf(3, false);
        Driver d = driverDAO.updateBankAccount(3, other.getId(), input(other.getBank().getId(), other.getAccountNumber(), true));
        assertEquals(1, defaultCount(d));
        assertTrue(d.getListBankAccount().stream().anyMatch(a -> a.getId().equals(other.getId()) && a.getIsDefault()));
        assertThrows(DriverException.class,
                () -> driverDAO.updateBankAccount(3, other.getId(), input(other.getBank().getId(), other.getAccountNumber(), false)));
    }

    @Test
    void deleteBankAccountRules() {
        BankAccount def = accountOf(3, true);
        BankAccount other = accountOf(3, false);
        assertThrows(DriverException.class, () -> driverDAO.deleteBankAccount(3, def.getId()));
        Driver d = driverDAO.deleteBankAccount(3, other.getId());
        assertTrue(d.getListBankAccount().stream().noneMatch(a -> a.getId().equals(other.getId())));
        assertNull(driverDAO.deleteBankAccount(30, def.getId())); // không phải tài khoản của Verstappen (30)
    }

    @Test
    void updateDriverInfoKeepsLockedFields() {
        Driver input = new Driver();
        input.setFullName(" Lando Norris Jr. ");
        input.setNationality("Việt Nam");
        input.setDateOfBirth(LocalDate.of(2000, 1, 2));
        input.setRacingNumber(99);
        input.setUsername("hacked");
        Driver d = driverDAO.updateDriverInfo(3, input);
        assertEquals("Lando Norris Jr.", d.getFullName());
        assertEquals(1, d.getRacingNumber());
        assertEquals("c", d.getUsername());

        input.setDateOfBirth(LocalDate.now().minusYears(5));
        assertThrows(DriverException.class, () -> driverDAO.updateDriverInfo(3, input));
    }
}
