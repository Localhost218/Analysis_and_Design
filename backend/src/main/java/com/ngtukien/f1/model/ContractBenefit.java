package com.ngtukien.f1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tblContractBenefit")
public class ContractBenefit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tblBenefitid")
    private Benefit benefit;

    private Integer value;
    private String unit;
    private String notes;
}
