package com.ngtukien.f1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tblRegisteredDriver")
public class RegisteredDriver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tblDriverMemberid")
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "tblTeamMemberid")
    @JsonIgnoreProperties("listRegisteredDriver")
    private Team team;
}
