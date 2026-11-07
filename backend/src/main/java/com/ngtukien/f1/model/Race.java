package com.ngtukien.f1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tblRace")
public class Race {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private Integer lapCount;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private Integer basePrize;

    @ManyToOne
    @JoinColumn(name = "tblSeasonid")
    @JsonIgnoreProperties("listRace")
    private Season season;

    @OneToMany(mappedBy = "race")
    @OrderBy("rank ASC")  // PostgreSQL: NULL (không hoàn thành) xếp cuối
    @JsonIgnoreProperties("race")
    private List<RegisteredReceipt> listRegisteredReceipt = new ArrayList<>();
}
