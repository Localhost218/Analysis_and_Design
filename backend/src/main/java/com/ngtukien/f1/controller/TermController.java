package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.TermDAO;
import com.ngtukien.f1.model.Term;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terms")
public class TermController {
    private final TermDAO termDAO;

    public TermController(TermDAO termDAO) {
        this.termDAO = termDAO;
    }

    @GetMapping
    public List<Term> searchByTitle(@RequestParam(defaultValue = "") String title) {
        return termDAO.searchByTitle(title);
    }

    @PostMapping
    public ResponseEntity<Object> createTerm(@RequestBody Term term) {
        if (!termDAO.createTerm(term)) {
            return ApiError.of(HttpStatus.CONFLICT, "Thông tin điều khoản không hợp lệ hoặc điều khoản đã tồn tại");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(term);
    }
}
