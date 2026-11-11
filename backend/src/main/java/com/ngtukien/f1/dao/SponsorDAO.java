package com.ngtukien.f1.dao;

import com.ngtukien.f1.exception.SponsorException;
import com.ngtukien.f1.model.Contract;
import com.ngtukien.f1.model.Member;
import com.ngtukien.f1.model.Sponsor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class SponsorDAO extends MemberDAO {
    public static final String COOPERATING = "Đang hợp tác";

    /**
     * Đăng nhập thành viên Nhà tài trợ (bằng username hoặc taxCode).
     * Trả về Sponsor nếu đúng, null nếu sai hoặc tài khoản không phải Nhà tài trợ.
     */
    @Override
    public Sponsor checkLogin(Member member) {
        if (member == null || isBlank(member.getUsername()) || isBlank(member.getPassword())) {
            return null;
        }
        String u = member.getUsername().trim();
        String p = member.getPassword();
        return em.createQuery(
                "SELECT s FROM Sponsor s WHERE (s.username = :u OR s.taxCode = :u) AND s.password = :p", Sponsor.class)
                .setParameter("u", u)
                .setParameter("p", p)
                .getResultStream().findFirst().orElse(null);
    }

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

    /**
     * Cập nhật thông tin nhà tài trợ.
     * false nếu thiếu thông tin, sai định dạng hoặc mã số thuế trùng với thành viên khác.
     */
    @Transactional
    public boolean updateSponsor(Sponsor sponsor) {
        if (sponsor == null || sponsor.getId() == null
                || isBlank(sponsor.getName()) || isBlank(sponsor.getTaxCode())
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

        Long conflict = em.createQuery("SELECT COUNT(s) FROM Sponsor s WHERE s.taxCode = :t AND s.id != :id", Long.class)
                .setParameter("t", taxCode)
                .setParameter("id", sponsor.getId())
                .getSingleResult();
        if (conflict > 0) {
            return false;
        }

        Sponsor existing = em.find(Sponsor.class, sponsor.getId());
        if (existing == null) {
            return false;
        }

        existing.setName(sponsor.getName().trim());
        existing.setTaxCode(taxCode);
        existing.setField(sponsor.getField().trim());
        existing.setAddress(sponsor.getAddress().trim());
        existing.setTel(sponsor.getTel().trim());
        existing.setEmail(sponsor.getEmail().trim());
        if (!isBlank(sponsor.getStatus())) {
            existing.setStatus(sponsor.getStatus().trim());
        }
        em.merge(existing);
        return true;
    }

    /**
     * Xóa nhà tài trợ khỏi hệ thống.
     * Ném SponsorException nếu nhà tài trợ đã có hợp đồng liên kết.
     * false nếu không tìm thấy.
     */
    @Transactional
    public boolean deleteSponsor(int sponsorId) {
        Sponsor sponsor = em.find(Sponsor.class, sponsorId);
        if (sponsor == null) {
            return false;
        }
        Long contractCount = em.createQuery("SELECT COUNT(c) FROM Contract c WHERE c.sponsor.id = :sid", Long.class)
                .setParameter("sid", sponsorId)
                .getSingleResult();
        if (contractCount > 0) {
            throw new SponsorException("Không thể xóa nhà tài trợ đã có hợp đồng liên kết trong hệ thống. Vui lòng cập nhật trạng thái sang 'Ngừng hợp tác'.");
        }
        em.remove(sponsor);
        return true;
    }

    /**
     * Lấy toàn bộ hợp đồng của nhà tài trợ (cả Chưa ký / Chờ ký và Đã ký).
     */
    public List<Contract> getContractsBySponsor(int sponsorId) {
        List<Contract> contracts = em.createQuery(
                "SELECT DISTINCT c FROM Contract c " +
                "LEFT JOIN FETCH c.season " +
                "LEFT JOIN FETCH c.organizer " +
                "WHERE c.sponsor.id = :sid ORDER BY c.id DESC", Contract.class)
                .setParameter("sid", sponsorId)
                .getResultList();
        for (Contract c : contracts) {
            Hibernate.initialize(c.getListContractTerm());
            Hibernate.initialize(c.getListContractBenefit());
        }
        return contracts;
    }
}
