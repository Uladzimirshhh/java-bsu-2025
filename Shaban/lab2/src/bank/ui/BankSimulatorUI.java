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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class BankSimulatorUI extends JFrame implements TransactionStatusListener {

    private final List<Account> userAccounts;
    private final TransactionProcessor processor;
    private final MockAccountRepository repository;
    private final List<Transaction> completedTransactions;
    private final AtomicLong pendingTransactionCounter;

    private JLabel balanceLabel;
    private JTextArea historyTextArea;
    private JTextField amountInputField;
    private JComboBox<TransactionActionType> actionComboBox;
    private JComboBox<String> accountSelectionComboBox;
    private JComboBox<String> targetAccountSelectionComboBox;

    private JButton concurrentButton;

    public BankSimulatorUI(List<Account> accounts, TransactionProcessor transactionProcessor, MockAccountRepository repo) {
        super("Bank Transaction Simulator (Swing)");
        this.userAccounts = accounts;
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

    private Account getSelectedAccount() {
        if (accountSelectionComboBox.getSelectedItem() == null) return userAccounts.get(0);
        String selectedIdentifierFull = (String) accountSelectionComboBox.getSelectedItem();
        String selectedIdentifierPrefix = selectedIdentifierFull.split(" - ")[0];
        return userAccounts.stream()
                .filter(a -> a.getAccountIdentifier().toString().substring(0, 8).equals(selectedIdentifierPrefix))
                .findFirst()
                .orElse(userAccounts.get(0));
    }

    private void initializeUIComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension initialSize = new Dimension(880, 700);

        setPreferredSize(initialSize);
        setMinimumSize(new Dimension(850, 450));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topControlsWrapper = new JPanel(new BorderLayout());
        topControlsWrapper.add(createStatusPanel(), BorderLayout.NORTH);
        topControlsWrapper.add(createControlPanel(), BorderLayout.SOUTH);

        mainPanel.add(topControlsWrapper, BorderLayout.NORTH);

        historyTextArea = new JTextArea();
        historyTextArea.setEditable(false);
        historyTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyTextArea.setBackground(new Color(230, 230, 230));

        JScrollPane scrollPane = new JScrollPane(historyTextArea);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

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

        List<String> accountLabels = userAccounts.stream()
                .map(a -> a.getAccountIdentifier().toString().substring(0, 8) + " - Balance: " + a.getCurrentBalance().toPlainString())
                .collect(Collectors.toList());

        accountSelectionComboBox = new JComboBox<>(accountLabels.toArray(new String[0]));
        accountSelectionComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateBalanceDisplay();
            }
        });

        statusPanel.add(new JLabel("Active Account:"));
        statusPanel.add(accountSelectionComboBox);

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
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weighty = 0.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(new JLabel("Transaction Type:"), gbc);

        actionComboBox = new JComboBox<>(TransactionActionType.values());
        actionComboBox.addItemListener(e -> updateTargetSelectionPanel());

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(actionComboBox, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        controlPanel.add(new JLabel("Amount (0 for Freeze):"), gbc);

        amountInputField = new JTextField(10);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
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

        List<String> targetLabels = userAccounts.stream()
                .map(a -> a.getAccountIdentifier().toString().substring(0, 8))
                .collect(Collectors.toList());

        targetAccountSelectionComboBox = new JComboBox<>(targetLabels.toArray(new String[0]));

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.0;
        controlPanel.add(new JLabel("Target Account for Transfer:"), gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(targetAccountSelectionComboBox, gbc);

        updateTargetSelectionPanel();

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        controlPanel.add(new JSeparator(), gbc);

        concurrentButton = new JButton("Concurrent Test: Run 100x Deposit/Withdrawal");
        concurrentButton.setFont(new Font("Arial", Font.BOLD, 12));
        concurrentButton.setBorder(BorderFactory.createLineBorder(new Color(255, 152, 0), 2));
        concurrentButton.addActionListener(e -> processNewTransaction(true));

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(concurrentButton, gbc);

        JButton visitorButton = new JButton("Generate Report (Visitor Pattern)");
        visitorButton.setFont(new Font("Arial", Font.BOLD, 12));
        visitorButton.setBorder(BorderFactory.createLineBorder(new Color(33, 150, 243), 2));
        visitorButton.addActionListener(e -> generateReport());

        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlPanel.add(visitorButton, gbc);

        return controlPanel;
    }

    private void updateTargetSelectionPanel() {
        boolean isTransfer = actionComboBox.getSelectedItem() == TransactionActionType.TRANSFER;
        targetAccountSelectionComboBox.setVisible(isTransfer);

        Component[] components = targetAccountSelectionComboBox.getParent().getComponents();
        for (Component component : components) {
            if (component instanceof JLabel && ((JLabel) component).getText().startsWith("Target Account")) {
                component.setVisible(isTransfer);
            }
        }

        targetAccountSelectionComboBox.getParent().revalidate();
        targetAccountSelectionComboBox.getParent().repaint();
    }


    private void processNewTransaction(boolean isConcurrent) {
        Account activeAccount = getSelectedAccount();
        TransactionActionType selectedAction = (TransactionActionType) actionComboBox.getSelectedItem();
        String amountText = amountInputField.getText();
        BigDecimal transactionAmount = BigDecimal.ZERO;

        String targetAccountId = null;

        if (selectedAction != TransactionActionType.FREEZE && !amountText.isEmpty()) {
            try {
                transactionAmount = new BigDecimal(amountText);
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(this, "Invalid amount format.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (selectedAction == TransactionActionType.TRANSFER) {
            String selectedTargetPrefix = (String) targetAccountSelectionComboBox.getSelectedItem();
            targetAccountId = userAccounts.stream()
                    .filter(a -> selectedTargetPrefix.startsWith(a.getAccountIdentifier().toString().substring(0, 8)))
                    .map(a -> a.getAccountIdentifier().toString())
                    .findFirst()
                    .orElse(null);

            if (targetAccountId == null) {
                JOptionPane.showMessageDialog(this, "Target account not selected or invalid.", "Transfer Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (activeAccount.getAccountIdentifier().toString().equals(targetAccountId)) {
                JOptionPane.showMessageDialog(this, "Cannot transfer to the same account.", "Transfer Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (isConcurrent) {
            final BigDecimal concurrentAmount = new BigDecimal("1.00");
            for (int i = 0; i < 100; i++) {
                Transaction depositTransaction = new Transaction(TransactionActionType.DEPOSIT, concurrentAmount, activeAccount.getAccountIdentifier());
                pendingTransactionCounter.incrementAndGet();
                historyTextArea.append("\n[Concurrent D] Submitting ID " + depositTransaction.getTransactionIdentifier().toString().substring(0, 4) + "...");
                processor.submitTransaction(depositTransaction);

                Transaction withdrawalTransaction = new Transaction(TransactionActionType.WITHDRAWAL, concurrentAmount, activeAccount.getAccountIdentifier());
                pendingTransactionCounter.incrementAndGet();
                historyTextArea.append("\n[Concurrent W] Submitting ID " + withdrawalTransaction.getTransactionIdentifier().toString().substring(0, 4) + "...");
                processor.submitTransaction(withdrawalTransaction);
            }
        } else {
            Transaction newTransaction = new Transaction(selectedAction, transactionAmount, activeAccount.getAccountIdentifier());
            if (targetAccountId != null) {
                newTransaction.setStatusMessage(targetAccountId);
            }
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

            updateAccountComboBoxes(updatedTransaction);
            updateBalanceDisplay();
            historyTextArea.setCaretPosition(historyTextArea.getDocument().getLength());
        });
    }

    private void updateAccountComboBoxes(Transaction completedTransaction) {
        String sourceAccountId = completedTransaction.getAccountIdentifier().toString();

        String targetAccountId = null;
        if (completedTransaction.getActionType() == TransactionActionType.TRANSFER && completedTransaction.getStatusMessage().startsWith("SUCCESS")) {
            String status = completedTransaction.getStatusMessage();
            try {
                // Извлечение префикса цели (до '...') из: "SUCCESS: Funds transferred to 3c07a00c. Source New Balance: 956.00"
                // Префикс - это первая часть статуса, которая идет после 'Funds transferred to ' и до '.'
                String targetIdPrefix = status.split(" transferred to ")[1].split("\\.")[0].substring(0, 8).trim();

                final String targetIdPrefixFinal = targetIdPrefix;

                targetAccountId = userAccounts.stream()
                        .filter(a -> a.getAccountIdentifier().toString().substring(0, 8).equals(targetIdPrefixFinal))
                        .map(a -> a.getAccountIdentifier().toString())
                        .findFirst()
                        .orElse(null);

            } catch (ArrayIndexOutOfBoundsException ignored) {}
        }

        updateSingleComboBoxLabel(accountSelectionComboBox, sourceAccountId);

        if (targetAccountId != null) {
            updateSingleComboBoxLabel(accountSelectionComboBox, targetAccountId);
            updateSingleComboBoxLabel(targetAccountSelectionComboBox, targetAccountId);
        }
    }

    private void updateSingleComboBoxLabel(JComboBox<String> comboBox, String accountId) {
        final String accountIdFinal = accountId;

        String accountPrefix = accountId.substring(0, 8);

        Optional<Account> currentAccountOptional = userAccounts.stream()
                .filter(a -> a.getAccountIdentifier().toString().equals(accountIdFinal))
                .findFirst();

        if (currentAccountOptional.isEmpty()) return;
        Account account = currentAccountOptional.get();

        ItemListener listener = comboBox.getItemListeners().length > 0 ? comboBox.getItemListeners()[0] : null;
        if (listener != null) {
            comboBox.removeItemListener(listener);
        }

        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboBox.getModel();

        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).startsWith(accountPrefix)) {
                String newLabel = accountPrefix + " - Balance: " + account.getCurrentBalance().toPlainString();
                String currentSelection = (String) comboBox.getSelectedItem();

                model.removeElementAt(i);
                model.insertElementAt(newLabel, i);

                if (currentSelection != null && currentSelection.startsWith(accountPrefix)) {
                    comboBox.setSelectedIndex(i);
                }
                break;
            }
        }

        if (listener != null) {
            comboBox.addItemListener(listener);
        }
    }

    private void updateBalanceDisplay() {
        Account activeAccount = getSelectedAccount();
        String currentSelectionPrefix = activeAccount.getAccountIdentifier().toString().substring(0, 8);

        balanceLabel.setText(String.format("ID: %s | Balance: %s | Frozen: %s",
                currentSelectionPrefix,
                activeAccount.getCurrentBalance().toPlainString(),
                activeAccount.getIsFrozen()));

        if (activeAccount.getIsFrozen()) {
            balanceLabel.setForeground(new Color(255, 69, 0));
        } else {
            balanceLabel.setForeground(new Color(0, 102, 0));
        }
    }

    public static void main(String[] arguments) {
        Account initialAccount1 = new Account();
        initialAccount1.setCurrentBalance(new BigDecimal("1000.00"));

        Account initialAccount2 = new Account();
        initialAccount2.setCurrentBalance(new BigDecimal("200.00"));

        Account initialAccount3 = new Account();
        initialAccount3.setCurrentBalance(new BigDecimal("5000.00"));

        List<Account> allUserAccounts = Arrays.asList(initialAccount1, initialAccount2, initialAccount3);
        MockAccountRepository repository = new MockAccountRepository(allUserAccounts);
        TransactionStrategyFactory factory = new TransactionStrategyFactory();
        TransactionProcessor processor = TransactionProcessor.getTransactionProcessorInstance(repository, factory);

        SwingUtilities.invokeLater(() -> {
            BankSimulatorUI ui = new BankSimulatorUI(allUserAccounts, processor, repository);
            ui.setVisible(true);
        });
    }
}