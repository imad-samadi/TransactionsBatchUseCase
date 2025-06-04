package com.S2M.TransactionsBatchUseCase.Entity.Repport;
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
@Table(name = "GLOBAL_REPORT")
public class GlobalReport {

    /** Transaction Entity Sequence Name. */
    private static final String GLOBAL_REPORT_SEQ = "GLOBAL_REPORT_SEQ";

    /** id. pk */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GLOBAL_REPORT_SEQ)
    @SequenceGenerator(name = GLOBAL_REPORT_SEQ, sequenceName = GLOBAL_REPORT_SEQ, allocationSize = 1)
    private Long id;

    @Column(name = "NET_SETTLEMENT_CURR")
    private String netSettlementCurren	; //Done
    @Column(name = "TRX_COUNT")
    private Integer transactionCount; //Done


    @Column(name = "NET_SETTLEMENT_AMOUNT")
    private BigDecimal netSettlementAmount;

    @Column(name = "NET_SETTLEMENT_SIGN")
    private String netSettlementSign;



    @Column(name = "C_TRX_COUNT")
    private Integer creditTrxCount; //Done

    @Column(name = "C_TRX_AMOUNT")
    private BigDecimal creditTrxAmount; //Done

    @Column(name = "D_TRX_COUNT")
    private Integer debitTrxCount; //D

    @Column(name = "D_TRX_AMOUNT")
    private BigDecimal debitTrxAmount; //D



    //Account Type
    @Column(name = "ACC_DB_TRX_COUNT")
    private Integer accountDebitTrxCount;//D
    @Column(name = "ACC_DB_TRX_AMOUNT")
    private BigDecimal accountDebitTrxAmount; //D
    @Column(name = "ACC_CR_TRX_COUNT")
    private Integer accountCreditTrxCount; //done
    @Column(name = "ACC_CR_TRX_AMOUNT")
    private BigDecimal accountCreditTrxAmount;//done

    //Wallet Type
    @Column(name = "WAL_DB_TRX_COUNT")
    private Integer walletDebitTrxCount;//D
    @Column(name = "WAL_DB_TRX_AMOUNT")
    private BigDecimal walletDebitTrxAmount;//D
    @Column(name = "WAL_CR_TRX_COUNT")
    private Integer walletCreditTrxCount; //done
    @Column(name = "WAL_CR_TRX_AMOUNT")
    private BigDecimal walletCreditTrxAmount; //done

    //Card Type (not found in the old project !!!!!)
    @Column(name = "CRD_DB_TRX_COUNT")
    private Integer cardDebitTrxCount;//D
    @Column(name = "CRD_DB_TRX_AMOUNT")
    private BigDecimal cardDebitTrxAmount;//D
    @Column(name = "CRD_CR_TRX_COUNT")
    private Integer cardCreditTrxCount; //D
    @Column(name = "CRD_CR_TRX_AMOUNT")
    private BigDecimal cardCreditTrxAmount; //D



    @Column(name = "GROSS_TRANS_AMOUNT")
    private BigDecimal grossSettlementAmount;

    @Column(name = "GROSS_TRANS_SIGN")
    private String grossSettlementSign ;








    @Column(name = "GROSS_INTER_FEE_AMOUNT")
    private BigDecimal interchangeFeeTotalAmount;

    @Column(name = "GROSS_INTER_FEE_SIGN")
    private String interchangeFeeSign;


    @Column(name = "C_INTER_FEE_COUNT")
    private Integer creditInterFeeCount; //D
    @Column(name = "C_INTER_FEE_TOTAL_AMOUNT")
    private BigDecimal creditInterFeeTotalAmount; //D

    @Column(name = "D_INTER_FEE_COUNT")
    private Integer debitInterFeeCount; //D

    @Column(name = "D_INTER_FEE_TOTAL_AMOUNT")
    private BigDecimal debitInterFeeTotalAmount; //D


}
