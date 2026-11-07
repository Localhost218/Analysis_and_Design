package com.ngtukien.f1.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/** Lớp cha cung cấp kết nối chung (em) cho mọi DAO; Spring Boot tự cấu hình kết nối. */
public class DAO {
    @PersistenceContext
    protected EntityManager em;

    protected static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    protected static String like(String keyword) {
        return "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
    }
}
