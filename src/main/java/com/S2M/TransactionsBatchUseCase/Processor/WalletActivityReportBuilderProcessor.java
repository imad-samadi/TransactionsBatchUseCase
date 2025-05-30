package com.S2M.TransactionsBatchUseCase.Processor;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.SettlementReport;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.WalletActivityReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.util.Date;
import java.util.List;

@Slf4j
public class WalletActivityReportBuilderProcessor implements ItemProcessor<List<SettlementReport>, WalletActivityReport> {

    private final String sessionId;
    private final String centerId;
    private final Date sessionDate;
    private final String aggregationCurrency; // From partition context

    public WalletActivityReportBuilderProcessor(String sessionId, String centerId, Date sessionDate, String aggregationCurrency) {
        this.sessionId = sessionId;
        this.centerId = centerId;
        this.sessionDate = sessionDate;
        this.aggregationCurrency = aggregationCurrency;
    }

    @Override
    public WalletActivityReport process(List<SettlementReport> settlementReports) {
        if (settlementReports == null || settlementReports.isEmpty()) {
            log.debug("No SettlementReports to aggregate for WalletActivityReport (Currency: {})", aggregationCurrency);
            return null;
        }
        log.info("Aggregating {} SettlementReports into WalletActivityReport for Currency: {}",
                settlementReports.size(), aggregationCurrency);

        WalletActivityReport war = new WalletActivityReport();
        war.setSessionId(sessionId);
        war.setCenterId(centerId);
        war.setSessionDate(sessionDate);
        war.setActivityReportCurrency(aggregationCurrency);
        war.setReports(settlementReports);

        // Establish the bidirectional relationship for JPA persistence
        for (SettlementReport sr : settlementReports) {
            sr.setWalletActivityReport(war);
        }
        return war;
    }
}
