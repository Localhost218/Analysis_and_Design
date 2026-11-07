package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.RaceDAO;
import com.ngtukien.f1.model.Race;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/races")
public class RaceController {
    private final RaceDAO raceDAO;

    public RaceController(RaceDAO raceDAO) {
        this.raceDAO = raceDAO;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getRaceInfo(@PathVariable int id) {
        Race race = raceDAO.getRaceInfo(id);
        if (race == null) {
            return ApiError.of(HttpStatus.NOT_FOUND, "Không tìm thấy chặng đua");
        }
        return ResponseEntity.ok(race);
    }
}
