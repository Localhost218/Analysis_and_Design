package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.PaymentException;
import com.ngtukien.f1.dao.RewardInvoiceDAO;
import com.ngtukien.f1.model.RewardInvoice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
public class RewardInvoiceController {
    private final RewardInvoiceDAO rewardInvoiceDAO;

    public RewardInvoiceController(RewardInvoiceDAO rewardInvoiceDAO) {
        this.rewardInvoiceDAO = rewardInvoiceDAO;
    }

    @PostMapping
    public ResponseEntity<Object> createRewardInvoice(@RequestBody RewardInvoice invoice) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(rewardInvoiceDAO.createRewardInvoice(invoice));
        } catch (PaymentException e) {
            return ApiError.of(HttpStatus.CONFLICT, "Thanh toán không thành công: " + e.getMessage());
        }
    }
}
