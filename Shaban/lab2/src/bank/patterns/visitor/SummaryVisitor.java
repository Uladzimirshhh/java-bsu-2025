package bank.patterns.visitor;

import bank.core.models.Transaction;
import bank.patterns.strategy.TransactionActionType;

import java.math.BigDecimal;

public class SummaryVisitor implements TransactionVisitor {

    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private long failedCount;
    private long totalProcessedCount;

    public SummaryVisitor() {
        this.totalDeposits = BigDecimal.ZERO;
        this.totalWithdrawals = BigDecimal.ZERO;
        this.failedCount = 0;
        this.totalProcessedCount = 0;
    }

    @Override
    public void visit(Transaction transactionElement) {
        totalProcessedCount++;

        if (transactionElement.getStatusMessage().contains("FAILED")) {
            failedCount++;
            return;
        }

        if (transactionElement.getActionType() == TransactionActionType.DEPOSIT) {
            totalDeposits = totalDeposits.add(transactionElement.getTransactionAmount());
        } else if (transactionElement.getActionType() == TransactionActionType.WITHDRAWAL) {
            totalWithdrawals = totalWithdrawals.add(transactionElement.getTransactionAmount());
        }
    }

    public String getSummaryReport() {
        return "=== Transaction Summary ===\n" +
                "Total Transactions Processed: " + totalProcessedCount + "\n" +
                "Successful Deposits: " + totalDeposits.toPlainString() + "\n" +
                "Successful Withdrawals: " + totalWithdrawals.toPlainString() + "\n" +
                "Failed Transactions Count: " + failedCount;
    }
}