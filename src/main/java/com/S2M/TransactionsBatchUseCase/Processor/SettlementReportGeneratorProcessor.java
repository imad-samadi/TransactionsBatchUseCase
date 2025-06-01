package com.S2M.TransactionsBatchUseCase.Processor;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.SettlementReport;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.Trasaction.Transaction;
import com.S2M.TransactionsBatchUseCase.Service.ReportCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class SettlementReportGeneratorProcessor implements ItemProcessor<List<Transaction>, SettlementReport> {

    private final ReportCalculationService calculationService;
    private final String sessionId;
    private final String centerId;
    private final Date sessionDate;
    private final String processingCurrency;
    private final String processingInstitutionId;

    @Override
    public SettlementReport process(List<Transaction> transactions) { // Removed 'throws Exception' for cleaner interface
        if (transactions == null || transactions.isEmpty()) {
            log.debug("No transactions to process for SettlementReport (Session: {}, Center: {}, Currency: {}, Institution: {})",
                    sessionId, centerId, processingCurrency, processingInstitutionId);
            return null; // Spring Batch will skip writing a null item
        }
       log.info("Processing {} transactions for SettlementReport (Session: {}, Center: {}, Currency: {}, Institution: {})",
                transactions.size(), sessionId, centerId, processingCurrency, processingInstitutionId);

        try {

            return calculationService.generateSettlementReportForInstitutionCurrency(
                    transactions,
                    processingInstitutionId,
                    processingCurrency,
                    sessionId,
                    centerId,
                    sessionDate
            );
        } catch (Exception e) {
            log.error("Error generating settlement report for Inst: {}, Curr: {}, Sess: {}, Center: {}. Error: {}",
                    processingInstitutionId, processingCurrency, sessionId, centerId, e.getMessage(), e);

            throw new RuntimeException("Failed to generate settlement report for Inst: " + processingInstitutionId +
                    ", Curr: " + processingCurrency, e);
        }
    }
}
