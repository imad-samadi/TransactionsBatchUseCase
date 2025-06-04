package com.S2M.CSVImport.Entity.Trasaction;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "TX_FEE_INFO")
public class FeeInfo {

    /** FeeInfo Entity Sequence Name. */
    private static final String FEE_INFO_SEQ = "FEE_INFO_SEQ";

    /** id. pk */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = FEE_INFO_SEQ)
    @SequenceGenerator(name = FEE_INFO_SEQ, sequenceName = FEE_INFO_SEQ, allocationSize = 1)
    private Long id;

    /** The fee amount. */
    @Column(name = "FEE_AMOUNT")
    private BigDecimal feeAmount;

    /** The fee code. */
    @Column(name = "FEE_CODE")
    private String feeCode;

    /** The fee currency. */
    @Column(name = "FEE_CURRENCY")
    private String feeCurrency;

    /** The fee description. */
    @Column(name = "FEE_DESC")
    private String feeDescription;

    /** The fee sign. */
    @Column(name = "FEE_SIGN")
    private String feeSign;

    /** The fee type. */
    @Column(name = "FEE_TYPE")
    private String feeType;

    @Column(name = "INST_REF")
    public String institutionReference;

    @Column(name = "TX_ID")
    private Long transactionId;
}
