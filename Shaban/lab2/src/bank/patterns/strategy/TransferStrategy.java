package bank.patterns.strategy;

import bank.core.models.Account;
import bank.core.models.Transaction;
import bank.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

public class TransferStrategy implements TransactionStrategy {

    @Override
    public boolean executeTransaction(Account sourceAccount, Transaction transactionData, AccountRepository repository) {
        if (sourceAccount.getIsFrozen()) {
            transactionData.setStatusMessage("FAILED: Source account is frozen.");
            return false;
        }
        if (transactionData.getTransactionAmount().signum() <= 0) {
            transactionData.setStatusMessage("FAILED: Transfer amount must be positive.");
            return false;
        }

        Optional<Account> targetAccountOptional = Optional.empty();

        try {
            targetAccountOptional = repository.findAccountByIdentifier(UUID.fromString(transactionData.getStatusMessage()));
        } catch (IllegalArgumentException idException) {
        }

        Account targetAccount = targetAccountOptional.orElse(null);

        if (targetAccount == null) {
            transactionData.setStatusMessage("FAILED: Target account not found.");
            return false;
        }

        if (targetAccount.getIsFrozen()) {
            transactionData.setStatusMessage("FAILED: Target account is frozen.");
            return false;
        }

        if (sourceAccount.getAccountIdentifier().equals(targetAccount.getAccountIdentifier())) {
            transactionData.setStatusMessage("FAILED: Source and target accounts must be different.");
            return false;
        }


        Lock firstLock, secondLock;
        if (sourceAccount.getAccountIdentifier().compareTo(targetAccount.getAccountIdentifier()) < 0) {
            firstLock = sourceAccount.getAccountOperationLock();
            secondLock = targetAccount.getAccountOperationLock();
        } else {
            firstLock = targetAccount.getAccountOperationLock();
            secondLock = sourceAccount.getAccountOperationLock();
        }

        firstLock.lock();
        secondLock.lock();

        try {
            BigDecimal transferAmount = transactionData.getTransactionAmount();
            BigDecimal withdrawalResult = sourceAccount.getCurrentBalance().subtract(transferAmount);
            if (withdrawalResult.signum() < 0) {
                transactionData.setStatusMessage("FAILED: Source account has insufficient funds. Balance: " + sourceAccount.getCurrentBalance().toPlainString());
                return false;
            }

            sourceAccount.setCurrentBalance(withdrawalResult);
            targetAccount.setCurrentBalance(targetAccount.getCurrentBalance().add(transferAmount));

            repository.saveAccount(targetAccount);

            transactionData.setStatusMessage("SUCCESS: Funds transfered to " + targetAccount.getAccountIdentifier().toString().substring(0, 4) +
                    ". Source New Balance: " + sourceAccount.getCurrentBalance().toPlainString());
            return true;
        } finally {
            secondLock.unlock();
            firstLock.unlock();
        }
    }
}