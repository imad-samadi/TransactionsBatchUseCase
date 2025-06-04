package com.S2M.TransactionsBatchUseCase.Writer;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.WalletActivityReport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.util.Assert;

@Slf4j
public class WalletActivityReportPersistingWriter implements ItemWriter<WalletActivityReport>, InitializingBean {
    private final EntityManagerFactory entityManagerFactory;

    public WalletActivityReportPersistingWriter(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(entityManagerFactory, "EntityManagerFactory must be provided");
    }

    @Override
    public void write(Chunk<? extends WalletActivityReport> chunk) throws Exception {
        EntityManager em = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
        if (em == null) {
            throw new IllegalStateException("Transactional EntityManager not found.");
        }

        for (WalletActivityReport war : chunk.getItems()) {
            if (war == null) continue;

            if (war.getId() == null) {
                log.debug("Persisting new WalletActivityReport for currency: {}", war.getActivityReportCurrency());
                em.persist(war);
            } else {
                log.debug("Merging existing WalletActivityReport ID: {} for currency: {}", war.getId(), war.getActivityReportCurrency());
                em.merge(war);
            }
        }

        em.flush();
        log.debug("Flushed EntityManager after persisting/merging WalletActivityReport(s).");
    }
}
