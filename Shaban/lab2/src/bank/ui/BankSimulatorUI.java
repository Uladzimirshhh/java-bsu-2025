package bank.ui;

import bank.core.models.Account;
import bank.core.models.Transaction;
import bank.patterns.factory.TransactionStrategyFactory;
import bank.patterns.observer.TransactionStatusListener;
import bank.patterns.strategy.TransactionActionType;
import bank.patterns.visitor.SummaryVisitor;
import bank.repository.MockAccountRepository;
import bank.service.TransactionProcessor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BankSimulatorUI extends JFrame implements TransactionStatusListener {

    private final Account userAccount;
    private final TransactionProcessor processor;
    private final MockAccountRepository repository;
    private final List<Transaction> completedTransactions;
    private final AtomicLong pendingTransactionCounter;

    private JLabel balanceLabel;
    private JTextArea historyTextArea;
    private JTextField amountInputField;
    private JComboBox<TransactionActionType> actionComboBox;

    private JButton concurrentButton;

    public BankSimulatorUI(Account account, TransactionProcessor transactionProcessor, MockAccountRepository repo) {
        super("Bank Transaction Simulator (Swing)");
        this.userAccount = account;
        this.processor = transactionProcessor;
        this.repository = repo;
        this.completedTransactions = new ArrayList<>();
        this.pendingTransactionCounter = new AtomicLong(0);

        this.processor.registerListener(this);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {

        }

        initializeUIComponents();
        updateBalanceDisplay();
    }

    private void initializeUIComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension initialSize = new Dimension(880, 700);

        setPreferredSize(initialSize);
        setMinimumSize(new Dimension(850, 550));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.NORTH);

        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.CENTER);

        historyTextArea = new JTextArea();
        historyTextArea.setEditable(false);
        historyTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyTextArea.setBackground(new Color(230, 230, 230));

        JScrollPane scrollPane = new JScrollPane(historyTextArea);
        scrollPane.setPreferredSize(new Dimension(800, 450));

        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        add(mainPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                processor.shutdown();
            }
        });
        setLocationRelativeTo(null);
        pack();
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                "Current Account Status"
        ));

        balanceLabel = new JLabel();
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));

        statusPanel.add(balanceLabel);
        return statusPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 102), 1),
                "Transaction Control"
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(new JLabel("Transaction Type:"), gbc);

        actionComboBox = new JComboBox<>(TransactionActionType.values());
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(actionComboBox, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(new JLabel("Amount (0 for Freeze):"), gbc);

        amountInputField = new JTextField(15);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(amountInputField, gbc);

        JButton submitButton = new JButton("Execute Single Transaction");
        submitButton.setFont(new Font("Arial", Font.BOLD, 12));
        submitButton.setBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 2));

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(submitButton, gbc);

        submitButton.addActionListener(e -> processNewTransaction(false));

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        controlPanel.add(new JSeparator(), gbc);

        concurrentButton = new JButton("Concurrent Test: Run 100x Deposit/Withdrawal");
        concurrentButton.setFont(new Font("Arial", Font.BOLD, 12));
        concurrentButton.setBorder(BorderFactory.createLineBorder(new Color(255, 152, 0), 2));
        concurrentButton.addActionListener(e -> processNewTransaction(true));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(concurrentButton, gbc);

        JButton visitorButton = new JButton("Generate Report (Visitor Pattern)");
        visitorButton.setFont(new Font("Arial", Font.BOLD, 12));
        visitorButton.setBorder(BorderFactory.createLineBorder(new Color(33, 150, 243), 2));
        visitorButton.addActionListener(e -> generateReport());

        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(visitorButton, gbc);

        return controlPanel;
    }

    private void processNewTransaction(boolean isConcurrent) {
        TransactionActionType selectedAction = (TransactionActionType) actionComboBox.getSelectedItem();
        String amountText = amountInputField.getText();
        BigDecimal transactionAmount = BigDecimal.ZERO;

        if (selectedAction != TransactionActionType.FREEZE && !amountText.isEmpty()) {
            try {
                transactionAmount = new BigDecimal(amountText);
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(this, "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (selectedAction == TransactionActionType.TRANSFER) {
            Account transferPartner = repository.getTransferPartnerAccount(userAccount.getAccountIdentifier());
            if (transferPartner == null) {
                JOptionPane.showMessageDialog(this, "No partner account available for transfer.", "Transfer Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (isConcurrent) {
            final BigDecimal concurrentAmount = new BigDecimal("1.00");
            for (int i = 0; i < 100; i++) {
                Transaction depositTransaction = new Transaction(TransactionActionType.DEPOSIT, concurrentAmount, userAccount.getAccountIdentifier());
                pendingTransactionCounter.incrementAndGet();
                historyTextArea.append("\n[Concurrent D] Submitting ID " + depositTransaction.getTransactionIdentifier().toString().substring(0, 4) + "...");
                processor.submitTransaction(depositTransaction);

                Transaction withdrawalTransaction = new Transaction(TransactionActionType.WITHDRAWAL, concurrentAmount, userAccount.getAccountIdentifier());
                pendingTransactionCounter.incrementAndGet();
                historyTextArea.append("\n[Concurrent W] Submitting ID " + withdrawalTransaction.getTransactionIdentifier().toString().substring(0, 4) + "...");
                processor.submitTransaction(withdrawalTransaction);
            }
        } else {
            Transaction newTransaction = new Transaction(selectedAction, transactionAmount, userAccount.getAccountIdentifier());
            pendingTransactionCounter.incrementAndGet();
            historyTextArea.append("\n[Submitting] " + newTransaction.getActionType().name() + " Transaction ID: " + newTransaction.getTransactionIdentifier().toString().substring(0, 4) + "...");
            processor.submitTransaction(newTransaction);
        }
        amountInputField.setText("");
    }

    private void generateReport() {
        if (pendingTransactionCounter.get() > 0) {
            JOptionPane.showMessageDialog(this, "Please wait for " + pendingTransactionCounter.get() + " transactions to finish.", "Visitor Running", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        SummaryVisitor reportVisitor = new SummaryVisitor();

        synchronized (completedTransactions) {
            for (Transaction t : completedTransactions) {
                t.accept(reportVisitor);
            }
        }

        JOptionPane.showMessageDialog(this, reportVisitor.getSummaryReport(), "Transaction Summary Report (Visitor)", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void notifyTransactionUpdate(Transaction updatedTransaction) {
        SwingUtilities.invokeLater(() -> {
            long remaining = pendingTransactionCounter.decrementAndGet();

            String statusColor = updatedTransaction.getStatusMessage().startsWith("SUCCESS") ? "[GREEN]" : "[RED]";

            synchronized (completedTransactions) {
                completedTransactions.add(updatedTransaction);
            }
            historyTextArea.append("\n[Completed - " + remaining + " left] " + statusColor + updatedTransaction.toString());

            updateBalanceDisplay();
            historyTextArea.setCaretPosition(historyTextArea.getDocument().getLength());
        });
    }

    private void updateBalanceDisplay() {
        balanceLabel.setText(String.format("Balance: %s | ID: %s | Frozen: %s",
                userAccount.getCurrentBalance().toPlainString(),
                userAccount.getAccountIdentifier().toString().substring(0, 8),
                userAccount.getIsFrozen()));

        if (userAccount.getIsFrozen()) {
            balanceLabel.setForeground(new Color(255, 69, 0));
        } else {
            balanceLabel.setForeground(new Color(0, 102, 0));
        }
    }

    public static void main(String[] arguments) {
        Account initialAccount1 = new Account();
        initialAccount1.setCurrentBalance(new BigDecimal("1000.00"));

        Account initialAccount2 = new Account();

        MockAccountRepository repository = new MockAccountRepository(Arrays.asList(initialAccount1, initialAccount2));
        TransactionStrategyFactory factory = new TransactionStrategyFactory();
        TransactionProcessor processor = TransactionProcessor.getTransactionProcessorInstance(repository, factory);

        SwingUtilities.invokeLater(() -> {
            BankSimulatorUI ui = new BankSimulatorUI(initialAccount1, processor, repository);
            ui.setVisible(true);
        });
    }
}