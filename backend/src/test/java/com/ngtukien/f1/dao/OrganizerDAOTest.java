package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.Member;
import com.ngtukien.f1.model.Organizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrganizerDAOTest {
    @Autowired
    OrganizerDAO organizerDAO;

    private static Member member(String u, String p) {
        Member m = new Member();
        m.setUsername(u);
        m.setPassword(p);
        return m;
    }

    @Test
    void checkLoginSuccess() {
        Organizer o = organizerDAO.checkLogin(member("b", "b@123"));
        assertNotNull(o);
        assertEquals("Nguyễn Thị B", o.getFullName());
    }

    @Test
    void checkLoginWrongPassword() {
        assertNull(organizerDAO.checkLogin(member("b", "sai")));
    }

    @Test
    void checkLoginNotOrganizer() {
        // tài khoản nhà tài trợ không đăng nhập được vào chức năng Ban tổ chức
        assertNull(organizerDAO.checkLogin(member("redblue", "redblue@123")));
    }
}
