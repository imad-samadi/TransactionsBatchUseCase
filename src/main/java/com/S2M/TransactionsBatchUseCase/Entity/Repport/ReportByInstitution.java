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
@Table(name = "REPORT_BY_INSTITUTION")
public class ReportByInstitution extends AbstractReport {

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

    @Column(name = "PEER_INST_ID") // Changed from RECEIV_INST_ID for clarity if it's always a peer
    private String peerInstId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SETTLEMENT_REPORT_ID", nullable = false)
    @ToString.Exclude
    private SettlementReport settlementReport;
}
