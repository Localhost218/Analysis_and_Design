package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.OrganizerDAO;
import com.ngtukien.f1.model.Member;
import com.ngtukien.f1.model.Organizer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizers")
public class OrganizerController {
    private final OrganizerDAO organizerDAO;

    public OrganizerController(OrganizerDAO organizerDAO) {
        this.organizerDAO = organizerDAO;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> checkLogin(@RequestBody Member member) {
        Organizer organizer = organizerDAO.checkLogin(member);
        if (organizer == null) {
            return ApiError.of(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác");
        }
        return ResponseEntity.ok(organizer);
    }
}
