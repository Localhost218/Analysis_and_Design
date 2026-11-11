package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ContractDAOTest {
    @Autowired
    ContractDAO contractDAO;

    private static <T> T withId(T entity, int id) {
        if (entity instanceof Member m) m.setId(id);
        if (entity instanceof Season s) s.setId(id);
        if (entity instanceof Term t) t.setId(id);
        if (entity instanceof Benefit b) b.setId(id);
        return entity;
    }

    /** Hợp đồng theo kịch bản: Heineken (b) tài trợ F1 FIA 2026. */
    private static Contract redBlueContract() {
        Contract c = new Contract();
        c.setSponsor(withId(new Sponsor(), 2));
        c.setSeason(withId(new Season(), 2));
        c.setOrganizer(withId(new Organizer(), 1));
        c.setValue(1_000_000_000);
        c.setEffectiveFrom(LocalDate.of(2026, 3, 6));
        c.setEffectiveTo(LocalDate.of(2026, 12, 6));
        c.setSponsorRepresentative("Trần Minh Quân");
        c.setOrganizerRepresentative("Nguyễn Văn A");
        ContractTerm t1 = new ContractTerm();
        t1.setTerm(withId(new Term(), 1));
        t1.setNotes("thanh toán trong vòng 7 ngày sau khi kết thúc chặng");
        ContractTerm t2 = new ContractTerm();
        t2.setTerm(withId(new Term(), 2));
        ContractBenefit b1 = new ContractBenefit();
        b1.setBenefit(withId(new Benefit(), 2));
        b1.setValue(10);
        b1.setUnit("vé");
        c.setListContractTerm(List.of(t1, t2));
        c.setListContractBenefit(List.of(b1));
        return c;
    }

    @Test
    void createThenSign() {
        Contract c = redBlueContract();
        assertTrue(contractDAO.createContract(c));
        assertNotNull(c.getId());
        assertEquals("C" + (100 + c.getId()), c.getBarcode());
        assertEquals("Chờ ký", c.getStatus());
        assertEquals(LocalDate.now(), c.getCreatedDate());
        assertNull(c.getSignedDate());
        assertEquals(2, c.getListContractTerm().size());
        assertEquals(1, c.getListContractBenefit().size());

        Contract signed = contractDAO.updateSignedStatus(c.getId());
        assertEquals("Đã ký", signed.getStatus());
        assertEquals(LocalDate.now(), signed.getSignedDate());
        // ký lại lần nữa thì không hợp lệ
        assertNull(contractDAO.updateSignedStatus(c.getId()));
    }

    @Test
    void createInvalidDates() {
        Contract c = redBlueContract();
        c.setEffectiveTo(LocalDate.of(2026, 1, 1));
        assertFalse(contractDAO.createContract(c));
    }

    @Test
    void createDuplicateTerm() {
        Contract c = redBlueContract();
        ContractTerm dup = new ContractTerm();
        dup.setTerm(withId(new Term(), 1));
        c.setListContractTerm(List.of(c.getListContractTerm().get(0), dup));
        assertFalse(contractDAO.createContract(c));
    }

    @Test
    void createWithoutTerms() {
        Contract c = redBlueContract();
        c.setListContractTerm(List.of());
        assertFalse(contractDAO.createContract(c));
    }

    @Test
    void createWithoutBenefits() {
        Contract c = redBlueContract();
        c.setListContractBenefit(List.of());
        assertFalse(contractDAO.createContract(c));
    }

    @Test
    void createWithStoppedSponsor() {
        // Salesforce (id=69) có trạng thái "Ngừng hợp tác" -> không được phép ký hợp đồng
        Contract c = redBlueContract();
        c.setSponsor(withId(new Sponsor(), 69));
        assertFalse(contractDAO.createContract(c));
    }

    @Test
    void createDuplicateBenefit() {
        Contract c = redBlueContract();
        ContractBenefit dup = new ContractBenefit();
        dup.setBenefit(withId(new Benefit(), 2));
        c.setListContractBenefit(List.of(c.getListContractBenefit().get(0), dup));
        assertFalse(contractDAO.createContract(c));
    }
}

