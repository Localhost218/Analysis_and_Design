package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.Sponsor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class SponsorDAO extends MemberDAO {
    public static final String COOPERATING = "Đang hợp tác";

    public List<Sponsor> searchByName(String sponsorName) {
        return em.createQuery("SELECT s FROM Sponsor s WHERE LOWER(s.name) LIKE :k ORDER BY s.name", Sponsor.class)
                .setParameter("k", like(sponsorName))
                .getResultList();
    }

    /** Nhà tài trợ kèm danh sách hợp đồng đã ký (listContract). */
    public Sponsor getSponsorInfo(int sponsorId) {
        Sponsor sponsor = em.find(Sponsor.class, sponsorId);
        if (sponsor != null) {
            Hibernate.initialize(sponsor.getListContract());
        }
        return sponsor;
    }

    /** false nếu thiếu thông tin, sai định dạng hoặc mã số thuế đã tồn tại. */
    @Transactional
    public boolean createSponsor(Sponsor sponsor) {
        if (sponsor == null || isBlank(sponsor.getName()) || isBlank(sponsor.getTaxCode())
                || isBlank(sponsor.getField()) || isBlank(sponsor.getAddress())
                || isBlank(sponsor.getTel()) || isBlank(sponsor.getEmail())) {
            return false;
        }
        String taxCode = sponsor.getTaxCode().trim();
        if (!taxCode.matches("\\d{10}(\\d{3})?")
                || !sponsor.getTel().trim().matches("0\\d{9,10}")
                || !sponsor.getEmail().trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            return false;
        }
        Long existed = em.createQuery("SELECT COUNT(m) FROM Member m WHERE m.username = :t OR m.id IN "
                        + "(SELECT s.id FROM Sponsor s WHERE s.taxCode = :t)", Long.class)
                .setParameter("t", taxCode).getSingleResult();
        if (existed > 0) {
            return false;
        }
        sponsor.setId(null);
        sponsor.setTaxCode(taxCode);
        sponsor.setName(sponsor.getName().trim());
        sponsor.setTel(sponsor.getTel().trim());
        sponsor.setEmail(sponsor.getEmail().trim());
        // Tài khoản đăng nhập của nhà tài trợ mặc định là mã số thuế
        sponsor.setUsername(taxCode);
        sponsor.setPassword(taxCode);
        if (isBlank(sponsor.getStatus())) {
            sponsor.setStatus(COOPERATING);
        }
        em.persist(sponsor);
        return true;
    }
}
