package bank.patterns.visitor;

import bank.core.models.Transaction;

public interface TransactionVisitor {
    void visit(Transaction transactionElement);
}