package com.S2M.TransactionsBatchUseCase.Entity.Repport.Trasaction;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "TRANSACTION")
public class Transaction {

    /** Transaction Entity Sequence Name. */
    private static final String TX_SEQ = "TX_SEQ";

    /** id. pk */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = TX_SEQ)
    @SequenceGenerator(name = TX_SEQ, sequenceName = TX_SEQ, allocationSize = 1)
    private Long id ;

    /** The cut off id. */
    @Column(name = "SESSION_ID", nullable = false)
    public String sessionId;
    @Column(name = "TX_CENTER_ID", nullable = false)
    private String centerId;

    /** The fee info. */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "TX_ID")
    public Set<FeeInfo> feeInfo ;

    /** The institution id. */
    @Column(name = "DEBIT_INST_REF", nullable = false)
    public String debitorInstitutionId;
    /** The cred institution id. */
    @Column(name = "CRED_INST_ID")
    public String creditorInstitutionId;






    /** The transaction amount. */
    @Column(name = "TX_AMOUNT", nullable = false)
    public BigDecimal transactionAmount;

    /** The transaction currency. */
    @Column(name = "TX_CURRENCY", nullable = false)
    public String transactionCurrency;

    /** The transaction sign. */
    @Column(name = "TX_SIGN", nullable = false)
    public String transactionSign;

    /** The transaction type. */
    @Column(name = "TX_TYPE", nullable = false)
    public String transactionType ;

    @Column(name = "TX_STATUS")
    private String transactionStatus;

    @Column(name = "TX_STATUS_DATE")
    private Date transactionStatusDate;




    @Column(name = "DEBIT_ACC_TYPE")
    private String debitorAccType;

    @Column(name = "CRED_ACC_TYPE")
    private String creditorAccType   ;


    @Column(name = "CRED_TYPE")
    private String creditorType ;

    @Column(name = "DEBIT_TYPE")
    private String debitorType ;



    @Column(name = "REQ_TIMESTAMP")
    private String reqTimestamp;

    @Column(name = "CRED_ACC_NUMBER")
    private String creditorAccNumber;

    @Column(name = "DEBIT_ACC_NUMBER")
    private String debitorAccNumber;
    /** The dph reference. */
    @Column(name = "DPH_REFERENCE")
    public String dphReference;

    @Column(name = "HOST_REFERENCE")
    private String hostReference;

    @Column(name = "AGREEMENT_REFERENCE")
    private String agreementReference;

    /** The origin phone number. */
    @Column(name = "DEBIT_PHONE_NUMBER")
    public String debitorPhoneNumber;

}
