package com.ngtukien.f1.dao;

import com.ngtukien.f1.model.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Transactional(readOnly = true)
public class ContractDAO extends DAO {
    public static final String WAITING = "Chờ ký";
    public static final String SIGNED = "Đã ký";

    /**
     * Lưu hợp đồng cùng danh sách ContractTerm, ContractBenefit ở trạng thái "Chờ ký".
     * Sau khi lưu, contract truyền vào có id và barcode. false nếu thông tin không hợp lệ.
     */
    @Transactional
    public boolean createContract(Contract contract) {
        if (contract == null || contract.getSponsor() == null || contract.getSeason() == null
                || contract.getOrganizer() == null) {
            return false;
        }
        Sponsor sponsor = find(Sponsor.class, contract.getSponsor().getId());
        Season season = find(Season.class, contract.getSeason().getId());
        Organizer organizer = find(Organizer.class, contract.getOrganizer().getId());
        if (sponsor == null || season == null || organizer == null
                || !"Đang hợp tác".equals(sponsor.getStatus())
                || contract.getValue() == null || contract.getValue() <= 0
                || contract.getEffectiveFrom() == null || contract.getEffectiveTo() == null
                || !contract.getEffectiveFrom().isBefore(contract.getEffectiveTo())
                || isBlank(contract.getSponsorRepresentative()) || isBlank(contract.getOrganizerRepresentative())) {
            return false;
        }

        // Bắt buộc hợp đồng phải có ít nhất một điều khoản
        if (contract.getListContractTerm() == null || contract.getListContractTerm().isEmpty()) {
            return false;
        }

        // Bắt buộc hợp đồng phải có ít nhất một quyền lợi
        if (contract.getListContractBenefit() == null || contract.getListContractBenefit().isEmpty()) {
            return false;
        }

        List<ContractTerm> terms = new ArrayList<>();
        Set<Integer> termIds = new HashSet<>();
        for (ContractTerm ct : nullSafe(contract.getListContractTerm())) {
            Term term = ct.getTerm() == null ? null : find(Term.class, ct.getTerm().getId());
            if (term == null || !termIds.add(term.getId())) {
                return false;
            }
            ContractTerm row = new ContractTerm();
            row.setTerm(term);
            row.setNotes(trimToNull(ct.getNotes()));
            terms.add(row);
        }
        if (terms.isEmpty()) {
            return false;
        }

        List<ContractBenefit> benefits = new ArrayList<>();
        Set<Integer> benefitIds = new HashSet<>();
        for (ContractBenefit cb : nullSafe(contract.getListContractBenefit())) {
            Benefit benefit = cb.getBenefit() == null ? null : find(Benefit.class, cb.getBenefit().getId());
            if (benefit == null || !benefitIds.add(benefit.getId()) || (cb.getValue() != null && cb.getValue() < 0)) {
                return false;
            }
            ContractBenefit row = new ContractBenefit();
            row.setBenefit(benefit);
            row.setValue(cb.getValue());
            row.setUnit(trimToNull(cb.getUnit()));
            row.setNotes(trimToNull(cb.getNotes()));
            benefits.add(row);
        }
        if (benefits.isEmpty()) {
            return false;
        }

        contract.setId(null);
        contract.setSponsor(sponsor);
        contract.setSeason(season);
        contract.setOrganizer(organizer);
        contract.setSponsorRepresentative(contract.getSponsorRepresentative().trim());
        contract.setOrganizerRepresentative(contract.getOrganizerRepresentative().trim());
        contract.setCreatedDate(LocalDate.now());
        contract.setSignedDate(null);
        contract.setStatus(WAITING);
        contract.setListContractTerm(terms);
        contract.setListContractBenefit(benefits);
        em.persist(contract);
        em.flush();
        contract.setBarcode("C" + (100 + contract.getId()));
        return true;
    }

    /** Chỉ áp dụng cho hợp đồng "Chờ ký": ghi ngày ký, chuyển sang "Đã ký". null nếu không thỏa. */
    @Transactional
    public Contract updateSignedStatus(int contractId) {
        Contract contract = em.find(Contract.class, contractId);
        if (contract == null || !WAITING.equals(contract.getStatus())) {
            return null;
        }
        contract.setSignedDate(LocalDate.now());
        contract.setStatus(SIGNED);
        return contract;
    }

    /** Lấy chi tiết hợp đồng kèm điều khoản, quyền lợi. */
    public Contract getContractById(int contractId) {
        Contract contract = em.find(Contract.class, contractId);
        if (contract != null) {
            org.hibernate.Hibernate.initialize(contract.getSeason());
            org.hibernate.Hibernate.initialize(contract.getSponsor());
            org.hibernate.Hibernate.initialize(contract.getOrganizer());
            org.hibernate.Hibernate.initialize(contract.getListContractTerm());
            for (ContractTerm ct : contract.getListContractTerm()) {
                org.hibernate.Hibernate.initialize(ct.getTerm());
            }
            org.hibernate.Hibernate.initialize(contract.getListContractBenefit());
            for (ContractBenefit cb : contract.getListContractBenefit()) {
                org.hibernate.Hibernate.initialize(cb.getBenefit());
            }
        }
        return contract;
    }

    private <T> T find(Class<T> type, Integer id) {
        return id == null ? null : em.find(type, id);
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static String trimToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }
}
