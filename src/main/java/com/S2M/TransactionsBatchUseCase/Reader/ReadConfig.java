package com.S2M.TransactionsBatchUseCase.Reader;

import com.S2M.TransactionsBatchUseCase.Config.BatchProperties;
import com.S2M.TransactionsBatchUseCase.DTO.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ReadConfig {

    // Externalized properties (file path, etc.)
    private final BatchProperties batchProperties;

    /**
     * LineTokenizer bean: splits each CSV line on commas
     * and maps columns to field names: reference, amount, currency, accountNumber.
     */
    @Bean
    @Qualifier("transactionCsvTokenizer")
    public LineTokenizer transactionCsvTokenizer() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames(new String[]{
                "reference",
                "amount",
                "currency",
                "accountNumber"
        });
        return tokenizer;
    }

    @Bean
    @Qualifier("transactionFieldSetMapper")
    public FieldSetMapper<Transaction> transactionFieldSetMapper() {
        BeanWrapperFieldSetMapper<Transaction> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Transaction.class);
        return fieldSetMapper;
    }

    @Bean
    @Qualifier("FlatFileItemReader")
    public FlatFileItemReader<Transaction> transactionCsvReader(
            @Qualifier("transactionCsvTokenizer") LineTokenizer tokenizer,
            @Qualifier("transactionFieldSetMapper") FieldSetMapper<Transaction> mapper
    ) {

        GenericCsvReaderFactory<Transaction> readerFactory =
                new GenericCsvReaderFactory<>(
                        "transactionCsvFileReader",
                        batchProperties.getInputFile(),
                        true
                );


        return readerFactory.createReader(tokenizer, mapper);
    }
}
