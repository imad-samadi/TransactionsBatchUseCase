package com.S2M.TransactionsBatchUseCase.Service;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.*;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.Trasaction.FeeInfo;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.Trasaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportCalculationService {



    // Transaction sign constants
    private static final String SIGN_DEBIT = "D";
    private static final String SIGN_CREDIT = "C";

    // Account type constants
    private static final String ACC_TYPE_WALLET = "0";
    private static final String ACC_TYPE_ACCOUNT = "1";
    private static final String ACC_TYPE_CARD = "2";



    public SettlementReport generateSettlementReportForInstitutionCurrency(
            List<Transaction> institutionTransactions,
            String currentInstitutionId,
            String processingCurrency,
            String sessionId,
            String centerId,
            Date sessionDate) {

        log.info("Generating settlement report for Institution: {}, Currency: {}, Session: {}, Transactions: {}",
                currentInstitutionId, processingCurrency, sessionId, institutionTransactions.size());

        SettlementReport sr = new SettlementReport();
        sr.setInstitutionId(currentInstitutionId);
        sr.setSessionId(sessionId);
        sr.setSessionDate(sessionDate);


        // Initialize report collections
        sr.setReportByTrxType(new ArrayList<>());
        sr.setReportByInstitution(new HashSet<>());
        sr.setReportByInstitutionAndTrxTypeResponse(new HashSet<>());

        // Build the overall (GlobalReport)
        GlobalReport globalReport = buildGlobalReport(institutionTransactions, currentInstitutionId, processingCurrency);
        sr.setGlobalReport(globalReport);

        // groupByTxType → buildReportByTrxType → populateAbstractReportMetrics
        Map<String, List<Transaction>> groupedByTxType = institutionTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getTransactionType));

        for (Map.Entry<String, List<Transaction>> entry : groupedByTxType.entrySet()) {
            ReportByTrxType rbtt = buildReportByTrxType(
                    entry.getValue(),
                    currentInstitutionId,
                    entry.getKey(),
                    processingCurrency
            );
            rbtt.setSettlementReport(sr);
            sr.getReportByTrxType().add(rbtt);
        }

        // Group by peer institution
        Map<String, List<Transaction>> groupedByPeerInstitution = institutionTransactions.stream()
                .collect(Collectors.groupingBy(tx -> getPeerInstitutionId(tx, currentInstitutionId)));

        for (Map.Entry<String, List<Transaction>> entry : groupedByPeerInstitution.entrySet()) {
            String peerInstId = entry.getKey();
            List<Transaction> peerTransactions = entry.getValue();

            ReportByInstitution rbi = buildReportByPeerInstitution(
                    peerTransactions,
                    currentInstitutionId,
                    peerInstId,
                    processingCurrency
            );
            rbi.setSettlementReport(sr);
            sr.getReportByInstitution().add(rbi);

            // Group by transaction type within peer institution
            Map<String, List<Transaction>> groupedByTxTypeForPeer = peerTransactions.stream()
                    .collect(Collectors.groupingBy(Transaction::getTransactionType));

            for (Map.Entry<String, List<Transaction>> txTypeEntry : groupedByTxTypeForPeer.entrySet()) {
                ReportByInstitutionAndTrxType rbit = buildReportByInstitutionAndTrxType(
                        txTypeEntry.getValue(),
                        currentInstitutionId,
                        peerInstId,
                        txTypeEntry.getKey(),
                        processingCurrency
                );
                rbit.setSettlementReport(sr);
                sr.getReportByInstitutionAndTrxTypeResponse().add(rbit);
            }
        }

        return sr;
    }

    private String getPeerInstitutionId(Transaction tx, String currentInstitutionId) {
        if (currentInstitutionId.equals(tx.getDebitorInstitutionId())) {
            return tx.getCreditorInstitutionId();
        } else if (currentInstitutionId.equals(tx.getCreditorInstitutionId())) {
            return tx.getDebitorInstitutionId();
        }
        return "UNKNOWN";
    }

    private GlobalReport buildGlobalReport(List<Transaction> transactions,
                                           String currentInstitutionId,
                                           String currency) {

        GlobalReport gr = new GlobalReport();
        initializeEmptyGlobalReport(gr, currency);

        if (transactions == null || transactions.isEmpty()) {
            log.info("Transactions list is empty");
            return gr;
        }

        for (Transaction tx : transactions) {
            boolean isDebitor = currentInstitutionId.equals(tx.getDebitorInstitutionId());
            boolean isCreditor = currentInstitutionId.equals(tx.getCreditorInstitutionId());

            BigDecimal txAmount = tx.getTransactionAmount();
            String txSign = tx.getTransactionSign();

            // Update transaction counts and amounts
            gr.setTransactionCount(gr.getTransactionCount() + 1);

            if (isCreditor) {
                gr.setCreditTrxCount(gr.getCreditTrxCount() + 1);
                gr.setCreditTrxAmount(gr.getCreditTrxAmount().add(txAmount));

                // Handle creditor account type
                if (ACC_TYPE_ACCOUNT.equals(tx.getCreditorAccType())) {
                    gr.setAccountCreditTrxCount(gr.getAccountCreditTrxCount() + 1);
                    gr.setAccountCreditTrxAmount(gr.getAccountCreditTrxAmount().add(txAmount));
                } else if (ACC_TYPE_WALLET.equals(tx.getCreditorAccType())) {
                    gr.setWalletCreditTrxCount(gr.getWalletCreditTrxCount() + 1);
                    gr.setWalletCreditTrxAmount(gr.getWalletCreditTrxAmount().add(txAmount));
                } else if (ACC_TYPE_CARD.equals(tx.getCreditorAccType())) {
                    gr.setCardCreditTrxCount(gr.getCardCreditTrxCount() + 1);
                    gr.setCardCreditTrxAmount(gr.getCardCreditTrxAmount().add(txAmount));
                }
            }

            if (isDebitor) {
                gr.setDebitTrxCount(gr.getDebitTrxCount() + 1);
                gr.setDebitTrxAmount(gr.getDebitTrxAmount().add(txAmount));

                // Handle debitor account type
                if (ACC_TYPE_ACCOUNT.equals(tx.getDebitorAccType())) {
                    gr.setAccountDebitTrxCount(gr.getAccountDebitTrxCount() + 1);
                    gr.setAccountDebitTrxAmount(gr.getAccountDebitTrxAmount().add(txAmount));
                } else if (ACC_TYPE_WALLET.equals(tx.getDebitorAccType())) {
                    gr.setWalletDebitTrxCount(gr.getWalletDebitTrxCount() + 1);
                    gr.setWalletDebitTrxAmount(gr.getWalletDebitTrxAmount().add(txAmount));
                } else if (ACC_TYPE_CARD.equals(tx.getDebitorAccType())) {
                    gr.setCardDebitTrxCount(gr.getCardDebitTrxCount() + 1);
                    gr.setCardDebitTrxAmount(gr.getCardDebitTrxAmount().add(txAmount));
                }
            }

            // Process fees
            processFeesForGlobalReport(gr, tx, currentInstitutionId);
        }

        // Calculate net values
        calculateNetAmounts(gr);
        return gr;
    }
    /**
     * Zero‐out every count and amount field on GlobalReport,
     * and seed its transaction currency.
     */
    private void initializeEmptyGlobalReport(GlobalReport gr, String currency) {
        gr.setNetSettlementCurren(currency);
        gr.setTransactionCount(0);
        gr.setNetSettlementAmount(BigDecimal.ZERO);
        gr.setNetSettlementSign(SIGN_CREDIT);

        // Transaction counts & amounts
        gr.setCreditTrxCount(0);
        gr.setDebitTrxCount(0);
        gr.setCreditTrxAmount(BigDecimal.ZERO);
        gr.setDebitTrxAmount(BigDecimal.ZERO);

        // By account type
        gr.setAccountCreditTrxCount(0);
        gr.setAccountCreditTrxAmount(BigDecimal.ZERO);
        gr.setWalletCreditTrxCount(0);
        gr.setWalletCreditTrxAmount(BigDecimal.ZERO);
        gr.setCardCreditTrxCount(0);
        gr.setCardCreditTrxAmount(BigDecimal.ZERO);

        gr.setAccountDebitTrxCount(0);
        gr.setAccountDebitTrxAmount(BigDecimal.ZERO);
        gr.setWalletDebitTrxCount(0);
        gr.setWalletDebitTrxAmount(BigDecimal.ZERO);
         gr.setCardDebitTrxCount(0);
         gr.setCardDebitTrxAmount(BigDecimal.ZERO);

        // Interchange fees
        gr.setCreditInterFeeCount(0);
        gr.setCreditInterFeeTotalAmount(BigDecimal.ZERO);
        gr.setDebitInterFeeCount(0);
        gr.setDebitInterFeeTotalAmount(BigDecimal.ZERO);
        gr.setInterchangeFeeTotalAmount(BigDecimal.ZERO);
        gr.setInterchangeFeeSign(SIGN_CREDIT);

        // Gross
        gr.setGrossSettlementAmount(BigDecimal.ZERO);
        gr.setGrossSettlementSign(SIGN_CREDIT);

    }

    private void processFeesForGlobalReport(GlobalReport gr, Transaction tx, String currentInstitutionId) {
        if (tx.getFeeInfo() == null) return;

        for (FeeInfo fee : tx.getFeeInfo()) {
            // Only count fees that belong to this institution
            if (! currentInstitutionId.equals(fee.getInstitutionReference())) {
                continue;
            }

                BigDecimal amt = fee.getFeeAmount();
                if (SIGN_CREDIT.equals(fee.getFeeSign())) {
                    gr.setCreditInterFeeCount(gr.getCreditInterFeeCount() + 1);
                    gr.setCreditInterFeeTotalAmount(gr.getCreditInterFeeTotalAmount().add(amt));
                } else if (SIGN_DEBIT.equals(fee.getFeeSign())) {
                    gr.setDebitInterFeeCount(gr.getDebitInterFeeCount() + 1);
                    gr.setDebitInterFeeTotalAmount(gr.getDebitInterFeeTotalAmount().add(amt));
                }

        }
    }



    private void calculateNetAmounts(GlobalReport gr) {
        // Gross settlement (transactions only)
        BigDecimal grossSettlement = gr.getCreditTrxAmount().subtract(gr.getDebitTrxAmount());
        gr.setGrossSettlementAmount(grossSettlement.abs()); //make it positive the sign who decide !!!!
        gr.setGrossSettlementSign(grossSettlement.compareTo(BigDecimal.ZERO) >= 0 ? SIGN_CREDIT : SIGN_DEBIT);

        // Net settlement (transactions + fees)
        BigDecimal totalCredits = gr.getCreditTrxAmount().add(gr.getCreditInterFeeTotalAmount());
        BigDecimal totalDebits = gr.getDebitTrxAmount().add(gr.getDebitInterFeeTotalAmount());
        BigDecimal netSettlement = totalCredits.subtract(totalDebits);

        gr.setNetSettlementAmount(netSettlement.abs());
        gr.setNetSettlementSign(netSettlement.compareTo(BigDecimal.ZERO) >= 0 ? SIGN_CREDIT : SIGN_DEBIT);

        // Interchange fee totals
        BigDecimal netInterchange = gr.getCreditInterFeeTotalAmount().subtract(gr.getDebitInterFeeTotalAmount());
        gr.setInterchangeFeeTotalAmount(netInterchange.abs());
        gr.setInterchangeFeeSign(netInterchange.compareTo(BigDecimal.ZERO) >= 0 ? SIGN_CREDIT : SIGN_DEBIT);
    }

    private void populateAbstractReportMetrics(AbstractReport report,
                                               List<Transaction> transactions,
                                               String currentInstitutionId,
                                               String currency) {
        // 1) initialize everything
        initializeEmptyAbstractReport(report, currency);

        if (transactions == null || transactions.isEmpty()) {
            return;
        }

        for (Transaction tx : transactions) {
            boolean isDebitor = currentInstitutionId.equals(tx.getDebitorInstitutionId());
            boolean isCreditor = currentInstitutionId.equals(tx.getCreditorInstitutionId());

            BigDecimal txAmount = tx.getTransactionAmount();

            report.setTransactionCount(report.getTransactionCount() + 1);

            if (isCreditor) {
                report.setCreditTrxCount(report.getCreditTrxCount() + 1);
                report.setCreditTrxTotalAmount(report.getCreditTrxTotalAmount().add(txAmount));
            }

            if (isDebitor) {
                report.setDebitTrxCount(report.getDebitTrxCount() + 1);
                report.setDebitTrxTotalAmount(report.getDebitTrxTotalAmount().add(txAmount));
            }

            // Process fees
            processFeesForAbstractReport(report, tx, currentInstitutionId);
        }

        // Calculate net transaction amount
        BigDecimal netTxAmount = report.getCreditTrxTotalAmount().subtract(report.getDebitTrxTotalAmount());
        report.setTransactionTotalAmount(netTxAmount.abs());
        report.setTransactionTotalSign(netTxAmount.compareTo(BigDecimal.ZERO) >= 0 ? SIGN_CREDIT : SIGN_DEBIT);
    }

    /**
     * Zero-out every counter and amount field on any AbstractReport,
     * and set its transaction currency.
     */
    private void initializeEmptyAbstractReport(AbstractReport report, String currency) {

        report.setTransactionCurrency(currency);

        // Transaction counts & totals
        report.setTransactionCount(0);
        report.setTransactionTotalAmount(BigDecimal.ZERO);
        report.setTransactionTotalSign(SIGN_CREDIT);  // default

        // Credit-side transactions
        report.setCreditTrxCount(0);
        report.setCreditTrxTotalAmount(BigDecimal.ZERO);

        // Debit-side transactions
        report.setDebitTrxCount(0);
        report.setDebitTrxTotalAmount(BigDecimal.ZERO);

        // Interchange fees
        report.setCreditInterFeeCount(0);
        report.setCreditInterFeeTotalAmount(BigDecimal.ZERO);
        report.setDebitInterFeeCount(0);
        report.setDebitInterFeeTotalAmount(BigDecimal.ZERO);
    }

    private void processFeesForAbstractReport(AbstractReport report, Transaction tx, String currentInstitutionId) {
        if (tx.getFeeInfo() == null) return;

        for (FeeInfo fee : tx.getFeeInfo()) {
            if (!currentInstitutionId.equals(fee.getInstitutionReference()) ) {
                continue;
            }

            BigDecimal feeAmount = fee.getFeeAmount();

            if (SIGN_CREDIT.equals(fee.getFeeSign())) {
                report.setCreditInterFeeCount(report.getCreditInterFeeCount() + 1);
                report.setCreditInterFeeTotalAmount(report.getCreditInterFeeTotalAmount().add(feeAmount));
            } else if (SIGN_DEBIT.equals(fee.getFeeSign())) {
                report.setDebitInterFeeCount(report.getDebitInterFeeCount() + 1);
                report.setDebitInterFeeTotalAmount(report.getDebitInterFeeTotalAmount().add(feeAmount));
            }
        }
    }

    private ReportByTrxType buildReportByTrxType(List<Transaction> transactions,
                                                 String currentInstitutionId,
                                                 String txType,
                                                 String currency) {
        ReportByTrxType rbtt = new ReportByTrxType();
        rbtt.setTransactionType(txType);
        populateAbstractReportMetrics(rbtt, transactions, currentInstitutionId, currency);
        return rbtt;
    }

    private ReportByInstitution buildReportByPeerInstitution(List<Transaction> transactions,
                                                             String currentInstitutionId,
                                                             String peerInstId,
                                                             String currency) {
        ReportByInstitution rbi = new ReportByInstitution();
        rbi.setPeerInstId(peerInstId);
        populateAbstractReportMetrics(rbi, transactions, currentInstitutionId, currency);
        return rbi;
    }

    private ReportByInstitutionAndTrxType buildReportByInstitutionAndTrxType(
            List<Transaction> transactions,
            String currentInstitutionId,
            String peerInstId,
            String txType,
            String currency) {

        ReportByInstitutionAndTrxType rbit = new ReportByInstitutionAndTrxType();
        rbit.setPeerInstId(peerInstId);
        rbit.setTransactionType(txType);

        // Populate common metrics
        populateAbstractReportMetrics(rbit, transactions, currentInstitutionId, currency);



        return rbit;
    }


}
