package com.S2M.CSVImport.Writer;

import com.S2M.CSVImport.Entity.Trasaction.Transaction;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;

import javax.sql.DataSource;

public class TransactionJdbcWriter implements ItemWriter<Transaction> {

    private final DataSource dataSource;
    private final ThreadLocal<JdbcBatchItemWriter<Transaction>> threadLocalWriter = new ThreadLocal<>();

    private static final String INSERT_TRANSACTION_SQL =
            "INSERT INTO TRANSACTION (" +
                    "   id, SESSION_ID, TX_CENTER_ID, DEBIT_INST_REF, CRED_INST_ID, TX_AMOUNT, TX_CURRENCY, " +
                    "   TX_SIGN, TX_TYPE, TX_STATUS, TX_STATUS_DATE, DEBIT_ACC_TYPE, CRED_ACC_TYPE, " +
                    "   CRED_TYPE, DEBIT_TYPE, REQ_TIMESTAMP, CRED_ACC_NUMBER, DEBIT_ACC_NUMBER, " +
                    "   DPH_REFERENCE, HOST_REFERENCE, AGREEMENT_REFERENCE, DEBIT_PHONE_NUMBER" +
                    ") VALUES (" +
                    "   :id, :sessionId, :centerId, :debitorInstitutionId, :creditorInstitutionId, :transactionAmount, :transactionCurrency, " +
                    "   :transactionSign, :transactionType, :transactionStatus, :transactionStatusDate, :debitorAccType, :creditorAccType, " +
                    "   :creditorType, :debitorType, :reqTimestamp, :creditorAccNumber, :debitorAccNumber, " +
                    "   :dphReference, :hostReference, :agreementReference, :debitorPhoneNumber" +
                    ")";

    public TransactionJdbcWriter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private JdbcBatchItemWriter<Transaction> getWriter() {
        JdbcBatchItemWriter<Transaction> writer = threadLocalWriter.get();
        if (writer == null) {
            writer = new JdbcBatchItemWriterBuilder<Transaction>()
                    .dataSource(dataSource)
                    .sql(INSERT_TRANSACTION_SQL)
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
    public void write(Chunk<? extends Transaction> chunk) throws Exception {
        getWriter().write(chunk);
    }
}
