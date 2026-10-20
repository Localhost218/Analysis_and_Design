package com.ngtukien.demo.model;

import com.ngtukien.demo.model.enums.CooperationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sponsor extends Member {
    private String name;
    private String taxCode;
    private String field;
    private String address;
    private String email;
    private String tel;
    private CooperationStatus status = CooperationStatus.ACTIVE;

    public String getDisplayName() {
        return name;
    }
}