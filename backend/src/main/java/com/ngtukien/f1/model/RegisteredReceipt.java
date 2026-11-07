package com.ngtukien.f1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Kết quả thi đấu của một tay đua tại một chặng (kèm tiền thưởng). */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tblRegisteredReceipt")
public class RegisteredReceipt {
    public static final String PAID = "Đã trả";
    public static final String UNPAID = "Chưa trả";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tblRegisteredDriverid")
    private RegisteredDriver registeredDriver;

    @ManyToOne
    @JoinColumn(name = "tblRaceid")
    @JsonIgnoreProperties("listRegisteredReceipt")
    private Race race;

    private Integer rank;
    private Integer score;
    private LocalDateTime finishedTime;
    private Integer completedLap;
    private Integer reward;
    private String completedStatus;

    @ManyToOne
    @JoinColumn(name = "tblOrganizerMemberid")
    private Organizer organizer;

    @ManyToOne
    @JoinColumn(name = "tblRewardInvoiceid")
    @JsonIgnoreProperties("listRegisteredReceipt")
    private RewardInvoice rewardInvoice;

    /** Không lưu cột riêng: đã có hóa đơn nghĩa là đã trả thưởng. */
    @JsonProperty("paymentStatus")
    public String getPaymentStatus() {
        return rewardInvoice == null ? UNPAID : PAID;
    }
}
