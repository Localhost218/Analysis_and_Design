package com.ngtukien.f1.exception;

/**
 * Lỗi nghiệp vụ nhà tài trợ (trùng MST, vi phạm ràng buộc hợp đồng khi xóa...).
 */
public class SponsorException extends RuntimeException {
    public SponsorException(String message) {
        super(message);
    }
}
