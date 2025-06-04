package com.S2M.CSVImport.Writer;

import com.S2M.CSVImport.Entity.Trasaction.FeeInfo;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;

import javax.sql.DataSource;

public class FeeInfoJdbcWriter implements ItemWriter<FeeInfo> {

    private final DataSource dataSource;
    private final ThreadLocal<JdbcBatchItemWriter<FeeInfo>> threadLocalWriter = new ThreadLocal<>();

    private static final String INSERT_FEE_INFO_SQL =
            "INSERT INTO TX_FEE_INFO (" +
                    "   id, FEE_AMOUNT, FEE_CODE, FEE_CURRENCY, FEE_DESC, FEE_SIGN, " +
                    "   FEE_TYPE, INST_REF, TX_ID" +
                    ") VALUES (" +
                    "   nextval('FEE_INFO_SEQ'), :feeAmount, :feeCode, :feeCurrency, " +
                    "   :feeDescription, :feeSign, :feeType, :institutionReference, :transactionId" +
                    ")";

    public FeeInfoJdbcWriter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private JdbcBatchItemWriter<FeeInfo> getWriter() {
        JdbcBatchItemWriter<FeeInfo> writer = threadLocalWriter.get();
        if (writer == null) {
            writer = new JdbcBatchItemWriterBuilder<FeeInfo>()
                    .dataSource(dataSource)
                    .sql(INSERT_FEE_INFO_SQL)
                    .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                    .build();
            try {
                writer.afterPropertiesSet();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to initialize writer", e);
            }
            threadLocalWriter.set(writer);
        }
        return writer;
    }

    @Override
    public void write(Chunk<? extends FeeInfo> chunk) throws Exception {
        getWriter().write(chunk);
    }
}
