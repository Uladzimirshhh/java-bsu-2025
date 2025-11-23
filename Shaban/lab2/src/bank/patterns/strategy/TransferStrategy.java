package bank.patterns.strategy;

import bank.core.models.Account;
import bank.core.models.Transaction;
import bank.repository.AccountRepository;
import bank.repository.MockAccountRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

public class TransferStrategy implements TransactionStrategy {

    private static final String TARGET_ACCOUNT_KEY = "TargetAccount";

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

        Account targetAccount = null;
        if (repository instanceof MockAccountRepository) {
            targetAccount = ((MockAccountRepository) repository).getTransferPartnerAccount(sourceAccount.getAccountIdentifier());
        }

        if (targetAccount == null || targetAccount.getIsFrozen()) {
            transactionData.setStatusMessage("FAILED: Target account is frozen or not found.");
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
            BigDecimal withdrawalResult = sourceAccount.getCurrentBalance().subtract(transactionData.getTransactionAmount());
            if (withdrawalResult.signum() < 0) {
                transactionData.setStatusMessage("FAILED: Source account has insufficient funds.");
                return false;
            }

            sourceAccount.setCurrentBalance(withdrawalResult);
            targetAccount.setCurrentBalance(targetAccount.getCurrentBalance().add(transactionData.getTransactionAmount()));

            repository.saveAccount(targetAccount);

            transactionData.setStatusMessage("SUCCESS: Funds transfered from " + sourceAccount.getAccountIdentifier().toString().substring(0, 4) +
                    " to " + targetAccount.getAccountIdentifier().toString().substring(0, 4) +
                    ". Source New Balance: " + sourceAccount.getCurrentBalance().toPlainString());
            return true;
        } finally {
            secondLock.unlock();
            firstLock.unlock();
        }
    }
}