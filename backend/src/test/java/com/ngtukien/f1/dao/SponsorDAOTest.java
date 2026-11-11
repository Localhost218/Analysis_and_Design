package com.ngtukien.f1.dao;

import com.ngtukien.f1.exception.SponsorException;
import com.ngtukien.f1.model.Contract;
import com.ngtukien.f1.model.Member;
import com.ngtukien.f1.model.Sponsor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SponsorDAOTest {
    @Autowired
    SponsorDAO sponsorDAO;

    @Test
    void searchByNameRed() {
        List<String> names = sponsorDAO.searchByName("red").stream().map(Sponsor::getName).toList();
        assertTrue(names.containsAll(List.of("Red Bull", "Red Blue")));
    }

    @Test
    void searchByNameNotFound() {
        assertTrue(sponsorDAO.searchByName("không-có-nhà-tài-trợ-này").isEmpty());
    }

    @Test
    void getSponsorInfoOnlySignedContracts() {
        Sponsor s = sponsorDAO.getSponsorInfo(3);
        assertEquals("Red Bull", s.getName());
        assertFalse(s.getListContract().isEmpty());
        assertTrue(s.getListContract().stream().map(Contract::getStatus).allMatch("Đã ký"::equals));
        // Mercedes chỉ có hợp đồng "Chờ ký" -> danh sách đã ký rỗng
        assertTrue(sponsorDAO.getSponsorInfo(5).getListContract().isEmpty());
    }

    private static Sponsor newSponsor(String taxCode) {
        Sponsor s = new Sponsor();
        s.setName("Nhà tài trợ thử");
        s.setTaxCode(taxCode);
        s.setField("Công nghệ");
        s.setAddress("Hà Nội");
        s.setTel("0912000111");
        s.setEmail("test@example.com");
        return s;
    }

    @Test
    void createSponsorSuccess() {
        Sponsor s = newSponsor("0999888777");
        assertTrue(sponsorDAO.createSponsor(s));
        assertNotNull(s.getId());
        assertEquals("Đang hợp tác", s.getStatus());
    }

    @Test
    void createSponsorDuplicateTaxCode() {
        assertFalse(sponsorDAO.createSponsor(newSponsor("0123456789")));
    }

    @Test
    void createSponsorInvalidEmail() {
        Sponsor s = newSponsor("0999888777");
        s.setEmail("khong-hop-le");
        assertFalse(sponsorDAO.createSponsor(s));
    }

    @Test
    void updateSponsorSuccess() {
        Sponsor s = sponsorDAO.getSponsorInfo(4); // Red Blue
        s.setField("Truyền thông số & Công nghệ");
        s.setTel("0912345999");
        assertTrue(sponsorDAO.updateSponsor(s));

        Sponsor updated = sponsorDAO.getSponsorInfo(4);
        assertEquals("Truyền thông số & Công nghệ", updated.getField());
        assertEquals("0912345999", updated.getTel());
    }

    @Test
    void updateSponsorDuplicateTaxCode() {
        Sponsor s = newSponsor("0301234567"); // MST của Red Bull
        s.setId(4); // Cập nhật cho Red Blue
        assertFalse(sponsorDAO.updateSponsor(s));
    }

    @Test
    void deleteSponsorWithContractsThrowsException() {
        // Red Bull (id 3) đã có hợp đồng C101, C102 -> không được xóa
        assertThrows(SponsorException.class, () -> sponsorDAO.deleteSponsor(3));
    }

    @Test
    void deleteSponsorWithoutContractsSuccess() {
        Sponsor s = newSponsor("0888999111");
        assertTrue(sponsorDAO.createSponsor(s));
        int id = s.getId();
        assertTrue(sponsorDAO.deleteSponsor(id));
        assertNull(sponsorDAO.getSponsorInfo(id));
    }

    @Test
    void getContractsBySponsorIncludesWaitingAndSigned() {
        // Red Blue (id 4) có cả C105 (Chờ ký) và C106 (Đã ký)
        List<Contract> contracts = sponsorDAO.getContractsBySponsor(4);
        assertFalse(contracts.isEmpty());
        List<String> statuses = contracts.stream().map(Contract::getStatus).toList();
        assertTrue(statuses.contains("Chờ ký"));
        assertTrue(statuses.contains("Đã ký"));
    }

    @Test
    void checkLoginWithUsernameSuccess() {
        Member m = new Member();
        m.setUsername("redblue");
        m.setPassword("redblue@123");
        Sponsor s = sponsorDAO.checkLogin(m);
        assertNotNull(s);
        assertEquals("Red Blue", s.getName());
        assertEquals("0123456789", s.getTaxCode());
    }

    @Test
    void checkLoginWithTaxCodeSuccess() {
        Member m = new Member();
        m.setUsername("0123456789");
        m.setPassword("redblue@123");
        Sponsor s = sponsorDAO.checkLogin(m);
        assertNotNull(s);
        assertEquals("Red Blue", s.getName());
    }

    @Test
    void checkLoginWrongPasswordReturnsNull() {
        Member m = new Member();
        m.setUsername("redblue");
        m.setPassword("wrong-pass");
        assertNull(sponsorDAO.checkLogin(m));
    }

    @Test
    void checkLoginOrganizerAccountReturnsNull() {
        // Tài khoản của Organizer không thể đăng nhập vào SponsorDAO
        Member m = new Member();
        m.setUsername("a");
        m.setPassword("a@123");
        assertNull(sponsorDAO.checkLogin(m));
    }
}

