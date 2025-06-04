package com.S2M.TransactionsBatchUseCase.Entity.Repport;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
@Table(name = "REPORT_BY_INST_AND_TRX_TYPE")
public class ReportByInstitutionAndTrxType extends AbstractReport {  // Inherit common fields

    private static final String TRX_REPORT_SEQ = "TRX_REPORT_SEQ";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = TRX_REPORT_SEQ)
    @SequenceGenerator(name = TRX_REPORT_SEQ, sequenceName = TRX_REPORT_SEQ, allocationSize = 1)
    private Long id;



    @Column(name = "RECEIV_INST_ID")
    private String peerInstId;

    // Common across reports but not in AbstractReport
    @Column(name = "TRX_TYPE")
    private String transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SETTLEMENT_REPORT_ID")
    @ToString.Exclude
    private SettlementReport settlementReport;
}
