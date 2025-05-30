package com.S2M.TransactionsBatchUseCase.Reader;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.SettlementReport;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

import java.util.List;
@Slf4j
public class SettlementReportsForCurrencyAggregatorReader implements ItemReader<List<SettlementReport>> {

    private final EntityManagerFactory entityManagerFactory;
    private final String sessionId;
    private final String aggregationCurrency; // From partition context
    private boolean dataFetched = false;

    public SettlementReportsForCurrencyAggregatorReader(EntityManagerFactory entityManagerFactory,
                                                        String sessionId, String aggregationCurrency) {
        this.entityManagerFactory = entityManagerFactory;
        this.sessionId = sessionId;
        this.aggregationCurrency = aggregationCurrency;
    }

    @Override
    public List<SettlementReport> read() {
        if (dataFetched) {
            return null; // Read only once per partition execution
        }
        EntityManager em = entityManagerFactory.createEntityManager();
        log.info("Reading SettlementReports for aggregation. SessionId: {}, Currency: {}", sessionId, aggregationCurrency);
        try {

            String jpql = "SELECT sr FROM SettlementReport sr " +
                    "LEFT JOIN FETCH sr.globalReport gr " +
                    "LEFT JOIN FETCH sr.reportByTrxType " +
                    "LEFT JOIN FETCH sr.reportByInstitution " +
                    "LEFT JOIN FETCH sr.reportByInstitutionAndTrxTypeResponse " +
                    "WHERE sr.sessionId = :sessionId " +
                    "AND gr.netSettlementCurren = :currency " +
                    "AND sr.walletActivityReport IS NULL";

            TypedQuery<SettlementReport> query = em.createQuery(jpql, SettlementReport.class);
            query.setParameter("sessionId", sessionId);
            query.setParameter("currency", aggregationCurrency);

            List<SettlementReport> reports = query.getResultList();
            this.dataFetched = true;

            if (reports.isEmpty()) {
                log.debug("No unaggregated SettlementReports found for SessionId: {}, Currency: {}", sessionId, aggregationCurrency);
                return null; // Processor will not be called for this partition
            }
            log.info("Fetched {} SettlementReports for aggregation. Currency: {}", reports.size(), aggregationCurrency);
            return reports; // Return the list as a single item
        } catch (Exception e) {
            log.error("Error reading settlement reports for aggregation. SessionId: {}, Currency: {}. Error: {}",
                    sessionId, aggregationCurrency, e.getMessage(), e);
            throw new RuntimeException("Failed to read settlement reports for aggregation", e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
