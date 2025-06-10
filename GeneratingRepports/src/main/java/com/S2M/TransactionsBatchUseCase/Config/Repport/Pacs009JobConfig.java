package com.S2M.TransactionsBatchUseCase.Config.Repport;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.SettlementReport;
import com.S2M.TransactionsBatchUseCase.Pacs009.Pacs009ItemProcessor;
import com.S2M.TransactionsBatchUseCase.Pacs009.Pacs009ItemWriter;
import com.S2M.TransactionsBatchUseCase.Pacs009.Pacs009Message;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class Pacs009JobConfig {

    @Bean
    @StepScope
    public JpaPagingItemReader<SettlementReport> pacs009Reader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<SettlementReport>()
                .name("pacs009Reader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM SettlementReport s WHERE s.globalReport IS NOT NULL")
                .pageSize(10)
                .build();
    }

    @Bean
    public Pacs009ItemProcessor pacs009Processor() {
        return new Pacs009ItemProcessor();
    }

    @Bean
    public Pacs009ItemWriter pacs009Writer() {
        return new Pacs009ItemWriter();
    }

    @Bean("pacs009JobListener")
    public JobExecutionListener pacs009JobListener(Pacs009ItemWriter pacs009Writer) {
        return new JobExecutionListener() {
            @Override
            public void afterJob(JobExecution jobExecution) {
                try {
                    pacs009Writer.afterJobWriteToXml();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to write pacs.009 XML", e);
                }
            }

            @Override
            public void beforeJob(JobExecution jobExecution) {
                // Optional
            }
        };
    }

    @Bean("generatePacs009XmlStep")
    public Step generatePacs009XmlStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       JpaPagingItemReader<SettlementReport> pacs009Reader,
                                       Pacs009ItemProcessor pacs009Processor,
                                       Pacs009ItemWriter pacs009Writer) {

        return new StepBuilder("generatePacs009XmlStep", jobRepository)
                .<SettlementReport, Pacs009Message.Pacs009MessageItem>chunk(10, transactionManager)
                .reader(pacs009Reader)
                .processor(pacs009Processor)
                .writer(pacs009Writer)
                .build();
    }
}
