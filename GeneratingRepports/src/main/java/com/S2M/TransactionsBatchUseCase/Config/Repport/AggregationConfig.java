package com.S2M.TransactionsBatchUseCase.Config.Repport;

import com.S2M.TransactionsBatchUseCase.Config.Partition.DetermineCurrencyInstitutionPairsTasklet;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.WalletActivityReport;
import com.S2M.TransactionsBatchUseCase.Processor.WalletActivityCreatorProcessor;
import com.S2M.TransactionsBatchUseCase.Reader.DistinctCurrencyReader;
import com.S2M.TransactionsBatchUseCase.Writer.WalletActivityReportPersistingWriter;
import com.S2M.TransactionsBatchUseCase.Repo.WalletActivityReportRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
@Configuration
@RequiredArgsConstructor
public class AggregationConfig {


    private final WalletActivityReportRepository walletRepo ;

    @Bean("walletActivityRepositoryWriter")
    public WalletActivityReportPersistingWriter walletActivityReportPersistenceWriter(
            EntityManagerFactory entityManagerFactory) {
        return new WalletActivityReportPersistingWriter(entityManagerFactory);
    }
    @Bean("settlementLinkJdbcWriter")

    public JdbcBatchItemWriter<WalletActivityReport> settlementLinkJdbcWriter(
            DataSource dataSource,
            // Use the specific SqlParameterSourceProvider for clarity
            @Qualifier("walletActivityReportSqlParameterSourceProviderForLinking") // Give it a distinct name
            ItemSqlParameterSourceProvider<WalletActivityReport> sqlParameterSourceProvider
    ) {
        JdbcBatchItemWriter<WalletActivityReport> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setItemSqlParameterSourceProvider(sqlParameterSourceProvider);
        writer.setSql("""
            UPDATE SETTLEMENT_REPORT sr
               SET W_ACTIVITY_REPORT_ID = :warId  -- Parameter name from your provider
             WHERE sr.SESSION_ID = :sessionId      -- Parameter name from your provider
               AND sr.W_ACTIVITY_REPORT_ID IS NULL
               AND EXISTS (
                   SELECT 1 FROM GLOBAL_REPORT gr
                   WHERE gr.ID = sr.GLOBAL_REPORT_ID
                     AND gr.NET_SETTLEMENT_CURR = :activityReportCurrency -- Parameter from provider
               )
        """);
        return writer;
    }
    @Bean("walletActivityReportSqlParameterSourceProviderForLinking")

    public ItemSqlParameterSourceProvider<WalletActivityReport> walletActivityReportSqlParameterSourceProviderForLinking() {
        // This provider will extract :warId, :sessionId, :activityReportCurrency from the WalletActivityReport object
        return war -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            if (war != null && war.getId() != null) {
                params.addValue("warId", war.getId());
                params.addValue("sessionId", war.getSessionId());
                params.addValue("activityReportCurrency", war.getActivityReportCurrency());
                // params.addValue("centerId", war.getCenterId()); // Only add if SQL uses :centerId
            }
            return params;
        };
    }
    @Bean("compositeWriter")
    public CompositeItemWriter<WalletActivityReport> compositeWriter(DataSource dataSource,EntityManagerFactory entityManagerFactory,
                                                                     @Qualifier("settlementLinkJdbcWriter") JdbcBatchItemWriter<WalletActivityReport> writer
                                                                     ) {
        CompositeItemWriter<WalletActivityReport> composite = new CompositeItemWriter<>();
        composite.setDelegates(List.of(
                walletActivityReportPersistenceWriter(entityManagerFactory),
                writer
        ));
        return composite;
    }
    @Bean("DistinctCurrencyReader")
@StepScope
    public DistinctCurrencyReader distinctCurrencyReaderForAggregation(
                                                                        @Value("#{jobExecutionContext['" + DetermineCurrencyInstitutionPairsTasklet.DISTINCT_CURRENCIES_KEY + "']}") Set<String> distinctCurrenciesFromJobContext
    ) {
        return new DistinctCurrencyReader(distinctCurrenciesFromJobContext != null ? distinctCurrenciesFromJobContext : Collections.emptySet());
    }

    @Bean
    @StepScope
    public WalletActivityCreatorProcessor walletActivityCreatorProcessor(

            @Value("#{jobParameters['sessionId']}") String sessionId,
            @Value("#{jobParameters['centerId']}") String centerId,
            @Value("#{jobParameters['sessionDate']}") Date sessionDate) {
        return new WalletActivityCreatorProcessor(sessionId, centerId, sessionDate);
    }
    @Bean("linkReportsToWalletActivityStep")
    public Step linkReportsToWalletActivityStep(JobRepository jobRepository,
                                                PlatformTransactionManager transactionManager,
                                             @Qualifier("DistinctCurrencyReader")   ItemReader<String> distinctCurrencyReaderForAggregation,
                                                ItemProcessor<String, WalletActivityReport> walletActivityCreatorProcessor,
                                                @Qualifier("compositeWriter") CompositeItemWriter<WalletActivityReport> compositeWriter) {
        return new StepBuilder("linkReportsToWalletActivityStep", jobRepository)
                .<String, WalletActivityReport>chunk(1, transactionManager)
                .reader(distinctCurrencyReaderForAggregation)
                .processor(walletActivityCreatorProcessor)
                .writer(compositeWriter)
                .build();
    }




}
