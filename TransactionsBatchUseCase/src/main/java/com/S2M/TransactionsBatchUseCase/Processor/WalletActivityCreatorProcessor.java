package com.S2M.TransactionsBatchUseCase.Processor;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.WalletActivityReport;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.util.Date;
@RequiredArgsConstructor
public class WalletActivityCreatorProcessor implements ItemProcessor<String, WalletActivityReport> {

    private final String sessionId;
    private final String centerId;
    private final Date sessionDate;

    @Override
    public WalletActivityReport process(String currency) {

        WalletActivityReport war = new WalletActivityReport();
        war.setSessionId(sessionId);
        war.setCenterId(centerId);
        war.setActivityReportCurrency(currency);
        war.setSessionDate(sessionDate);
        return war;
    }
}
