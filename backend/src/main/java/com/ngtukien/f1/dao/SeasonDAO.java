package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.Season;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class SeasonDAO extends DAO {

    public List<Season> searchByName(String seasonName) {
        return em.createQuery("SELECT s FROM Season s WHERE LOWER(s.name) LIKE :k ORDER BY s.year DESC, s.name", Season.class)
                .setParameter("k", like(seasonName))
                .getResultList();
    }

    /** false nếu thiếu thông tin, ngày không hợp lệ hoặc mùa giải (tên + năm) đã tồn tại. */
    @Transactional
    public boolean createSeason(Season season) {
        if (season == null || isBlank(season.getName()) || season.getYear() == null
                || season.getStartDate() == null || season.getEndDate() == null
                || season.getYear() < 1950 || season.getYear() > 2100
                || !season.getStartDate().isBefore(season.getEndDate())) {
            return false;
        }
        Long existed = em.createQuery("SELECT COUNT(s) FROM Season s WHERE LOWER(s.name) = :n AND s.year = :y", Long.class)
                .setParameter("n", season.getName().trim().toLowerCase())
                .setParameter("y", season.getYear())
                .getSingleResult();
        if (existed > 0) {
            return false;
        }
        season.setId(null);
        season.setName(season.getName().trim());
        em.persist(season);
        return true;
    }

    /** Mùa giải kèm danh sách chặng đua (listRace). */
    public Season getSeasonInfo(int seasonId) {
        Season season = em.find(Season.class, seasonId);
        if (season != null) {
            Hibernate.initialize(season.getListRace());
        }
        return season;
    }
}
