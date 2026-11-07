package com.ngtukien.f1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tblContract")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String barcode;
    private LocalDate createdDate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Integer value;
    private String sponsorRepresentative;
    private String organizerRepresentative;
    private LocalDate signedDate;
    private String status;

    @ManyToOne
    @JoinColumn(name = "tblSponsorMemberid")
    @JsonIgnoreProperties("listContract")
    private Sponsor sponsor;

    @ManyToOne
    @JoinColumn(name = "tblSeasonid")
    @JsonIgnoreProperties("listRace")
    private Season season;

    @ManyToOne
    @JoinColumn(name = "tblOrganizerMemberid")
    private Organizer organizer;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tblContractid", nullable = false)
    private List<ContractTerm> listContractTerm = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tblContractid", nullable = false)
    private List<ContractBenefit> listContractBenefit = new ArrayList<>();
}
