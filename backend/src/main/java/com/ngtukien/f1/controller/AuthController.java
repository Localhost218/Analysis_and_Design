package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.DriverDAO;
import com.ngtukien.f1.dao.MemberDAO;
import com.ngtukien.f1.dao.OrganizerDAO;
import com.ngtukien.f1.dao.SponsorDAO;
import com.ngtukien.f1.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final OrganizerDAO organizerDAO;
    private final SponsorDAO sponsorDAO;
    private final DriverDAO driverDAO;
    private final MemberDAO memberDAO;

    public AuthController(OrganizerDAO organizerDAO, SponsorDAO sponsorDAO, DriverDAO driverDAO, MemberDAO memberDAO) {
        this.organizerDAO = organizerDAO;
        this.sponsorDAO = sponsorDAO;
        this.driverDAO = driverDAO;
        this.memberDAO = memberDAO;
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest req) {
        if (req == null || req.getUsername() == null || req.getPassword() == null) {
            return ApiError.of(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác");
        }
        Member probe = new Member();
        probe.setUsername(req.getUsername().trim());
        probe.setPassword(req.getPassword());

        // 1. Kiểm tra Ban tổ chức
        Organizer organizer = organizerDAO.checkLogin(probe);
        if (organizer != null) {
            return ResponseEntity.ok(Map.of("role", "ORGANIZER", "roleName", "Ban tổ chức", "user", organizer));
        }

        // 2. Kiểm tra Nhà tài trợ (bằng username hoặc taxCode)
        Sponsor sponsor = sponsorDAO.checkLogin(probe);
        if (sponsor != null) {
            return ResponseEntity.ok(Map.of("role", "SPONSOR", "roleName", "Nhà tài trợ", "user", sponsor));
        }

        // 3. Kiểm tra Tay đua
        Driver driver = driverDAO.checkLogin(probe);
        if (driver != null) {
            return ResponseEntity.ok(Map.of("role", "DRIVER", "roleName", "Tay đua", "user", driver));
        }

        // 4. Kiểm tra Khách hàng / Thành viên chung
        Member member = memberDAO.checkLogin(probe);
        if (member != null) {
            return ResponseEntity.ok(Map.of("role", "CUSTOMER", "roleName", "Khách hàng", "user", member));
        }

        return ApiError.of(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác");
    }
}
