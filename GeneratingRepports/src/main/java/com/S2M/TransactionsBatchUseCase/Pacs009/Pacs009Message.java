package com.S2M.TransactionsBatchUseCase.Pacs009;



import jakarta.xml.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.009.001.08")
@XmlAccessorType(XmlAccessType.FIELD)
public class Pacs009Message {

    @XmlElement(name = "GrpHdr")
    private GroupHeader grpHdr;

    @XmlElement(name = "CdtTrfTxInf")
    private List<Pacs009MessageItem> transactions;

    public GroupHeader getGrpHdr() {
        return grpHdr;
    }

    public void setGrpHdr(GroupHeader grpHdr) {
        this.grpHdr = grpHdr;
    }

    public List<Pacs009MessageItem> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Pacs009MessageItem> transactions) {
        this.transactions = transactions;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GroupHeader {
        @XmlElement(name = "MsgId")
        private String msgId;

        @XmlElement(name = "CreDtTm")
        private String creationDateTime;

        public GroupHeader() {}

        public GroupHeader(String msgId, String creationDateTime) {
            this.msgId = msgId;
            this.creationDateTime = creationDateTime;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Pacs009MessageItem {

        @XmlElement(name = "DbtrAgt")
        private String debtorBIC;

        @XmlElement(name = "CdtrAgt")
        private String creditorBIC;

        @XmlElement(name = "IntrBkSttlmAmt")
        private String amount;

        @XmlAttribute(name = "Ccy")
        private String currency;

        @XmlElement(name = "IntrBkSttlmDt")
        private String settlementDate;

        public Pacs009MessageItem() {}

        public Pacs009MessageItem(String debtorBIC, String creditorBIC, String amount, String currency) {
            this.debtorBIC = debtorBIC;
            this.creditorBIC = creditorBIC;
            this.amount = amount;
            this.currency = currency;
            this.settlementDate =   LocalDate.now().toString();
        }
    }
}
