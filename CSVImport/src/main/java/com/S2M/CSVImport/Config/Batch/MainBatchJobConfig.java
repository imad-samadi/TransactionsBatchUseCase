package com.S2M.CSVImport.Config.Batch;

import com.S2M.CSVImport.Listeners.LoggingJobListener;
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



    @Bean("CSVImportJob")
    public Job CSVImportJobJob(
            JobRepository jobRepository,

            @Qualifier("writeTransactionStep") Step writeTransactionStep,
            @Qualifier("processFeeInfoFileStep") Step processFeeInfoFileStep


    ) {
        return new JobBuilder("CSVImportJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(writeTransactionStep)
                .next(processFeeInfoFileStep)
                .listener(new LoggingJobListener())

                .build();
    }
}
