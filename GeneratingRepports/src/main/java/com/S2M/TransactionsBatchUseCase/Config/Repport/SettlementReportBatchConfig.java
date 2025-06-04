package com.S2M.TransactionsBatchUseCase.Config.Repport;

import com.S2M.TransactionsBatchUseCase.Config.Partition.CurrencyInstitutionPartitioner;
import com.S2M.TransactionsBatchUseCase.Config.Partition.DetermineCurrencyInstitutionPairsTasklet;
import com.S2M.TransactionsBatchUseCase.DTO.CurrencyInstitutionPair;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.SettlementReport;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.Trasaction.Transaction;
import com.S2M.TransactionsBatchUseCase.Listeners.LoggingSkipListener;
import com.S2M.TransactionsBatchUseCase.Listeners.LoggingStepListener;
import com.S2M.TransactionsBatchUseCase.Listeners.PerThreadCountingWriteListener;
import com.S2M.TransactionsBatchUseCase.Listeners.SimpleChunkListener;
import com.S2M.TransactionsBatchUseCase.Processor.SettlementReportGeneratorProcessor;
import com.S2M.TransactionsBatchUseCase.Reader.TransactionsForInstitutionCurrencyReader;
import com.S2M.TransactionsBatchUseCase.Repo.SettlementReportRepository;
import com.S2M.TransactionsBatchUseCase.Service.ReportCalculationService;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Configuration
public class SettlementReportBatchConfig {





    @Bean
    public Step determineWorkUnitsStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       DetermineCurrencyInstitutionPairsTasklet tasklet) {
        return new StepBuilder("determineWorkUnitsStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public DetermineCurrencyInstitutionPairsTasklet determineCurrencyInstitutionPairsTasklet(
            DataSource dataSource, // Autowired by Spring
            @Value("#{jobParameters['sessionId']}") String sessionId,
            @Value("#{jobParameters['centerId']}") String centerId) {
        return new DetermineCurrencyInstitutionPairsTasklet(dataSource, sessionId, centerId);
    }

    @Bean
    @StepScope
    public Partitioner currencyInstitutionPartitioner(
            @Value("#{jobExecutionContext['" + DetermineCurrencyInstitutionPairsTasklet.CURRENCY_INSTITUTION_PAIRS_KEY + "']}") Set<CurrencyInstitutionPair> pairs) {
        return new CurrencyInstitutionPartitioner(pairs != null ? pairs : Collections.emptySet());
    }

    @Bean
    @StepScope
    public ItemReader<List<Transaction>> transactionsForInstitutionCurrencyReader(
            EntityManagerFactory entityManagerFactory, // Autowired
            @Value("#{jobParameters['sessionId']}") String sessionId,
            @Value("#{jobParameters['centerId']}") String centerId,
            @Value("#{stepExecutionContext['processingCurrency']}") String processingCurrency,
            @Value("#{stepExecutionContext['processingInstitutionId']}") String processingInstitutionId) {
        return new TransactionsForInstitutionCurrencyReader(entityManagerFactory, sessionId, centerId, processingCurrency, processingInstitutionId);
    }

    @Bean
    @StepScope
    public ItemProcessor<List<Transaction>, SettlementReport> settlementReportGeneratorProcessor(
            ReportCalculationService reportCalculationService,
            @Value("#{jobParameters['sessionId']}") String sessionId,
            @Value("#{jobParameters['centerId']}") String centerId,
            @Value("#{jobParameters['sessionDate']}") Date sessionDate,
            @Value("#{stepExecutionContext['processingCurrency']}") String processingCurrency,
            @Value("#{stepExecutionContext['processingInstitutionId']}") String processingInstitutionId) {
        return new SettlementReportGeneratorProcessor(
                reportCalculationService, sessionId, centerId, sessionDate, processingCurrency, processingInstitutionId);
    }

    @Bean
    public RepositoryItemWriter<SettlementReport> settlementReportWriter(
            SettlementReportRepository settlementReportRepository
    ) {
        return new RepositoryItemWriterBuilder<SettlementReport>()
                .repository(settlementReportRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public Step generateAndSaveSettlementReportsManagerStep(JobRepository jobRepository,
                                                            Partitioner currencyInstitutionPartitioner,
                                                            @Qualifier("workerSettlementReportStep") Step workerSettlementReportStep,
                                                            TaskExecutor partitionTaskExecutor) {    // From MainBatchJobConfig
        return new StepBuilder("generateAndSaveSettlementReportsManagerStep", jobRepository)
                .partitioner("workerSettlementReportStep", currencyInstitutionPartitioner)
                .step(workerSettlementReportStep)
                .gridSize(50)
                .taskExecutor(partitionTaskExecutor)
                .build();
    }

    @Bean("workerSettlementReportStep")
    public Step workerSettlementReportStep(JobRepository jobRepository,
                                           PlatformTransactionManager transactionManager,
                                           ItemReader<List<Transaction>> transactionsForInstitutionCurrencyReader,
                                           ItemProcessor<List<Transaction>, SettlementReport> settlementReportGeneratorProcessor,
                                           RepositoryItemWriter<SettlementReport> settlementReportWriter) {
        return new StepBuilder("workerSettlementReportStep", jobRepository)
                .<List<Transaction>, SettlementReport>chunk(1, transactionManager)
                .reader(transactionsForInstitutionCurrencyReader)
                .processor(settlementReportGeneratorProcessor)
                .writer(settlementReportWriter)
                //.listener(new LoggingStepListener())
                .listener(new PerThreadCountingWriteListener())
                .build();
    }




}
