package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.Race;
import com.ngtukien.f1.model.RegisteredReceipt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RaceDAOTest {
    @Autowired
    RaceDAO raceDAO;

    private Map<String, Integer> rewardByDriver(int raceId) {
        Race race = raceDAO.getRaceInfo(raceId);
        return race.getListRegisteredReceipt().stream().collect(Collectors.toMap(
                r -> r.getRegisteredDriver().getDriver().getFullName(), RegisteredReceipt::getReward));
    }

    @Test
    void rewardByRankCoefficient() {
        Map<String, Integer> r = rewardByDriver(3); // C03 – Australian, mức thưởng 100.000.000
        assertEquals(100_000_000, r.get("Trần Văn B"));
        assertEquals(72_000_000, r.get("Nguyễn Văn An"));
        assertEquals(60_000_000, r.get("Max Verstappen")); // đã trả, giữ nguyên
        assertEquals(16_000_000, r.get("Lê Minh Cường"));
        assertEquals(0, r.get("Oscar Piastri"));          // Bỏ cuộc
    }

    @Test
    void rewardTiedRank() {
        Map<String, Integer> r = rewardByDriver(1); // C01 – Bahrain 2025: đồng hạng 2
        assertEquals(66_000_000, r.get("Charles Leclerc"));
        assertEquals(66_000_000, r.get("Lando Norris"));
        assertEquals(48_000_000, r.get("Lewis Hamilton"));
        assertEquals(0, r.get("George Russell"));         // Không xuất phát
    }

    @Test
    void rewardAbuDhabi() {
        assertEquals(86_400_000, rewardByDriver(4).get("Lando Norris")); // 120.000.000 × 0.72
    }

    @Test
    void raceWithoutBasePrizeOrResults() {
        Race race = raceDAO.getRaceInfo(2);
        assertNull(race.getBasePrize());
        assertTrue(race.getListRegisteredReceipt().isEmpty());
        assertNull(raceDAO.getRaceInfo(9999));
    }

    @Test
    void threeWayTieAtTop() {
        RegisteredReceipt a = receipt(1), b = receipt(1), c = receipt(1), d = receipt(4);
        RaceDAO.calculateReward(100_000_000, List.of(a, b, c, d));
        // (1.00 + 0.72 + 0.60) / 3 = 0.7733333333
        assertEquals(77_333_333, a.getReward());
        assertEquals(77_333_333, c.getReward());
        assertEquals(48_000_000, d.getReward());
    }

    private static RegisteredReceipt receipt(int rank) {
        RegisteredReceipt r = new RegisteredReceipt();
        r.setRank(rank);
        r.setCompletedStatus("Hoàn thành");
        return r;
    }
}
