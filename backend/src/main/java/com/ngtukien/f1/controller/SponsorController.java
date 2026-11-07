package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.SponsorDAO;
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
}
