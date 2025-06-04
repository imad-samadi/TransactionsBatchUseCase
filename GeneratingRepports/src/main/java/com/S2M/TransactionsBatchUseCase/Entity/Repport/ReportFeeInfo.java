package com.S2M.TransactionsBatchUseCase.Entity.Repport;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "REPORT_FEE_INFO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Added for easier construction
@ToString
public class ReportFeeInfo {

    private static final String REPORT_FEE_INFO_SEQ = "REPORT_FEE_INFO_SEQ";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = REPORT_FEE_INFO_SEQ)
    @SequenceGenerator(name = REPORT_FEE_INFO_SEQ, sequenceName = REPORT_FEE_INFO_SEQ, allocationSize = 1)
    private Long id;


    @Column(name = "FEE_AMOUNT")
    private BigDecimal feeAmount;

    @Column(name = "FEE_CODE")
    private String feeCode;

    @Column(name = "FEE_CURRENCY")
    private String feeCurrency;

    @Column(name = "FEE_DESC")
    private String feeDescription;

    @Column(name = "FEE_SIGN")
    private String feeSign;

    @Column(name = "FEE_TYPE")
    private String feeType ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SETTLEMENT_REPORT_ID", nullable = false)
    @ToString.Exclude
    private SettlementReport settlementReport;
}
