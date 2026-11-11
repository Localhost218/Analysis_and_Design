package com.ngtukien.f1.exception;

/**
 * Lỗi nghiệp vụ của tay đua: thông tin cá nhân không hợp lệ, tài khoản ngân hàng không hợp lệ / trùng, vi phạm ràng buộc tài khoản mặc định.
 */
public class DriverException extends RuntimeException {
    public DriverException(String message) {
        super(message);
    }
}
