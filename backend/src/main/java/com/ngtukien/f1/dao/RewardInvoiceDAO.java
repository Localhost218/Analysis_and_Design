package com.ngtukien.f1.dao;

import com.ngtukien.f1.exception.PaymentException;
import com.ngtukien.f1.model.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Transactional(readOnly = true)
public class RewardInvoiceDAO extends DAO {
    public static final String SUCCESS = "Thanh toán thành công";
    public static final String DEFAULT_METHOD = "Chuyển khoản online";
    public static final String DIRECT_METHOD = "Trả thưởng trực tiếp";

    /**
     * Lưu hóa đơn và gán hóa đơn cho các kết quả trong listRegisteredReceipt trong cùng một giao dịch.
     * Ném PaymentException nếu không thể thanh toán (bảng trống, kết quả đã trả, thiếu tài khoản
     * nhận thưởng mặc định...); khi đó giao dịch rollback, không có gì được lưu.
     */
    @Transactional
    public RewardInvoice createRewardInvoice(RewardInvoice invoice) {
        if (invoice == null || invoice.getOrganizer() == null || invoice.getOrganizer().getId() == null) {
            throw new PaymentException("Không xác định được người thực hiện thanh toán");
        }
        Organizer organizer = em.find(Organizer.class, invoice.getOrganizer().getId());
        if (organizer == null) {
            throw new PaymentException("Không xác định được người thực hiện thanh toán");
        }
        if (invoice.getListRegisteredReceipt() == null || invoice.getListRegisteredReceipt().isEmpty()) {
            throw new PaymentException("Bảng thanh toán đang trống");
        }

        String paymentMethod = invoice.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = DEFAULT_METHOD;
        }
        boolean isDirect = DIRECT_METHOD.equalsIgnoreCase(paymentMethod.trim());

        List<RegisteredReceipt> receipts = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        long total = 0;
        for (RegisteredReceipt item : invoice.getListRegisteredReceipt()) {
            RegisteredReceipt r = item == null || item.getId() == null ? null : em.find(RegisteredReceipt.class, item.getId());
            if (r == null || !ids.add(r.getId())) {
                throw new PaymentException("Kết quả thi đấu không hợp lệ");
            }
            Driver driver = r.getRegisteredDriver().getDriver();
            if (r.getRewardInvoice() != null) {
                throw new PaymentException("Có kết quả đã được trả thưởng: " + driver.getFullName());
            }
            if (r.getReward() == null || r.getReward() <= 0) {
                throw new PaymentException("Tiền thưởng bằng 0 VNĐ: " + driver.getFullName());
            }
            // Chỉ bắt buộc tài khoản nhận thưởng mặc định còn hoạt động khi thanh toán "Chuyển khoản online"
            if (!isDirect) {
                Hibernate.initialize(driver.getListBankAccount());
                boolean hasDefaultAccount = driver.getListBankAccount().stream()
                        .anyMatch(a -> Boolean.TRUE.equals(a.getIsDefault()) && BankAccount.ACTIVE.equals(a.getStatus()));
                if (!hasDefaultAccount) {
                    throw new PaymentException("Thiếu thông tin nhận thưởng: " + driver.getFullName());
                }
            }
            total += r.getReward();
            receipts.add(r);
        }
        if (total > Integer.MAX_VALUE) {
            throw new PaymentException("Tổng tiền vượt quá giới hạn cho một hóa đơn");
        }

        invoice.setId(null);
        invoice.setOrganizer(organizer);
        invoice.setPaidAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        invoice.setAmount((int) total);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setStatus(SUCCESS);
        invoice.setListRegisteredReceipt(receipts);
        em.persist(invoice);
        em.flush();
        invoice.setBarcode(String.format("HD%03d", invoice.getId()));
        for (RegisteredReceipt r : receipts) {
            r.setRewardInvoice(invoice);
        }
        return invoice;
    }

    /** Lấy thông tin hóa đơn chi tiết theo ID. */
    public RewardInvoice getInvoiceById(int id) {
        RewardInvoice invoice = em.find(RewardInvoice.class, id);
        if (invoice != null) {
            Hibernate.initialize(invoice.getOrganizer());
            Hibernate.initialize(invoice.getListRegisteredReceipt());
            for (RegisteredReceipt r : invoice.getListRegisteredReceipt()) {
                Hibernate.initialize(r.getRace());
                if (r.getRace() != null) {
                    Hibernate.initialize(r.getRace().getSeason());
                }
                Hibernate.initialize(r.getRegisteredDriver());
                if (r.getRegisteredDriver() != null) {
                    Hibernate.initialize(r.getRegisteredDriver().getDriver());
                }
            }
        }
        return invoice;
    }
}
