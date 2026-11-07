package com.ngtukien.f1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/** Định dạng lỗi chung trả về frontend: { "message": "..." }. */
final class ApiError {
    private ApiError() {
    }

    static ResponseEntity<Object> of(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
