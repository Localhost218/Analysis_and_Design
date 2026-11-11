package com.ngtukien.f1.exception;

/**
 * Lỗi khi trả thưởng (bảng thanh toán trống, kết quả đã trả, thiếu tài khoản nhận thưởng...).
 * Kế thừa RuntimeException để giao dịch rollback nhưng không bị @Repository bọc lại
 * thành InvalidDataAccessApiUsageException.
 */
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}
