package com.S2M.TransactionsBatchUseCase.Config.CSV;

import com.S2M.TransactionsBatchUseCase.Config.Batch.BatchProperties;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.Trasaction.Transaction;
import com.S2M.TransactionsBatchUseCase.Listeners.*;
import com.S2M.TransactionsBatchUseCase.Reader.CSV.GenericCsvReaderFactory;
import com.S2M.TransactionsBatchUseCase.Reader.CSV.Mapper.TransactionFileFieldSetMapper;
import com.S2M.TransactionsBatchUseCase.Writer.TransactionJdbcWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TransactionFileReaderConfig {

    private final BatchProperties batchProperties;
    private final TaskExecutor partitionTaskExecutor;


    @Bean
    @Qualifier("transactionFileTokenizer")
    public LineTokenizer transactionFileTokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(","); // Assuming CSV, change if different (e.g., ";", "\t")

        // **IMPORTANT**: These names MUST match the column headers in your CSV file
        // AND the order MUST match the order of columns in the CSV file.
        // These names are also used in TransactionFileFieldSetMapper.
        tokenizer.setNames(new String[]{  "id",
                "sessionId", "centerId", "debitorInstitutionId", "creditorInstitutionId",
                "transactionAmount", "transactionCurrency", "transactionSign", "transactionType",
                "transactionStatus", "transactionStatusDate", "debitorAccType", "creditorAccType",
                "creditorType", "debitorType", "reqTimestamp", "creditorAccNumber",
                "debitorAccNumber", "dphReference", "hostReference", "agreementReference",
                "debitorPhoneNumber"

        });
        return tokenizer;
    }

    @Bean
    @Qualifier("transactionFileFieldSetMapper")
    public FieldSetMapper<Transaction> transactionFileFieldSetMapper() {
        return new TransactionFileFieldSetMapper();
    }

    @Bean
    @Qualifier("transactionFileReader") // Changed qualifier name for clarity
    public FlatFileItemReader<Transaction> transactionFileReader(
            @Qualifier("transactionFileTokenizer") LineTokenizer tokenizer,
            @Qualifier("transactionFileFieldSetMapper") FieldSetMapper<Transaction> mapper
    ) {
        GenericCsvReaderFactory<Transaction> readerFactory =
                new GenericCsvReaderFactory<>(
                        "transactionFileReader",                // bean name for the reader
                        batchProperties.getTransactionInputFile(), // path from properties
                        true                                    // true if CSV has a header row
                );
        return readerFactory.createReader(tokenizer, mapper);
    }


    @Bean
    public ItemWriter<Transaction> transactionItemWriter(DataSource dataSource) {
        return new TransactionJdbcWriter(dataSource);
    }

    @Bean("writeTransactionStep")
    public Step writeTransactionStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     //@Qualifier("transactionFileReader") ItemReader<Transaction> reader,
                                     @Qualifier("threadSafeTransactionReader") ItemStreamReader<Transaction> reader,
                                     ItemWriter<Transaction> writer) {
        return new StepBuilder("writeTransactionStep", jobRepository)
                .<Transaction, Transaction>chunk(batchProperties.getCSVChunkSize(), transactionManager)
                .reader(reader)
                .writer(writer)
                .taskExecutor(partitionTaskExecutor)
                .listener(new LoggingStepListener())
                .listener(new LoggingSkipListener())
                .listener(new CSVWriteListener())
                //.listener(new SimpleChunkListener())
                .build();
    }

    @Bean
    @Qualifier("threadSafeTransactionReader")
    public SynchronizedItemStreamReader<Transaction> threadSafeTransactionReader(
            @Qualifier("transactionFileReader") FlatFileItemReader<Transaction> delegateReader) {
        SynchronizedItemStreamReader<Transaction> synchronizedReader = new SynchronizedItemStreamReader<>();
        synchronizedReader.setDelegate(delegateReader);
        return synchronizedReader;
    }
}
