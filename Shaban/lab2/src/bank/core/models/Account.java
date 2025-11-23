package bank.core.models;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {

    private final UUID accountIdentifier;
    private BigDecimal currentBalance;
    private boolean isFrozen;
    private final Lock accountOperationLock;

    public Account() {
        this.accountIdentifier = UUID.randomUUID();
        this.currentBalance = new BigDecimal("0.00");
        this.isFrozen = false;
        this.accountOperationLock = new ReentrantLock();
    }

    public UUID getAccountIdentifier() {
        return accountIdentifier;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public boolean getIsFrozen() {
        return isFrozen;
    }

    public void setIsFrozen(boolean isFrozen) {
        this.isFrozen = isFrozen;
    }
    
    public Lock getAccountOperationLock() {
        return accountOperationLock;
    }

    @Override
    public String toString() {
        return "AccountIdentifier: " + accountIdentifier.toString().substring(0, 8) + 
               ", Balance: " + currentBalance.toPlainString() + 
               ", Frozen: " + isFrozen;
    }
}
