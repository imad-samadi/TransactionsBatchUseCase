package com.S2M.TransactionsBatchUseCase.Config.Batch;

import com.S2M.TransactionsBatchUseCase.Listeners.LoggingJobListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
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


   @Bean
   public TaskExecutor partitionTaskExecutor(@Value("${spring.datasource.hikari.maximum-pool-size}") int hikariMaxConnections) {
       ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();


       int systemCapacityMaxThreads = Runtime.getRuntime().availableProcessors() ;

       log.info("systemCapacityMaxThreads: {}", systemCapacityMaxThreads);

       log.info("hikariMaxConnections {}", hikariMaxConnections);

       int safeMaxThreads = Math.min(systemCapacityMaxThreads, hikariMaxConnections-1);

       taskExecutor.setMaxPoolSize(safeMaxThreads);


       taskExecutor.setCorePoolSize(safeMaxThreads);

       log.info("CorePoolSize : {}", safeMaxThreads);

       // Queue capacity: How many tasks can wait if all maxPoolSize threads are busy.
       // A larger queue allows more partitions to be generated and queued up by the partitioner
       // without overwhelming the immediate thread pool.
       taskExecutor.setQueueCapacity(safeMaxThreads * 5); // e.g., 16 * 5 = 80


       taskExecutor.setThreadNamePrefix("partition-worker-");
       taskExecutor.setAllowCoreThreadTimeOut(true); // Allow core threads to terminate if idle for too long
       taskExecutor.setKeepAliveSeconds(60);       // How long core threads can be idle before terminating
       taskExecutor.initialize();
       taskExecutor.setThreadFactory(r -> {
           Thread t = new Thread(r);
           t.setDaemon(true);
           return t;
       });
       return taskExecutor;
   }



    @Bean("walletActivityReportingJob")
    public Job walletActivityReportingJob(
            JobRepository jobRepository,

           // @Qualifier("writeTransactionStep") Step writeTransactionStep,
            // @Qualifier("processFeeInfoFileStep") Step processFeeInfoFileStep,

            @Qualifier("determineWorkUnitsStep") Step determineWorkUnitsStep,
            @Qualifier("generateAndSaveSettlementReportsManagerStep") Step generateAndSaveSettlementReportsManagerStep,
            @Qualifier("linkReportsToWalletActivityStep") Step aggregateReportsAndCreateWalletActivityStep,
            @Qualifier("generatePacs009XmlStep") Step generatePacs009XmlStep ,
            @Qualifier("pacs009JobListener")JobExecutionListener pacs009JobListener
    ) {
        return new JobBuilder("walletActivityReportingJob", jobRepository)
                .incrementer(new RunIdIncrementer())

                .start(determineWorkUnitsStep)
                .next(generateAndSaveSettlementReportsManagerStep)
                .next(aggregateReportsAndCreateWalletActivityStep)
                .next(generatePacs009XmlStep)
                .listener(new LoggingJobListener())
                .listener(pacs009JobListener)

                .build();
    }
}
