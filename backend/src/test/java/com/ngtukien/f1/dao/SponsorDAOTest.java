package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.Contract;
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
}
