package com.S2M.TransactionsBatchUseCase.Entity.Repport;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "SETTLEMENT_REPORT")
public class SettlementReport {

    private static final String SETTLEMENT_REPORT_SEQ = "SETTLEMENT_REPORT_SEQ";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SETTLEMENT_REPORT_SEQ)
    @SequenceGenerator(name = SETTLEMENT_REPORT_SEQ, sequenceName = SETTLEMENT_REPORT_SEQ, allocationSize = 1)
    private Long id;

    @Column(name = "INST_ID")
    private String institutionId;

    @Column(name = "SESSION_ID")
    private String sessionId;

    @Column(name = "SESSION_GENERATION_DATE")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date sessionDate;

    // Link to parent WalletActivityReport
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "W_ACTIVITY_REPORT_ID")
    @ToString.Exclude // Avoid circular toString
    private WalletActivityReport walletActivityReport;


    // Keep this one as a List if its order is most important and you want to eager fetch it
    @OneToMany(mappedBy = "settlementReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE) // This makes it eager by default with Hibernate
    private List<ReportByTrxType> reportByTrxType;

    // Change these to Set
    @OneToMany(mappedBy = "settlementReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<ReportByInstitution> reportByInstitution = new HashSet<>(); // Initialize

    @OneToMany(mappedBy = "settlementReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<ReportByInstitutionAndTrxType> reportByInstitutionAndTrxTypeResponse = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "GLOBAL_REPORT_ID", referencedColumnName = "id")
    private GlobalReport globalReport;

    //add the fees
}
