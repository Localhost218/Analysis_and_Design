package com.ngtukien.demo.model;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Contract {
    private Integer id;
    private String barcode;
    private Sponsor sponsor;
    private Organizer organizer;
    private LocalDate signedDate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status;
}