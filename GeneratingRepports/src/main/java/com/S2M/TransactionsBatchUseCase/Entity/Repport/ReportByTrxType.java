package com.S2M.TransactionsBatchUseCase.Entity.Repport;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
@Table(name = "REPORT_BY_TRX_TYPE")
public class ReportByTrxType extends AbstractReport{

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "report_trx_type_seq"
    )
    @SequenceGenerator(
            name = "report_trx_type_seq",
            sequenceName = "REPORT_BY_TRX_TYPE_SEQ",
            allocationSize = 1
    )
    private Long id;


    @Column(name = "TRX_TYPE")
    private String transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SETTLEMENT_REPORT_ID", nullable = false)
    @ToString.Exclude
    private SettlementReport settlementReport;


}
