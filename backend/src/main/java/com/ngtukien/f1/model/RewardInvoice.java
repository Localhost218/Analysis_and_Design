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
@Table(name = "tblRewardInvoice")
public class RewardInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String barcode;
    private LocalDateTime paidAt;
    private Integer amount;
    private String paymentMethod;
    private String status;

    @ManyToOne
    @JoinColumn(name = "tblOrganizerMemberid")
    private Organizer organizer;

    @OneToMany(mappedBy = "rewardInvoice")
    @JsonIgnoreProperties("rewardInvoice")
    private List<RegisteredReceipt> listRegisteredReceipt = new ArrayList<>();
}
