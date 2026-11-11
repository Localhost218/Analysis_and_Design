package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.Member;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class MemberDAO extends DAO {

    /** Trả về Member khớp username/password, null nếu sai. */
    public Member checkLogin(Member member) {
        if (member == null || isBlank(member.getUsername()) || isBlank(member.getPassword())) {
            return null;
        }
        return em.createQuery("SELECT m FROM Member m WHERE m.username = :u AND m.password = :p", Member.class)
                .setParameter("u", member.getUsername().trim())
                .setParameter("p", member.getPassword())
                .getResultStream().findFirst().orElse(null);
    }

    /**
     * Đăng nhập cho mọi vai trò (Ban tổ chức, Nhà tài trợ, Tay đua, Khách hàng).
     * Cho phép Nhà tài trợ đăng nhập bằng username hoặc mã số thuế.
     */
    public Member checkLoginUniversal(String identifier, String password) {
        if (isBlank(identifier) || isBlank(password)) {
            return null;
        }
        String idTrim = identifier.trim();
        Member m = em.createQuery("SELECT m FROM Member m WHERE m.username = :u AND m.password = :p", Member.class)
                .setParameter("u", idTrim)
                .setParameter("p", password)
                .getResultStream().findFirst().orElse(null);
        if (m != null) {
            return m;
        }
        return em.createQuery("SELECT s FROM Sponsor s WHERE s.taxCode = :tc AND (s.password = :p OR s.username = :p)", Member.class)
                .setParameter("tc", idTrim)
                .setParameter("p", password)
                .getResultStream().findFirst().orElse(null);
    }
}

