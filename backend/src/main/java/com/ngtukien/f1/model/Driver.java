package com.ngtukien.f1.model;

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
@Table(name = "tblDriver")
@PrimaryKeyJoinColumn(name = "tblMemberid")
public class Driver extends Member {
    private String fullName;
    private LocalDate dateOfBirth;
    private String nationality;
    private Integer racingNumber;

    @OneToMany
    @JoinColumn(name = "tblDriverMemberid")
    private List<BankAccount> listBankAccount = new ArrayList<>();
}
