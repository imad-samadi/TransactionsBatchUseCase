package com.S2M.TransactionsBatchUseCase.Reader;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.Trasaction.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Repository;

import java.util.List;
@RequiredArgsConstructor
@Slf4j
public class TransactionsForInstitutionCurrencyReader implements ItemReader<List<Transaction>> {

    private final EntityManagerFactory entityManagerFactory;
    private final String sessionId; // Job Parameter
    private final String centerId;  // Job Parameter
    private final String processingCurrency;    // From Partition Context
    private final String processingInstitutionId; // From Partition Context
    private boolean dataFetched = false; // Ensures the reader is called only once per step execution for its partition




    @Override
    public List<Transaction> read() {
        if (dataFetched) {
            // This reader is designed to return its single item (the list) once.
            // Subsequent calls within the same step execution (for this partition) should return null.
            return null;
        }

        EntityManager em = entityManagerFactory.createEntityManager();
       // log.debug("Reading transactions for - SessionId: {}, CenterId: {}, ProcessingCurrency: {}, ProcessingInstitutionId: {}",
              //  sessionId, centerId, processingCurrency, processingInstitutionId);
        try {

            String jpql = "SELECT t FROM Transaction t LEFT JOIN FETCH t.feeInfo " +
                    "WHERE t.sessionId = :sessionId AND t.centerId = :centerId " +
                    "AND t.transactionCurrency = :currency " +
                    "AND (t.debitorInstitutionId = :institutionId OR t.creditorInstitutionId = :institutionId)";

            TypedQuery<Transaction> query = em.createQuery(jpql, Transaction.class);
            query.setParameter("sessionId", sessionId);
            query.setParameter("centerId", centerId);
            query.setParameter("currency", processingCurrency);
            query.setParameter("institutionId", processingInstitutionId);

            List<Transaction> transactions = query.getResultList();
            this.dataFetched = true; // Mark data as fetched so next call returns null

            if (transactions.isEmpty()) {
                log.debug("No transactions found for - SessionId: {}, CenterId: {}, ProcessingCurrency: {}, ProcessingInstitutionId: {}",
                        sessionId, centerId, processingCurrency, processingInstitutionId);
                return null; // Return null if no transactions, ItemProcessor will not be called.
            }

          //  log.info("Fetched {} transactions for - SessionId: {}, CenterId: {}, ProcessingCurrency: {}, ProcessingInstitutionId: {}",
                 //   transactions.size(), sessionId, centerId, processingCurrency, processingInstitutionId);
            return transactions; // Return the entire list as a single item.
        } catch (Exception e) {

            log.error("Error reading transactions for - SessionId: {}, CenterId: {}, ProcessingCurrency: {}, ProcessingInstitutionId: {}",
                    sessionId, centerId, processingCurrency, processingInstitutionId, e);
            throw new RuntimeException("Failed to read transactions for partition", e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
