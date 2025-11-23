package bank.patterns.command;

import bank.core.models.Account;
import bank.core.models.Transaction;
import bank.patterns.strategy.TransactionStrategy;
import bank.repository.AccountRepository;

import java.util.function.Consumer;

public class TransactionCommand implements Runnable {

    private final Account targetAccount;
    private final Transaction transactionData;
    private final TransactionStrategy executionStrategy;
    private final Consumer<Transaction> completionHandler;
    private final AccountRepository repository;

    public TransactionCommand(Account account, Transaction transaction, TransactionStrategy strategy, Consumer<Transaction> handler, AccountRepository accountRepository) {
        this.targetAccount = account;
        this.transactionData = transaction;
        this.executionStrategy = strategy;
        this.completionHandler = handler;
        this.repository = accountRepository;
    }

    @Override
    public void run() {
        try {
            boolean result = executionStrategy.executeTransaction(targetAccount, transactionData, repository);
            if (!result && transactionData.getStatusMessage().contains("Pending")) {
                transactionData.setStatusMessage("FAILED: Execution could not be completed.");
            }
        } catch (Exception executionException) {
            transactionData.setStatusMessage("ERROR: System failure during execution: " + executionException.getMessage());
        } finally {
            completionHandler.accept(transactionData);
        }
    }
}