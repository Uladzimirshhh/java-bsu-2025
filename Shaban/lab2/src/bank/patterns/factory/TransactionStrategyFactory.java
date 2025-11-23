package bank.patterns.factory;

import bank.patterns.strategy.TransactionActionType;
import bank.patterns.strategy.TransactionStrategy;
import bank.patterns.strategy.DepositStrategy;
import bank.patterns.strategy.WithdrawalStrategy;
import bank.patterns.strategy.FreezeStrategy;
import bank.patterns.strategy.TransferStrategy;

public class TransactionStrategyFactory {

    public TransactionStrategy createStrategy(TransactionActionType actionType) {
        switch (actionType) {
            case DEPOSIT:
                return new DepositStrategy();
            case WITHDRAWAL:
                return new WithdrawalStrategy();
            case FREEZE:
                return new FreezeStrategy();
            case TRANSFER:
                return new TransferStrategy();
            default:
                throw new IllegalArgumentException("Unknown action type: " + actionType);
        }
    }
}