package com.ngtukien.f1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tblTeam")
@PrimaryKeyJoinColumn(name = "tblMemberid")
public class Team extends Member {
    private String name;
    private String manufacturer;
    private String description;

    @OneToMany(mappedBy = "team")
    @JsonIgnoreProperties("team")
    private List<RegisteredDriver> listRegisteredDriver = new ArrayList<>();
}
