package bank.core.models;

import bank.patterns.strategy.TransactionActionType;
import bank.patterns.visitor.TransactionVisitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {

    private final UUID transactionIdentifier;
    private final LocalDateTime transactionTimestamp;
    private final TransactionActionType actionType;
    private final BigDecimal transactionAmount;
    private final UUID accountIdentifier;
    private String statusMessage;

    public Transaction(TransactionActionType actionType, BigDecimal amount, UUID accountIdentifier) {
        this.transactionIdentifier = UUID.randomUUID();
        this.transactionTimestamp = LocalDateTime.now();
        this.actionType = actionType;
        this.transactionAmount = amount;
        this.accountIdentifier = accountIdentifier;
        this.statusMessage = "Pending";
    }

    public UUID getTransactionIdentifier() {
        return transactionIdentifier;
    }

    public LocalDateTime getTransactionTimestamp() {
        return transactionTimestamp;
    }

    public TransactionActionType getActionType() {
        return actionType;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public UUID getAccountIdentifier() {
        return accountIdentifier;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public void accept(TransactionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return transactionIdentifier.toString().substring(0, 4) + 
               " - " + transactionTimestamp.toLocalTime().toString().substring(0, 8) + 
               " | Action: " + actionType.name() + 
               " | Amount: " + transactionAmount.toPlainString() + 
               " | Status: " + statusMessage;
    }
}
