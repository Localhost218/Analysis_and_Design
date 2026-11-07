package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.Race;
import com.ngtukien.f1.model.RegisteredReceipt;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class RaceDAO extends DAO {
    public static final String COMPLETED = "Hoàn thành";

    /** Hệ số xếp hạng cho hạng 1 → 10; hạng từ 11 trở đi là 0. */
    private static final BigDecimal[] COEFFICIENTS = {
            new BigDecimal("1.00"), new BigDecimal("0.72"), new BigDecimal("0.60"), new BigDecimal("0.48"),
            new BigDecimal("0.40"), new BigDecimal("0.32"), new BigDecimal("0.24"), new BigDecimal("0.16"),
            new BigDecimal("0.08"), new BigDecimal("0.04")
    };

    /**
     * Chặng đua kèm danh sách kết quả (listRegisteredReceipt), mỗi kết quả có RegisteredDriver và Driver.
     * Tiền thưởng của các kết quả chưa trả được tính lại theo mức thưởng cơ sở × hệ số xếp hạng.
     */
    @Transactional
    public Race getRaceInfo(int raceId) {
        Race race = em.find(Race.class, raceId);
        if (race == null) {
            return null;
        }
        Hibernate.initialize(race.getListRegisteredReceipt());
        if (race.getBasePrize() != null && race.getBasePrize() > 0) {
            calculateReward(race.getBasePrize(), race.getListRegisteredReceipt());
        }
        return race;
    }

    /** reward = basePrize × hệ số; đồng hạng: cộng hệ số các vị trí bị chiếm rồi chia đều. */
    static void calculateReward(int basePrize, List<RegisteredReceipt> receipts) {
        Map<Integer, Long> tiedCount = receipts.stream()
                .filter(RaceDAO::isRanked)
                .collect(Collectors.groupingBy(RegisteredReceipt::getRank, Collectors.counting()));
        for (RegisteredReceipt r : receipts) {
            if (r.getRewardInvoice() != null) {
                continue; // đã trả thưởng thì giữ nguyên số tiền đã chi
            }
            BigDecimal coefficient = BigDecimal.ZERO;
            if (isRanked(r)) {
                int rank = r.getRank();
                long k = tiedCount.get(rank);
                BigDecimal sum = BigDecimal.ZERO;
                for (int position = rank; position < rank + k; position++) {
                    sum = sum.add(coefficientOf(position));
                }
                coefficient = sum.divide(BigDecimal.valueOf(k), 10, RoundingMode.HALF_UP);
            }
            int reward = coefficient.multiply(BigDecimal.valueOf(basePrize)).setScale(0, RoundingMode.FLOOR).intValue();
            if (r.getReward() == null || r.getReward() != reward) {
                r.setReward(reward);
            }
        }
    }

    private static boolean isRanked(RegisteredReceipt r) {
        return COMPLETED.equals(r.getCompletedStatus()) && r.getRank() != null && r.getRank() > 0;
    }

    private static BigDecimal coefficientOf(int position) {
        return position >= 1 && position <= COEFFICIENTS.length ? COEFFICIENTS[position - 1] : BigDecimal.ZERO;
    }
}
