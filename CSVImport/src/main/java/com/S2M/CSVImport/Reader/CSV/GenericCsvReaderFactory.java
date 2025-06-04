package com.S2M.CSVImport.Reader.CSV;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;

public class GenericCsvReaderFactory<T> {

    private final String readerName;
    private final String filePath;
    private final boolean hasHeader;



    public GenericCsvReaderFactory(String readerName, String filePath, boolean hasHeader) {
        Assert.hasText(readerName, "Reader name cannot be empty.");
        Assert.hasText(filePath, "File path cannot be empty.");
        this.readerName = readerName;
        this.filePath = filePath;
        this.hasHeader = hasHeader;
    }


    public FlatFileItemReader<T> createReader(
            LineTokenizer lineTokenizer,
            FieldSetMapper<T> fieldSetMapper
    ) {
        Assert.notNull(lineTokenizer, "LineTokenizer cannot be null.");
        Assert.notNull(fieldSetMapper, "FieldSetMapper cannot be null.");


        DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);


        FlatFileItemReaderBuilder<T> builder = new FlatFileItemReaderBuilder<>();
        builder.name(this.readerName);
        builder.resource(new FileSystemResource(this.filePath));
        if (this.hasHeader) {
            builder.linesToSkip(1);
        }
        builder.lineMapper(lineMapper);
        builder.strict(true);

        return builder.build();
    }
}
