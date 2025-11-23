package bank.patterns.strategy;

import bank.core.models.Account;
import bank.core.models.Transaction;
import bank.repository.AccountRepository;

public class FreezeStrategy implements TransactionStrategy {

    @Override
    public boolean executeTransaction(Account targetAccount, Transaction transactionData, AccountRepository repository) {
        targetAccount.getAccountOperationLock().lock();
        try {
            targetAccount.setIsFrozen(true);
            transactionData.setStatusMessage("SUCCESS: Account has been FROZEN.");
            return true;
        } finally {
            targetAccount.getAccountOperationLock().unlock();
        }
    }
}