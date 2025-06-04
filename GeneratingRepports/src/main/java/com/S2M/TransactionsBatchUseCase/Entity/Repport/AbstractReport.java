package com.S2M.TransactionsBatchUseCase.Entity.Repport;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@MappedSuperclass
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public abstract class AbstractReport {


    @Column(name = "TRX_CURR")
    private String transactionCurrency; //D
    @Column(name = "TRX_COUNT")
    private Integer transactionCount; //D



    @Column(name = "TRX_TOTAL_AMOUNT")
    private BigDecimal transactionTotalAmount; // I guess this the NET Related to the sign !!!!!!!
    @Column(name = "TRX_TOTAL_SIGN")
    private String transactionTotalSign;//D



    @Column(name = "CREDIT_TRX_COUNT")
    private Integer creditTrxCount; //D
    @Column(name = "CREDIT_TRX_TOTAL_AMOUNT")
    private BigDecimal creditTrxTotalAmount; //D

    @Column(name = "D_TRX_COUNT")
    private Integer debitTrxCount; //D
    @Column(name = "D_TRX_TOTAL_AMOUNT") // fix the column name
    private BigDecimal debitTrxTotalAmount; //D


    @Column(name = "C_INTER_FEE_COUNT")
    private Integer creditInterFeeCount;

    @Column(name = "C_INTER_FEE_TOTAL_AMOUNT")
    private BigDecimal creditInterFeeTotalAmount;

    @Column(name = "D_INTER_FEE_COUNT")
    private Integer debitInterFeeCount;

    @Column(name = "D_INTER_FEE_TOTAL_AMOUNT")
    private BigDecimal debitInterFeeTotalAmount;

}
