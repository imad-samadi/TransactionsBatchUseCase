package com.S2M.CSVImport.Config.CSV;

import com.S2M.CSVImport.Config.BatchProperties;
import com.S2M.CSVImport.Entity.Trasaction.FeeInfo;
import com.S2M.CSVImport.Listeners.CSVWriteListener;
import com.S2M.CSVImport.Listeners.LoggingSkipListener;
import com.S2M.CSVImport.Listeners.LoggingStepListener;
import com.S2M.CSVImport.Reader.CSV.GenericCsvReaderFactory;
import com.S2M.CSVImport.Reader.CSV.Mapper.FeeInfoFileFieldSetMapper;
import com.S2M.CSVImport.Writer.FeeInfoJdbcWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class FeeInfoFileReaderConfig {

    private final BatchProperties batchProperties;

    private final TaskExecutor partitionTaskExecutor;



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
    public ItemWriter<FeeInfo> feeInfoItemWriter(DataSource dataSource) {
        return new FeeInfoJdbcWriter(dataSource);
    }

    @Bean("processFeeInfoFileStep")
    public Step processFeeInfoFileStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       //@Qualifier("feeInfoFileReader") ItemReader<FeeInfo> feeInfoFileReader,
                                       @Qualifier("threadSafeFeeInfoReader") ItemStreamReader<FeeInfo> feeInfoFileReader,
                                       ItemWriter<FeeInfo> feeInfoWriter) {
        return new StepBuilder("processFeeInfoFileStep", jobRepository)
                .<FeeInfo, FeeInfo>chunk(batchProperties.getCSVChunkSize(), transactionManager)
                .reader(feeInfoFileReader)
                .writer(feeInfoWriter)
                .listener(new LoggingStepListener())
                .listener(new LoggingSkipListener())
                //.listener(new SimpleChunkListener())
                .listener(new CSVWriteListener())
                .taskExecutor(partitionTaskExecutor)
                .build();
    }


    @Bean
    @Qualifier("threadSafeFeeInfoReader")
    public SynchronizedItemStreamReader<FeeInfo> threadSafeFeeInfoReader(
            @Qualifier("feeInfoFileReader") FlatFileItemReader<FeeInfo> delegateReader) {
        SynchronizedItemStreamReader<FeeInfo> synchronizedReader = new SynchronizedItemStreamReader<>();
        synchronizedReader.setDelegate(delegateReader);
        return synchronizedReader;
    }

}
