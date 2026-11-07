package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.BenefitDAO;
import com.ngtukien.f1.model.Benefit;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/benefits")
public class BenefitController {
    private final BenefitDAO benefitDAO;

    public BenefitController(BenefitDAO benefitDAO) {
        this.benefitDAO = benefitDAO;
    }

    @GetMapping
    public List<Benefit> searchByTitle(@RequestParam(defaultValue = "") String title) {
        return benefitDAO.searchByTitle(title);
    }

    @PostMapping
    public ResponseEntity<Object> createBenefit(@RequestBody Benefit benefit) {
        if (!benefitDAO.createBenefit(benefit)) {
            return ApiError.of(HttpStatus.CONFLICT, "Thông tin quyền lợi không hợp lệ hoặc quyền lợi đã tồn tại");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(benefit);
    }
}
