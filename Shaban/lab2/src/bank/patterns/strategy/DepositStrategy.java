package bank.patterns.strategy;

import bank.core.models.Account;
import bank.core.models.Transaction;
import bank.repository.AccountRepository;

public class DepositStrategy implements TransactionStrategy {

    @Override
    public boolean executeTransaction(Account targetAccount, Transaction transactionData, AccountRepository repository) {
        if (targetAccount.getIsFrozen()) {
            transactionData.setStatusMessage("FAILED: Account is frozen and cannot be modified.");
            return false;
        }

        if (transactionData.getTransactionAmount().signum() <= 0) {
            transactionData.setStatusMessage("FAILED: Deposit amount must be positive.");
            return false;
        }

        targetAccount.getAccountOperationLock().lock();
        try {
            targetAccount.setCurrentBalance(targetAccount.getCurrentBalance().add(transactionData.getTransactionAmount()));
            transactionData.setStatusMessage("SUCCESS: Funds deposited. New Balance: " + targetAccount.getCurrentBalance().toPlainString());
            return true;
        } finally {
            targetAccount.getAccountOperationLock().unlock();
        }
    }
}