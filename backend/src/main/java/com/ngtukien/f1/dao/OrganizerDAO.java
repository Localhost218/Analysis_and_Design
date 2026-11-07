package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.Member;
import com.ngtukien.f1.model.Organizer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class OrganizerDAO extends MemberDAO {

    /** Đăng nhập thành viên Ban tổ chức; null nếu sai hoặc tài khoản không phải Ban tổ chức. */
    @Override
    public Organizer checkLogin(Member member) {
        if (member == null || isBlank(member.getUsername()) || isBlank(member.getPassword())) {
            return null;
        }
        return em.createQuery("SELECT o FROM Organizer o WHERE o.username = :u AND o.password = :p", Organizer.class)
                .setParameter("u", member.getUsername().trim())
                .setParameter("p", member.getPassword())
                .getResultStream().findFirst().orElse(null);
    }
}
