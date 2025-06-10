package com.S2M.TransactionsBatchUseCase.Pacs009;

import com.S2M.TransactionsBatchUseCase.Entity.Repport.GlobalReport;
import com.S2M.TransactionsBatchUseCase.Entity.Repport.SettlementReport;
import org.springframework.batch.item.ItemProcessor;

public class Pacs009ItemProcessor implements ItemProcessor<SettlementReport, Pacs009Message.Pacs009MessageItem> {

    @Override
    public Pacs009Message.Pacs009MessageItem process(SettlementReport report) {
        GlobalReport global = report.getGlobalReport();

        String sign = global.getNetSettlementSign(); // Should be "D" or "C"
        String currency = global.getNetSettlementCurren();
        String amount = global.getNetSettlementAmount().abs().toPlainString();

        String debtorBIC;
        String creditorBIC;

        if ("D".equalsIgnoreCase(sign)) {
            debtorBIC = resolveBIC(report.getInstitutionId());
            creditorBIC = getCentralBankBIC();
        } else {
            debtorBIC = getCentralBankBIC();
            creditorBIC = resolveBIC(report.getInstitutionId());
        }

        return new Pacs009Message.Pacs009MessageItem(debtorBIC, creditorBIC, amount, currency);
    }

    private String resolveBIC(String institutionId) {
        return  institutionId ;
    }

    private String getCentralBankBIC() {
        return "CENTRALBANKLYBXXX";
    }
}
