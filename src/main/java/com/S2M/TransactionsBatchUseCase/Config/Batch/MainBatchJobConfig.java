package com.S2M.TransactionsBatchUseCase.Config.Batch;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
@Configuration
@AllArgsConstructor
public class MainBatchJobConfig {

    private final BatchProperties batchProperties;

    @Bean
    public TaskExecutor partitionTaskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(batchProperties.getCorePoolSize());
        exec.setMaxPoolSize(batchProperties.getMaxPoolSize());
        exec.setThreadFactory(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        exec.initialize();
        return exec;
    }

    @Bean("walletActivityReportingJob")
    public Job walletActivityReportingJob(
            JobRepository jobRepository,

           // @Qualifier("writeTransactionStep") Step writeTransactionStep,
          //  @Qualifier("processFeeInfoFileStep") Step processFeeInfoFileStep,

            @Qualifier("determineWorkUnitsStep") Step determineWorkUnitsStep,
            @Qualifier("generateAndSaveSettlementReportsManagerStep") Step generateAndSaveSettlementReportsManagerStep,
            @Qualifier("aggregateReportsAndCreateWalletActivityManagerStep") Step aggregateReportsAndCreateWalletActivityManagerStep
    ) {
        return new JobBuilder("walletActivityReportingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
               // .start(writeTransactionStep)
               // .next(processFeeInfoFileStep)
                .start(determineWorkUnitsStep)
                .next(generateAndSaveSettlementReportsManagerStep)
                .next(aggregateReportsAndCreateWalletActivityManagerStep)

                .build();
    }
}
