package bank.service;

import bank.core.models.Account;
import bank.core.models.Transaction;
import bank.patterns.command.TransactionCommand;
import bank.patterns.factory.TransactionStrategyFactory;
import bank.patterns.observer.TransactionStatusListener;
import bank.patterns.strategy.TransactionStrategy;
import bank.repository.MockAccountRepository;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionProcessor {

    private static volatile TransactionProcessor instanceOfProcessor;
    private final ExecutorService commandExecutor;
    private final TransactionStrategyFactory strategyFactory;
    private final List<TransactionStatusListener> statusListeners;
    private final MockAccountRepository accountDataAccess;

    private TransactionProcessor(MockAccountRepository repository, TransactionStrategyFactory factory) {
        this.commandExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
        this.strategyFactory = factory;
        this.statusListeners = Collections.synchronizedList(new LinkedList<>());
        this.accountDataAccess = repository;
    }

    public static TransactionProcessor getTransactionProcessorInstance(MockAccountRepository repository, TransactionStrategyFactory factory) {
        if (instanceOfProcessor == null) {
            synchronized (TransactionProcessor.class) {
                if (instanceOfProcessor == null) {
                    instanceOfProcessor = new TransactionProcessor(repository, factory);
                }
            }
        }
        return instanceOfProcessor;
    }

    public void registerListener(TransactionStatusListener listener) {
        statusListeners.add(listener);
    }

    public void submitTransaction(Transaction newTransaction) {
        Optional<Account> targetAccountOptional = accountDataAccess.findAccountByIdentifier(newTransaction.getAccountIdentifier());
        if (targetAccountOptional.isEmpty()) {
            newTransaction.setStatusMessage("FAILED: Target account not found in repository.");
            handleCompletion(newTransaction);
            return;
        }
        Account targetAccount = targetAccountOptional.get();

        try {
            // Вся логика, связанная со старым методом getTransferPartnerAccount, удалена.
            // UUID получателя теперь передается через StatusMessage в классе Transaction

            TransactionStrategy strategy = strategyFactory.createStrategy(newTransaction.getActionType());

            TransactionCommand command = new TransactionCommand(
                    targetAccount,
                    newTransaction,
                    strategy,
                    this::handleCompletion,
                    accountDataAccess
            );

            commandExecutor.submit(command);
        } catch (Exception creationException) {
            newTransaction.setStatusMessage("ERROR: System failure during setup: " + creationException.getMessage());
            handleCompletion(newTransaction);
        }
    }

    private void handleCompletion(Transaction completedTransaction) {
        Optional<Account> accountAfterOperation = accountDataAccess.findAccountByIdentifier(completedTransaction.getAccountIdentifier());

        if (accountAfterOperation.isPresent()) {
            accountDataAccess.saveAccount(accountAfterOperation.get());
        }

        statusListeners.forEach(listener -> listener.notifyTransactionUpdate(completedTransaction));
    }

    public void shutdown() {
        commandExecutor.shutdown();
    }
}