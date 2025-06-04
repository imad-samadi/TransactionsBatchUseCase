package com.S2M.CSVImport.Reader.CSV.Mapper;

import com.S2M.CSVImport.Entity.Trasaction.Transaction;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import java.util.HashSet;

public class TransactionFileFieldSetMapper implements FieldSetMapper<Transaction> {

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Transaction mapFieldSet(FieldSet fieldSet) {
        if (fieldSet == null) {
            return null;
        }

        Transaction.TransactionBuilder builder = Transaction.builder();

        // Read the id from the CSV
        builder.id(fieldSet.readLong("id"));

        // Initialize feeInfo to empty; populated later if needed
        builder.feeInfo(new HashSet<>());

        builder.sessionId(fieldSet.readString("sessionId"));
        builder.centerId(fieldSet.readString("centerId"));
        builder.debitorInstitutionId(fieldSet.readString("debitorInstitutionId"));
        builder.creditorInstitutionId(fieldSet.readString("creditorInstitutionId"));
        builder.transactionAmount(fieldSet.readBigDecimal("transactionAmount"));
        builder.transactionCurrency(fieldSet.readString("transactionCurrency"));
        builder.transactionSign(fieldSet.readString("transactionSign"));
        builder.transactionType(fieldSet.readString("transactionType"));
        builder.transactionStatus(fieldSet.readString("transactionStatus"));

        String statusDate = fieldSet.readString("transactionStatusDate");
        if (statusDate != null && !statusDate.isEmpty()) {
            builder.transactionStatusDate(fieldSet.readDate("transactionStatusDate", DATE_PATTERN));
        }

        builder.debitorAccType(fieldSet.readString("debitorAccType"));
        builder.creditorAccType(fieldSet.readString("creditorAccType"));
        builder.creditorType(fieldSet.readString("creditorType"));
        builder.debitorType(fieldSet.readString("debitorType"));
        builder.reqTimestamp(fieldSet.readString("reqTimestamp"));
        builder.creditorAccNumber(fieldSet.readString("creditorAccNumber"));
        builder.debitorAccNumber(fieldSet.readString("debitorAccNumber"));
        builder.dphReference(fieldSet.readString("dphReference"));
        builder.hostReference(fieldSet.readString("hostReference"));
        builder.agreementReference(fieldSet.readString("agreementReference"));
        builder.debitorPhoneNumber(fieldSet.readString("debitorPhoneNumber"));

        return builder.build();
    }
}
