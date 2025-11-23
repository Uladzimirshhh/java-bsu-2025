package bank.patterns.observer;

import bank.core.models.Transaction;

public interface TransactionStatusListener {

    void notifyTransactionUpdate(Transaction updatedTransaction);
}