package com.ngtukien.f1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tblOrganizer")
@PrimaryKeyJoinColumn(name = "tblMemberid")
public class Organizer extends Member {
    private String fullName;
    private String address;
    private LocalDate dateOfBirth;
    private String email;
    private String tel;
    private String role;
}
