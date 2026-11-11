package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.DriverDAO;
import com.ngtukien.f1.exception.DriverException;
import com.ngtukien.f1.model.Bank;
import com.ngtukien.f1.model.BankAccount;
import com.ngtukien.f1.model.Driver;
import com.ngtukien.f1.model.Member;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    private final DriverDAO driverDAO;

    public DriverController(DriverDAO driverDAO) {
        this.driverDAO = driverDAO;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> checkLogin(@RequestBody Member member) {
        Driver driver = driverDAO.checkLogin(member);
        if (driver == null) {
            return ApiError.of(HttpStatus.UNAUTHORIZED, "Tên đăng nhập hoặc mật khẩu không chính xác");
        }
        return ResponseEntity.ok(driver);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getDriverInfo(@PathVariable int id) {
        Driver driver = driverDAO.getDriverInfo(id);
        if (driver == null) {
            return ApiError.of(HttpStatus.NOT_FOUND, "Không tìm thấy tay đua");
        }
        return ResponseEntity.ok(driver);
    }

    @GetMapping("/{id}/rewards")
    public List<com.ngtukien.f1.model.RegisteredReceipt> getRewardsByDriver(@PathVariable int id) {
        return driverDAO.getRewardsByDriver(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateDriverInfo(@PathVariable int id, @RequestBody Driver driver) {
        try {
            return okOrNotFound(driverDAO.updateDriverInfo(id, driver));
        } catch (DriverException e) {
            return ApiError.of(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/banks")
    public List<Bank> getAllBanks() {
        return driverDAO.getAllBanks();
    }

    @PostMapping("/{id}/bank-accounts")
    public ResponseEntity<Object> addBankAccount(@PathVariable int id, @RequestBody BankAccount account) {
        try {
            return okOrNotFound(driverDAO.addBankAccount(id, account));
        } catch (DriverException e) {
            return ApiError.of(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}/bank-accounts/{accountId}")
    public ResponseEntity<Object> updateBankAccount(@PathVariable int id, @PathVariable int accountId,
                                                    @RequestBody BankAccount account) {
        try {
            return okOrNotFound(driverDAO.updateBankAccount(id, accountId, account));
        } catch (DriverException e) {
            return ApiError.of(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}/bank-accounts/{accountId}")
    public ResponseEntity<Object> deleteBankAccount(@PathVariable int id, @PathVariable int accountId) {
        try {
            return okOrNotFound(driverDAO.deleteBankAccount(id, accountId));
        } catch (DriverException e) {
            return ApiError.of(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private static ResponseEntity<Object> okOrNotFound(Driver driver) {
        return driver == null
                ? ApiError.of(HttpStatus.NOT_FOUND, "Không tìm thấy tay đua hoặc tài khoản ngân hàng")
                : ResponseEntity.ok(driver);
    }
}
