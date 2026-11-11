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
        Map<String, Integer> r = rewardByDriver(42); // C18 – United States 2026, mức thưởng 100.000.000, chưa trả
        assertEquals(100_000_000, r.get("Kimi Antonelli"));
        assertEquals(72_000_000, r.get("Charles Leclerc"));
        assertEquals(32_000_000, r.get("Isack Hadjar"));
        assertEquals(4_000_000, r.get("Pierre Gasly"));
        assertEquals(0, r.get("Nico Hulkenberg"));      // hạng 11
        assertEquals(0, r.get("Valtteri Bottas"));      // Bỏ cuộc
    }

    @Test
    void paidRewardKeptAndHigherBasePrize() {
        assertEquals(100_000_000, rewardByDriver(41).get("Max Verstappen")); // Singapore 2026, đã trả
        assertEquals(24_000_000, rewardByDriver(41).get("Liam Lawson"));     // hạng 7, chưa trả
        Race monaco = raceDAO.getRaceInfo(30);                                // Monaco 2026, mức thưởng 150.000.000
        assertEquals(150_000_000, monaco.getBasePrize());
        assertEquals(150_000_000, monaco.getListRegisteredReceipt().stream()
                .filter(x -> Integer.valueOf(1).equals(x.getRank())).findFirst().orElseThrow().getReward());
    }

    @Test
    void twoWayTie() {
        RegisteredReceipt a = receipt(1), b = receipt(2), c = receipt(2), d = receipt(4);
        RaceDAO.calculateReward(100_000_000, List.of(a, b, c, d));
        assertEquals(100_000_000, a.getReward());
        assertEquals(66_000_000, b.getReward()); // (0.72 + 0.60) / 2
        assertEquals(66_000_000, c.getReward());
        assertEquals(48_000_000, d.getReward());
    }

    @Test
    void raceWithoutBasePrizeOrResults() {
        Race mexico = raceDAO.getRaceInfo(43); // Mexico 2026: có kết quả, chưa có mức thưởng
        assertNull(mexico.getBasePrize());
        assertFalse(mexico.getListRegisteredReceipt().isEmpty());
        Race brazil = raceDAO.getRaceInfo(44); // Brazil 2026: có mức thưởng, chưa nhập kết quả
        assertNotNull(brazil.getBasePrize());
        assertTrue(brazil.getListRegisteredReceipt().isEmpty());
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
