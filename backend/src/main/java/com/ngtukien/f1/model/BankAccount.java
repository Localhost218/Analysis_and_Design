package com.ngtukien.f1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tblBankAccount")
public class BankAccount {
    public static final String ACTIVE = "Hoạt động";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String accountNumber;
    private String accountHolder;
    private Boolean isDefault;
    private String status;

    @ManyToOne
    @JoinColumn(name = "tblBankid")
    private Bank bank;
}
