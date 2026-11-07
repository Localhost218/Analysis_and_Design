package com.ngtukien.f1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tblSponsor")
@PrimaryKeyJoinColumn(name = "tblMemberid")
public class Sponsor extends Member {
    private String name;
    private String taxCode;
    private String field;
    private String address;
    private String email;
    private String tel;
    private String status;

    /** Danh sách hợp đồng ĐÃ KÝ (báo cáo tổng hợp mục 7). */
    @OneToMany(mappedBy = "sponsor")
    @SQLRestriction("status = 'Đã ký'")
    @OrderBy("signedDate DESC")
    @JsonIgnoreProperties("sponsor")
    private List<Contract> listContract = new ArrayList<>();
}
