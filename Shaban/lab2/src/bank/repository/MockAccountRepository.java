package bank.repository;

import bank.core.models.Account;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MockAccountRepository implements AccountRepository {

    private final ConcurrentHashMap<UUID, Account> storageMap;

    public MockAccountRepository(List<Account> initialAccounts) {
        this.storageMap = new ConcurrentHashMap<>(
                initialAccounts.stream()
                        .collect(Collectors.toMap(Account::getAccountIdentifier, account -> account))
        );
    }

    @Override
    public Optional<Account> findAccountByIdentifier(UUID identifier) {
        return Optional.ofNullable(storageMap.get(identifier));
    }

    @Override
    public void saveAccount(Account accountToSave) {
        storageMap.put(accountToSave.getAccountIdentifier(), accountToSave);
    }

    public Optional<Account> findTransferPartnerById(UUID targetIdentifier) {
        return Optional.ofNullable(storageMap.get(targetIdentifier));
    }
}