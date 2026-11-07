package com.ngtukien.f1.dao;

/**
 * Lỗi khi trả thưởng (bảng thanh toán trống, kết quả đã trả, thiếu tài khoản nhận thưởng...).
 * Không kế thừa IllegalStateException/IllegalArgumentException để @Repository không bọc lại thành
 * InvalidDataAccessApiUsageException; giao dịch vẫn rollback vì đây là RuntimeException.
 */
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}
