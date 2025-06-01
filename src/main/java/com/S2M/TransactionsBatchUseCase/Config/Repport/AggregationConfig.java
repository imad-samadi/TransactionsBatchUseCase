package com.S2M.TransactionsBatchUseCase.Config.Repport;

import com.S2M.TransactionsBatchUseCase.Config.Partition.CurrencyForAggregationPartitioner;
import com.S2M.TransactionsBatchUseCase.Config.Partition.DetermineCurrencyInstitutionPairsTasklet;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.SettlementReport;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.WalletActivityReport;
import com.S2M.TransactionsBatchUseCase.Listeners.*;
import com.S2M.TransactionsBatchUseCase.Processor.WalletActivityReportBuilderProcessor;
import com.S2M.TransactionsBatchUseCase.Reader.SettlementReportsForCurrencyAggregatorReader;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
@Configuration
public class AggregationConfig {


    @Bean
    @StepScope
    public Partitioner currencyForAggregationPartitioner(
            @Value("#{jobExecutionContext['" + DetermineCurrencyInstitutionPairsTasklet.DISTINCT_CURRENCIES_KEY + "']}") Set<String> distinctCurrencies) {
        return new CurrencyForAggregationPartitioner(distinctCurrencies != null ? distinctCurrencies : Collections.emptySet());
    }

    @Bean
    @StepScope
    public ItemReader<List<SettlementReport>> settlementReportsForCurrencyAggregatorReader(
            EntityManagerFactory entityManagerFactory, // Autowired
            @Value("#{jobParameters['sessionId']}") String sessionId,
            @Value("#{stepExecutionContext['aggregationCurrency']}") String aggregationCurrency) {
        return new SettlementReportsForCurrencyAggregatorReader(entityManagerFactory, sessionId, aggregationCurrency);
    }
    @Bean
    @StepScope
    public ItemProcessor<List<SettlementReport>, WalletActivityReport> walletActivityReportBuilderProcessor(
            @Value("#{jobParameters['sessionId']}") String sessionId,
            @Value("#{jobParameters['centerId']}") String centerId,
            @Value("#{jobParameters['sessionDate']}") Date sessionDate,
            @Value("#{stepExecutionContext['aggregationCurrency']}") String aggregationCurrency) {
        return new WalletActivityReportBuilderProcessor(sessionId, centerId, sessionDate, aggregationCurrency);
    }

    @Bean
    public JpaItemWriter<WalletActivityReport> walletActivityReportWriter(EntityManagerFactory entityManagerFactory) { // Autowired
        return new JpaItemWriterBuilder<WalletActivityReport>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step workerWalletActivityAggregationStep(JobRepository jobRepository,
                                                    PlatformTransactionManager transactionManager,
                                                    ItemReader<List<SettlementReport>> settlementReportsForCurrencyAggregatorReader, // From this config
                                                    ItemProcessor<List<SettlementReport>, WalletActivityReport> walletActivityReportBuilderProcessor, // From this config
                                                    JpaItemWriter<WalletActivityReport> walletActivityReportWriter) { // From this config
        return new StepBuilder("workerWalletActivityAggregationStep", jobRepository)
                .<List<SettlementReport>, WalletActivityReport>chunk(1, transactionManager)
                .reader(settlementReportsForCurrencyAggregatorReader)
                .processor(walletActivityReportBuilderProcessor)
                .writer(walletActivityReportWriter)
                //.listener(new LoggingStepListener())
                .listener(new WalletActivityReportWriteListener())

                .build();
    }

    @Bean
    public Step aggregateReportsAndCreateWalletActivityManagerStep(JobRepository jobRepository,
                                                                   Partitioner currencyForAggregationPartitioner, // From this config
                                                                   Step workerWalletActivityAggregationStep,   // From this config
                                                                   TaskExecutor partitionTaskExecutor) {      // From MainBatchJobConfig
        return new StepBuilder("aggregateReportsAndCreateWalletActivityManagerStep", jobRepository)
                .partitioner("workerWalletActivityAggregationStep", currencyForAggregationPartitioner)
                .step(workerWalletActivityAggregationStep)
                .gridSize(50)
                .taskExecutor(partitionTaskExecutor)
                .build();
    }

}
