package com.S2M.TransactionsBatchUseCase.Config.Batch;

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
public class MainBatchJobConfig {

    @Bean
    public TaskExecutor partitionTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

        // Determine a sensible max based on system resources (e.g., 2 * number of CPU cores for I/O bound)
        // Let's say your system can comfortably handle 8-16 concurrent database-intensive tasks.
        int systemCapacityMaxThreads = 16; // EXAMPLE VALUE - TUNE THIS!

        taskExecutor.setMaxPoolSize(systemCapacityMaxThreads);

        // Core pool size: Threads to keep alive even when idle.
        // Can be smaller to save resources if load is often low.
        taskExecutor.setCorePoolSize(Math.min(4, systemCapacityMaxThreads)); // e.g., 4

        // Queue capacity: How many tasks can wait if all maxPoolSize threads are busy.
        // A larger queue allows more partitions to be generated and queued up by the partitioner
        // without overwhelming the immediate thread pool.
        taskExecutor.setQueueCapacity(systemCapacityMaxThreads * 5); // e.g., 16 * 5 = 80
        // Allows many partitions to be queued.

        taskExecutor.setThreadNamePrefix("partition-worker-");
        taskExecutor.setAllowCoreThreadTimeOut(true); // Allow core threads to terminate if idle for too long
        taskExecutor.setKeepAliveSeconds(60);       // How long core threads can be idle before terminating
        taskExecutor.initialize();
        return taskExecutor;
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
