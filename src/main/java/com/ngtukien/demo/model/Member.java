package com.ngtukien.demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Member implements java.io.Serializable {
    private Integer id;
    private String username;
    private String password;

    public String getDisplayName() {
        return username;
    }
}