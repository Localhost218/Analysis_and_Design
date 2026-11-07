package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.Benefit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class BenefitDAO extends DAO {
    public static final String ACTIVE = "Hoạt động";

    public List<Benefit> searchByTitle(String benefitTitle) {
        return em.createQuery("SELECT b FROM Benefit b WHERE LOWER(b.title) LIKE :k AND b.status = :s ORDER BY b.title", Benefit.class)
                .setParameter("k", like(benefitTitle))
                .setParameter("s", ACTIVE)
                .getResultList();
    }

    /** false nếu thiếu thông tin hoặc quyền lợi (cùng tiêu đề) đã tồn tại. */
    @Transactional
    public boolean createBenefit(Benefit benefit) {
        if (benefit == null || isBlank(benefit.getTitle()) || isBlank(benefit.getDescription()) || isBlank(benefit.getType())) {
            return false;
        }
        Long existed = em.createQuery("SELECT COUNT(b) FROM Benefit b WHERE LOWER(b.title) = :t", Long.class)
                .setParameter("t", benefit.getTitle().trim().toLowerCase())
                .getSingleResult();
        if (existed > 0) {
            return false;
        }
        benefit.setId(null);
        benefit.setTitle(benefit.getTitle().trim());
        benefit.setStatus(ACTIVE);
        em.persist(benefit);
        return true;
    }
}
