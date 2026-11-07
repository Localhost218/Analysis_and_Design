package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.Term;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class TermDAO extends DAO {
    public static final String ACTIVE = "Hoạt động";

    public List<Term> searchByTitle(String termTitle) {
        return em.createQuery("SELECT t FROM Term t WHERE LOWER(t.title) LIKE :k AND t.status = :s ORDER BY t.title", Term.class)
                .setParameter("k", like(termTitle))
                .setParameter("s", ACTIVE)
                .getResultList();
    }

    /** false nếu thiếu thông tin hoặc điều khoản (cùng tiêu đề) đã tồn tại. */
    @Transactional
    public boolean createTerm(Term term) {
        if (term == null || isBlank(term.getTitle()) || isBlank(term.getDetails()) || isBlank(term.getType())) {
            return false;
        }
        Long existed = em.createQuery("SELECT COUNT(t) FROM Term t WHERE LOWER(t.title) = :t", Long.class)
                .setParameter("t", term.getTitle().trim().toLowerCase())
                .getSingleResult();
        if (existed > 0) {
            return false;
        }
        term.setId(null);
        term.setTitle(term.getTitle().trim());
        term.setStatus(ACTIVE);
        em.persist(term);
        return true;
    }
}
