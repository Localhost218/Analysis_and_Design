package com.ngtukien.f1.controller;

import com.ngtukien.f1.dao.SeasonDAO;
import com.ngtukien.f1.model.Season;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seasons")
public class SeasonController {
    private final SeasonDAO seasonDAO;

    public SeasonController(SeasonDAO seasonDAO) {
        this.seasonDAO = seasonDAO;
    }

    @GetMapping
    public List<Season> searchByName(@RequestParam(defaultValue = "") String name) {
        return seasonDAO.searchByName(name);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getSeasonInfo(@PathVariable int id) {
        Season season = seasonDAO.getSeasonInfo(id);
        if (season == null) {
            return ApiError.of(HttpStatus.NOT_FOUND, "Không tìm thấy mùa giải");
        }
        return ResponseEntity.ok(season);
    }

    @PostMapping
    public ResponseEntity<Object> createSeason(@RequestBody Season season) {
        if (!seasonDAO.createSeason(season)) {
            return ApiError.of(HttpStatus.CONFLICT, "Thông tin mùa giải không hợp lệ hoặc mùa giải đã tồn tại");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(season);
    }
}
