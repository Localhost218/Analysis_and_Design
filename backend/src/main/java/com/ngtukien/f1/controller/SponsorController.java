package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.SponsorDAO;
import com.ngtukien.f1.exception.SponsorException;
import com.ngtukien.f1.model.Member;
import com.ngtukien.f1.model.Sponsor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sponsors")
public class SponsorController {
    private final SponsorDAO sponsorDAO;

    public SponsorController(SponsorDAO sponsorDAO) {
        this.sponsorDAO = sponsorDAO;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> checkLogin(@RequestBody Member member) {
        Sponsor sponsor = sponsorDAO.checkLogin(member);
        if (sponsor == null) {
            return ApiError.of(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác");
        }
        return ResponseEntity.ok(sponsor);
    }

    @GetMapping
    public List<Sponsor> searchByName(@RequestParam(defaultValue = "") String name) {
        return sponsorDAO.searchByName(name);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getSponsorInfo(@PathVariable int id) {
        Sponsor sponsor = sponsorDAO.getSponsorInfo(id);
        if (sponsor == null) {
            return ApiError.of(HttpStatus.NOT_FOUND, "Không tìm thấy nhà tài trợ");
        }
        return ResponseEntity.ok(sponsor);
    }

    @PostMapping
    public ResponseEntity<Object> createSponsor(@RequestBody Sponsor sponsor) {
        if (!sponsorDAO.createSponsor(sponsor)) {
            return ApiError.of(HttpStatus.CONFLICT,
                    "Thông tin nhà tài trợ không hợp lệ hoặc mã số thuế đã tồn tại");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(sponsor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateSponsor(@PathVariable int id, @RequestBody Sponsor sponsor) {
        sponsor.setId(id);
        if (!sponsorDAO.updateSponsor(sponsor)) {
            return ApiError.of(HttpStatus.BAD_REQUEST,
                    "Thông tin nhà tài trợ không hợp lệ hoặc mã số thuế đã tồn tại");
        }
        return ResponseEntity.ok(sponsorDAO.getSponsorInfo(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteSponsor(@PathVariable int id) {
        try {
            if (!sponsorDAO.deleteSponsor(id)) {
                return ApiError.of(HttpStatus.NOT_FOUND, "Không tìm thấy nhà tài trợ");
            }
            return ResponseEntity.ok(java.util.Map.of("message", "Đã xóa nhà tài trợ thành công"));
        } catch (SponsorException e) {
            return ApiError.of(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}/contracts")
    public List<com.ngtukien.f1.model.Contract> getContractsBySponsor(@PathVariable int id) {
        return sponsorDAO.getContractsBySponsor(id);
    }
}

