package com.S2M.TransactionsBatchUseCase.Config.CSV;
import com.S2M.TransactionsBatchUseCase.Config.Batch.BatchProperties;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.Trasaction.FeeInfo;
import com.S2M.TransactionsBatchUseCase.Listeners.LoggingSkipListener;
import com.S2M.TransactionsBatchUseCase.Listeners.LoggingStepListener;
import com.S2M.TransactionsBatchUseCase.Listeners.PerThreadCountingWriteListener;
import com.S2M.TransactionsBatchUseCase.Listeners.SimpleChunkListener;
import com.S2M.TransactionsBatchUseCase.Writer.FeeInfoJdbcWriter;
import com.S2M.TransactionsBatchUseCase.Reader.CSV.GenericCsvReaderFactory;
import com.S2M.TransactionsBatchUseCase.Reader.CSV.Mapper.FeeInfoFileFieldSetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class FeeInfoFileReaderConfig {

    private final BatchProperties batchProperties;

    @Bean
    @Qualifier("feeInfoFileTokenizer")
    public LineTokenizer feeInfoFileTokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(","); // Or your CSV delimiter

        // **IMPORTANT**: These names MUST match the column headers in your FeeInfo CSV file
        // AND the order MUST match the order of columns in the CSV file.
        // These names are also used in FeeInfoFileFieldSetMapper.
        tokenizer.setNames(new String[]{
                "feeAmount", "feeCode", "feeCurrency", "feeDescription",
                "feeSign", "feeType", "institutionReference",
                "transactionId" // Column in CSV representing the parent Transaction's ID
        });
        return tokenizer;
    }

    @Bean
    @Qualifier("feeInfoFileFieldSetMapper")
    public FieldSetMapper<FeeInfo> feeInfoFileFieldSetMapper() {
        return new FeeInfoFileFieldSetMapper();
    }

    @Bean
    @Qualifier("feeInfoFileReader")
    public FlatFileItemReader<FeeInfo> feeInfoFileReader(
            @Qualifier("feeInfoFileTokenizer") LineTokenizer tokenizer,
            @Qualifier("feeInfoFileFieldSetMapper") FieldSetMapper<FeeInfo> mapper
    ) {
        GenericCsvReaderFactory<FeeInfo> readerFactory =
                new GenericCsvReaderFactory<>(
                        "feeInfoFileReader",                  // bean name for the reader
                        batchProperties.getFeeInfoInputFile(),// path from properties
                        true                                  // true if CSV has a header row
                );
        return readerFactory.createReader(tokenizer, mapper);
    }

    @Bean
    public FeeInfoJdbcWriter feeInfoItemWriter(DataSource dataSource) {
        return new FeeInfoJdbcWriter(dataSource);
    }

    @Bean("processFeeInfoFileStep")
    public Step processFeeInfoFileStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       @Qualifier("feeInfoFileReader") ItemReader<FeeInfo> feeInfoReader,
                                       FeeInfoJdbcWriter feeInfoWriter) {
        return new StepBuilder("processFeeInfoFileStep", jobRepository)
                .<FeeInfo, FeeInfo>chunk(100, transactionManager)
                .reader(feeInfoReader)
                .writer(feeInfoWriter)
                .listener(new LoggingStepListener())
                .listener(new LoggingSkipListener())
                .listener(new PerThreadCountingWriteListener())
                .listener(new SimpleChunkListener())
                .build();
    }
}
