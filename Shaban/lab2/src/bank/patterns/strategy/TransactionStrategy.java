package bank.patterns.strategy;

import bank.core.models.Account;
import bank.core.models.Transaction;
import bank.repository.AccountRepository;

public interface TransactionStrategy {

    boolean executeTransaction(Account targetAccount, Transaction transactionData, AccountRepository repository);
}