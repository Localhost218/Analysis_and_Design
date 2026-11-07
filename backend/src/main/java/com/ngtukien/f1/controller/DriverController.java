package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.DriverDAO;
import com.ngtukien.f1.model.Driver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    private final DriverDAO driverDAO;

    public DriverController(DriverDAO driverDAO) {
        this.driverDAO = driverDAO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getDriverInfo(@PathVariable int id) {
        Driver driver = driverDAO.getDriverInfo(id);
        if (driver == null) {
            return ApiError.of(HttpStatus.NOT_FOUND, "Không tìm thấy tay đua");
        }
        return ResponseEntity.ok(driver);
    }
}
