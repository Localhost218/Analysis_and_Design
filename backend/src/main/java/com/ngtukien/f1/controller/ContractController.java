package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.ContractDAO;
import com.ngtukien.f1.model.Contract;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {
    private final ContractDAO contractDAO;

    public ContractController(ContractDAO contractDAO) {
        this.contractDAO = contractDAO;
    }

    @PostMapping
    public ResponseEntity<Object> createContract(@RequestBody Contract contract) {
        if (!contractDAO.createContract(contract)) {
            return ApiError.of(HttpStatus.BAD_REQUEST, "Thông tin hợp đồng không hợp lệ");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(contract);
    }

    @PutMapping("/{id}/signed")
    public ResponseEntity<Object> updateSignedStatus(@PathVariable int id) {
        Contract contract = contractDAO.updateSignedStatus(id);
        if (contract == null) {
            return ApiError.of(HttpStatus.CONFLICT, "Hợp đồng không tồn tại hoặc không ở trạng thái Chờ ký");
        }
        return ResponseEntity.ok(contract);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getContract(@PathVariable int id) {
        Contract contract = contractDAO.getContractById(id);
        if (contract == null) {
            return ApiError.of(HttpStatus.NOT_FOUND, "Không tìm thấy hợp đồng");
        }
        return ResponseEntity.ok(contract);
    }
}

