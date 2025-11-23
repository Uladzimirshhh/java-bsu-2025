package bank.repository;

import bank.core.models.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Optional<Account> findAccountByIdentifier(UUID identifier);

    void saveAccount(Account accountToSave);
}