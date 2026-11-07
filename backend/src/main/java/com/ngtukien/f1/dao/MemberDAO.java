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
}
