package com.S2M.TransactionsBatchUseCase.Writer;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.Trasaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;

import javax.sql.DataSource;


@RequiredArgsConstructor
public class TransactionJdbcWriter  implements ItemWriter<Transaction> {

    private final DataSource dataSource;


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

    private JdbcBatchItemWriter<Transaction> delegate;

    /**
     * Initializes the delegate JdbcBatchItemWriter if it hasn't been already.
     * This method is called before the first write operation.
     */
    private void initDelegate() {
        if (delegate == null) {
            delegate = new JdbcBatchItemWriterBuilder<Transaction>()
                    .dataSource(this.dataSource)
                    .sql(INSERT_TRANSACTION_SQL)
                    .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                    .build();

            delegate.afterPropertiesSet();

        }
    }

    @Override
    public void write(Chunk<? extends Transaction> chunk) throws Exception {
        initDelegate(); // Ensure the delegate is initialized
        delegate.write(chunk);
    }
}
