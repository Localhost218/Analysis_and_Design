package com.ngtukien.f1.dao;

import com.ngtukien.f1.exception.PaymentException;
import com.ngtukien.f1.model.Organizer;
import com.ngtukien.f1.model.RegisteredReceipt;
import com.ngtukien.f1.model.RewardInvoice;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RewardInvoiceDAOTest {
    @Autowired
    RewardInvoiceDAO rewardInvoiceDAO;
    @Autowired
    RaceDAO raceDAO;
    @Autowired
    EntityManager em;

    // Dữ liệu thật: C18 – United States 2026 (race 42, chưa trả), C17 – Singapore 2026 (race 41, đã trả trừ Lawson)
    // Kết quả US 2026: 854 Antonelli (1), 855 Leclerc (2), 858 Lando Norris (5), 859 Isack Hadjar (6, chưa có TK),
    // 862 Liam Lawson (9, không có TK mặc định), 863 Pierre Gasly (10, TK mặc định bị khóa), 864 Hulkenberg (11)
    @BeforeEach
    void calculateRewards() {
        raceDAO.getRaceInfo(41);
        raceDAO.getRaceInfo(42);
    }

    private static RewardInvoice invoiceOf(Integer... receiptIds) {
        RewardInvoice inv = new RewardInvoice();
        Organizer o = new Organizer();
        o.setId(1);
        inv.setOrganizer(o);
        inv.setListRegisteredReceipt(Arrays.stream(receiptIds).map(id -> {
            RegisteredReceipt r = new RegisteredReceipt();
            r.setId(id);
            return r;
        }).toList());
        return inv;
    }

    @Test
    void payManyDrivers() {
        // Antonelli 100.000.000 + Leclerc 72.000.000 + Norris 40.000.000 = 212.000.000
        RewardInvoice inv = rewardInvoiceDAO.createRewardInvoice(invoiceOf(854, 855, 858));
        assertEquals(212_000_000, inv.getAmount());
        assertEquals(String.format("HD%03d", inv.getId()), inv.getBarcode());
        assertEquals("Thanh toán thành công", inv.getStatus());
        assertEquals("Chuyển khoản online", inv.getPaymentMethod());
        assertEquals("Đã trả", em.find(RegisteredReceipt.class, 854).getPaymentStatus());
        assertEquals("Đã trả", em.find(RegisteredReceipt.class, 858).getPaymentStatus());
    }

    @Test
    void alreadyPaid() {
        // Verstappen thắng Singapore 2026, đã trả
        PaymentException e = assertThrows(PaymentException.class,
                () -> rewardInvoiceDAO.createRewardInvoice(invoiceOf(832)));
        assertTrue(e.getMessage().contains("Max Verstappen"));
    }

    @Test
    void missingDefaultBankAccount() {
        // Lawson có 2 tài khoản nhưng không tài khoản nào là mặc định
        PaymentException e = assertThrows(PaymentException.class,
                () -> rewardInvoiceDAO.createRewardInvoice(invoiceOf(854, 862)));
        assertEquals("Thiếu thông tin nhận thưởng: Liam Lawson", e.getMessage());
    }

    @Test
    void noBankAccount() {
        PaymentException e = assertThrows(PaymentException.class,
                () -> rewardInvoiceDAO.createRewardInvoice(invoiceOf(859)));
        assertEquals("Thiếu thông tin nhận thưởng: Isack Hadjar", e.getMessage());
    }

    @Test
    void lockedDefaultBankAccount() {
        PaymentException e = assertThrows(PaymentException.class,
                () -> rewardInvoiceDAO.createRewardInvoice(invoiceOf(863)));
        assertEquals("Thiếu thông tin nhận thưởng: Pierre Gasly", e.getMessage());
    }

    @Test
    void multipleBankAccountsOnlyDefaultUsed() {
        // Lando Norris có nhiều tài khoản, đúng 1 tài khoản mặc định
        RegisteredReceipt r = em.find(RegisteredReceipt.class, 858);
        com.ngtukien.f1.model.Driver driver = r.getRegisteredDriver().getDriver();
        assertTrue(driver.getListBankAccount().size() >= 2);
        assertEquals(1, driver.getListBankAccount().stream().filter(a -> Boolean.TRUE.equals(a.getIsDefault())).count());

        RewardInvoice inv = rewardInvoiceDAO.createRewardInvoice(invoiceOf(858));
        assertEquals("Thanh toán thành công", inv.getStatus());
    }

    @Test
    void zeroReward() {
        assertThrows(PaymentException.class, () -> rewardInvoiceDAO.createRewardInvoice(invoiceOf(864))); // hạng 11
        assertThrows(PaymentException.class, () -> rewardInvoiceDAO.createRewardInvoice(invoiceOf(873))); // bỏ cuộc
    }

    @Test
    void emptyPaymentTable() {
        assertThrows(PaymentException.class, () -> rewardInvoiceDAO.createRewardInvoice(invoiceOf()));
    }

    @Test
    void payDirectRewardRejectsMultipleDrivers() {
        RewardInvoice inv = invoiceOf(862, 838);
        inv.setPaymentMethod("Trả thưởng trực tiếp");
        PaymentException e = assertThrows(PaymentException.class, () -> rewardInvoiceDAO.createRewardInvoice(inv));
        assertEquals("Trả thưởng trực tiếp chỉ thực hiện cho từng tay đua", e.getMessage());
    }

    @Test
    void payDirectRewardSuccessEvenWithoutDefaultBankAccount() {
        // Trả thưởng trực tiếp không cần tài khoản ngân hàng mặc định (Lawson ở US 2026, hạng 9)
        RewardInvoice inv = invoiceOf(862);
        inv.setPaymentMethod("Trả thưởng trực tiếp");
        RewardInvoice saved = rewardInvoiceDAO.createRewardInvoice(inv);
        assertNotNull(saved.getId());
        assertEquals(8_000_000, saved.getAmount());
        assertEquals("Trả thưởng trực tiếp", saved.getPaymentMethod());
        assertEquals("Thanh toán thành công", saved.getStatus());
        assertEquals("Đã trả", em.find(RegisteredReceipt.class, 862).getPaymentStatus());
    }
}
