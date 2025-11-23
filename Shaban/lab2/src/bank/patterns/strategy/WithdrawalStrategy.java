package bank.patterns.strategy;

import bank.core.models.Account;
import bank.core.models.Transaction;
import bank.repository.AccountRepository;

import java.math.BigDecimal;

public class WithdrawalStrategy implements TransactionStrategy {

    @Override
    public boolean executeTransaction(Account targetAccount, Transaction transactionData, AccountRepository repository) {
        if (targetAccount.getIsFrozen()) {
            transactionData.setStatusMessage("FAILED: Account is frozen and cannot be modified.");
            return false;
        }

        if (transactionData.getTransactionAmount().signum() <= 0) {
            transactionData.setStatusMessage("FAILED: Withdrawal amount must be positive.");
            return false;
        }

        targetAccount.getAccountOperationLock().lock();
        try {
            BigDecimal resultingBalance = targetAccount.getCurrentBalance().subtract(transactionData.getTransactionAmount());
            if (resultingBalance.signum() < 0) {
                transactionData.setStatusMessage("FAILED: Insufficient funds. Current Balance: " + targetAccount.getCurrentBalance().toPlainString());
                return false;
            }

            targetAccount.setCurrentBalance(resultingBalance);
            transactionData.setStatusMessage("SUCCESS: Funds withdrawn. New Balance: " + targetAccount.getCurrentBalance().toPlainString());
            return true;
        } finally {
            targetAccount.getAccountOperationLock().unlock();
        }
    }
}