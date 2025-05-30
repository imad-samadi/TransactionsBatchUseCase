package com.S2M.TransactionsBatchUseCase.Entity.Repport;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.format.annotation.DateTimeFormat;


import java.util.Date;
import java.util.List;
@Entity
@Getter
@Setter
@Table(name = "WALLET_ACTIVITY_REPORT")
public class WalletActivityReport {

    private static final String W_ACTIVITY_REPORT_SEQ = "W_ACTIVITY_REPORT_SEQ";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = W_ACTIVITY_REPORT_SEQ)
    @SequenceGenerator(name = W_ACTIVITY_REPORT_SEQ, sequenceName = W_ACTIVITY_REPORT_SEQ, allocationSize = 1)
    private Long id;

    @Column(name = "CENTER_ID")
    private String centerId;

    @Column(name = "REP_CURR")
    private String activityReportCurrency;

    @Column(name = "SESSION_ID")
    private String sessionId;

    @Column(name = "SESSION_DATE")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date sessionDate;

    @OneToMany(mappedBy = "walletActivityReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE) // Eagerly fetch for simplicity in batch writing
    private List<SettlementReport> reports;


}
