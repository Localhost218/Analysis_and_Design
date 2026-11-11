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

    @BeforeEach
    void calculateRewards() {
        raceDAO.getRaceInfo(3);
        raceDAO.getRaceInfo(4);
    }

    private static RewardInvoice invoiceOf(Integer... receiptIds) {
        RewardInvoice inv = new RewardInvoice();
        Organizer o = new Organizer();
        o.setId(2);
        inv.setOrganizer(o);
        inv.setListRegisteredReceipt(Arrays.stream(receiptIds).map(id -> {
            RegisteredReceipt r = new RegisteredReceipt();
            r.setId(id);
            return r;
        }).toList());
        return inv;
    }

    @Test
    void payManyDriversAcrossSeasons() {
        // Kịch bản: Trần Văn B + Nguyễn Văn An (C03 2025) + Lando Norris (C24 2024) = 258.400.000
        RewardInvoice inv = rewardInvoiceDAO.createRewardInvoice(invoiceOf(1, 2, 11));
        assertEquals(258_400_000, inv.getAmount());
        assertEquals(String.format("HD%03d", inv.getId()), inv.getBarcode());
        assertEquals("Thanh toán thành công", inv.getStatus());
        assertEquals("Chuyển khoản online", inv.getPaymentMethod());
        assertEquals("Đã trả", em.find(RegisteredReceipt.class, 1).getPaymentStatus());
        assertEquals("Đã trả", em.find(RegisteredReceipt.class, 11).getPaymentStatus());
    }

    @Test
    void alreadyPaid() {
        PaymentException e = assertThrows(PaymentException.class,
                () -> rewardInvoiceDAO.createRewardInvoice(invoiceOf(3)));
        assertTrue(e.getMessage().contains("Max Verstappen"));
    }

    @Test
    void missingDefaultBankAccount() {
        // Lê Minh Cường có 2 tài khoản ngân hàng nhưng cả 2 đều là FALSE (không có tài khoản mặc định)
        PaymentException e = assertThrows(PaymentException.class,
                () -> rewardInvoiceDAO.createRewardInvoice(invoiceOf(1, 8)));
        assertEquals("Thiếu thông tin nhận thưởng: Lê Minh Cường", e.getMessage());
    }

    @Test
    void multipleBankAccountsOnlyDefaultUsed() {
        // Trần Văn B có 3 tài khoản ngân hàng (1 VCB mặc định, 2 tài khoản phụ TCB & MB)
        // Hệ thống kiểm tra đúng 1 tài khoản mặc định và thanh toán thành công
        RegisteredReceipt r = em.find(RegisteredReceipt.class, 1);
        com.ngtukien.f1.model.Driver driver = r.getRegisteredDriver().getDriver();
        assertEquals(3, driver.getListBankAccount().size());
        assertEquals(1, driver.getListBankAccount().stream().filter(a -> Boolean.TRUE.equals(a.getIsDefault())).count());

        RewardInvoice inv = rewardInvoiceDAO.createRewardInvoice(invoiceOf(1));
        assertEquals("Thanh toán thành công", inv.getStatus());
    }


    @Test
    void zeroReward() {
        assertThrows(PaymentException.class, () -> rewardInvoiceDAO.createRewardInvoice(invoiceOf(9)));
    }

    @Test
    void emptyPaymentTable() {
        assertThrows(PaymentException.class, () -> rewardInvoiceDAO.createRewardInvoice(invoiceOf()));
    }

    @Test
    void payDirectRewardSuccessEvenWithoutDefaultBankAccount() {
        // Lê Minh Cường (receipt 8) không có tài khoản nhận thưởng mặc định
        // Trả thưởng trực tiếp cho phép trả thành công mà không báo lỗi thiếu tài khoản
        RewardInvoice inv = invoiceOf(8);
        inv.setPaymentMethod("Trả thưởng trực tiếp");
        RewardInvoice saved = rewardInvoiceDAO.createRewardInvoice(inv);
        assertNotNull(saved.getId());
        assertEquals("Trả thưởng trực tiếp", saved.getPaymentMethod());
        assertEquals("Thanh toán thành công", saved.getStatus());
        assertEquals("Đã trả", em.find(RegisteredReceipt.class, 8).getPaymentStatus());
    }
}

