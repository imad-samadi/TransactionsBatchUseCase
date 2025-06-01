package com.S2M.TransactionsBatchUseCase.Config.Batch;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@AllArgsConstructor
@Slf4j
public class MainBatchJobConfig {

    private final BatchProperties batchProperties;

    @Bean //strategy 2
    public TaskExecutor partitionTaskExecutor(
            @Value("${spring.datasource.hikari.maximum-pool-size:20}") int hikariMaxConnections
    ) {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();

        int cpuCores = Runtime.getRuntime().availableProcessors();

        int corePool = cpuCores;
        int maxPool  = Math.min(hikariMaxConnections, cpuCores * 2);

        exec.setCorePoolSize(corePool);          // e.g. 8 on an 8-core box
        exec.setMaxPoolSize(maxPool);            // e.g. min(20,16) = 16 if Hikari=20

        //10-slot queue so that after 16 active threads, up to 10 more tasks wait here
        exec.setQueueCapacity(10);

        exec.setThreadNamePrefix("partition-worker-");
        exec.setAllowCoreThreadTimeOut(true);
        exec.setKeepAliveSeconds(15);
        exec.setWaitForTasksToCompleteOnShutdown(false);

        // don’t block JVM exit
        exec.setThreadFactory(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        exec.initialize();
        return exec;
    }



    /*@Bean strategy 1 :
    public TaskExecutor partitionTaskExecutor(
            @Value("${spring.datasource.hikari.maximum-pool-size:20}") int hikariMax
    ) {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int corePool = cpuCores;
        int maxPool  = Math.min(hikariMax, cpuCores * 2);

        exec.setCorePoolSize(corePool);     // e.g. 8
        exec.setMaxPoolSize(maxPool);       // e.g. 16
        exec.setQueueCapacity(0);

        // CallerRunsPolicy:
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        exec.setThreadNamePrefix("partition-worker-");
        exec.setAllowCoreThreadTimeOut(true);
        exec.setKeepAliveSeconds(15);
        exec.setWaitForTasksToCompleteOnShutdown(false);
        exec.setThreadFactory(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        exec.initialize();
        return exec;
    }*/

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
