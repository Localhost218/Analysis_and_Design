package com.ngtukien.demo.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Organizer extends Member {
    private String fullName;
    private String address;
    private LocalDateTime dateOfBirth;
    private String email;

    public String getDisplayName() {
        return fullName;
    }
}