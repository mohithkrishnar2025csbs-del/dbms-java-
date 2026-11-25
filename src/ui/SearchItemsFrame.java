package ui;

import model.FoundItem;
import model.LostItem;
import model.User;
import service.ItemService;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Search and Filter Interface for Lost and Found Campus Items.
 */
public class SearchItemsFrame extends JFrame {
    private final User currentUser;
    private final ItemService itemService;

    private JComboBox<String> itemTypeCombo;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JTextField locationField;
    private JTextField dateField;
    private JButton searchButton;
    private JButton resetButton;
    private JButton viewDetailsButton;
    private JButton claimButton;

    private JTable resultsTable;
    private DefaultTableModel tableModel;

    // Cache of current search results
    private List<LostItem> currentLostItems = new ArrayList<>();
    private List<FoundItem> currentFoundItems = new ArrayList<>();

    public SearchItemsFrame(User currentUser) {
        this.currentUser = currentUser;
        this.itemService = new ItemService();
        initComponents();
        performSearch(); // Initial load
    }

    private void initComponents() {
        setTitle("Search & Filter Items - Smart Lost-and-Found Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(880, 580));
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBackground(UITheme.COLOR_BG);
        contentPane.setBorder(new EmptyBorder(20, 25, 20, 25));

        // ----------------------------------------------------
        // Top Filter Panel
        // ----------------------------------------------------
        JPanel filterCard = new JPanel();
        filterCard.setBackground(Color.WHITE);
        filterCard.setLayout(new BorderLayout(10, 10));
        filterCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLbl = new JLabel("Search Campus Lost & Found Registry");
        titleLbl.setFont(UITheme.FONT_SUBHEADER);
        titleLbl.setForeground(UITheme.COLOR_PRIMARY);

        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        itemTypeCombo = new JComboBox<>(new String[]{"Found Items (Available to Claim)", "Lost Items"});
        itemTypeCombo.setFont(UITheme.FONT_REGULAR);
        itemTypeCombo.setBackground(Color.WHITE);

        searchField = UITheme.createTextField();
        searchField.setToolTipText("Search by keyword or item title");

        categoryCombo = UITheme.createComboBox(new String[]{
                "All Categories", "ID Card", "Wallet", "Mobile Phone", "Keys",
                "Books", "Electronics", "Bags", "Accessories", "Other"
        });

        locationField = UITheme.createTextField();
        dateField = UITheme.createTextField();
        dateField.setToolTipText("YYYY-MM-DD");

        searchButton = UITheme.createPrimaryButton("Search");
        resetButton = UITheme.createSecondaryButton("Reset Filters");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        controlsPanel.add(new JLabel("Catalog Type:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3;
        controlsPanel.add(itemTypeCombo, gbc);

        gbc.gridx = 2; gbc.weightx = 0.2;
        controlsPanel.add(new JLabel("Keywords:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.3;
        controlsPanel.add(searchField, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        controlsPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3;
        controlsPanel.add(categoryCombo, gbc);

        gbc.gridx = 2; gbc.weightx = 0.2;
        controlsPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.3;
        controlsPanel.add(locationField, gbc);

        // Row 3
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        controlsPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3;
        controlsPanel.add(dateField, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(resetButton);
        btnRow.add(searchButton);

        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 2;
        controlsPanel.add(btnRow, gbc);

        filterCard.add(titleLbl, BorderLayout.NORTH);
        filterCard.add(controlsPanel, BorderLayout.CENTER);

        // ----------------------------------------------------
        // Center Results Table
        // ----------------------------------------------------
        String[] columns = {"ID", "Item Name", "Category", "Location", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultsTable = new JTable(tableModel);
        UITheme.styleTable(resultsTable);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1));

        // ----------------------------------------------------
        // Bottom Actions Panel
        // ----------------------------------------------------
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomPanel.setOpaque(false);

        viewDetailsButton = UITheme.createSecondaryButton("View Item Details");
        viewDetailsButton.addActionListener(e -> viewSelectedDetails());

        claimButton = UITheme.createAccentButton("Claim This Item");
        claimButton.addActionListener(e -> claimSelectedItem());

        bottomPanel.add(viewDetailsButton);
        bottomPanel.add(claimButton);

        contentPane.add(filterCard, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);

        // Event listeners
        searchButton.addActionListener(e -> performSearch());
        resetButton.addActionListener(e -> {
            searchField.setText("");
            categoryCombo.setSelectedIndex(0);
            locationField.setText("");
            dateField.setText("");
            performSearch();
        });

        itemTypeCombo.addActionListener(e -> performSearch());
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        String location = locationField.getText().trim();
        String dateStr = dateField.getText().trim();
        boolean isFound = itemTypeCombo.getSelectedIndex() == 0;

        searchButton.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                tableModel.setRowCount(0);
                if (isFound) {
                    currentFoundItems = itemService.searchFoundItems(keyword, category, location, dateStr);
                } else {
                    currentLostItems = itemService.searchLostItems(keyword, category, location, dateStr);
                }
                return null;
            }

            @Override
            protected void done() {
                searchButton.setEnabled(true);
                if (isFound) {
                    for (FoundItem f : currentFoundItems) {
                        tableModel.addRow(new Object[]{
                                f.getFoundItemId(),
                                f.getItemName(),
                                f.getCategory(),
                                f.getFoundLocation(),
                                f.getFoundDate(),
                                f.getStatus()
                        });
                    }
                } else {
                    for (LostItem l : currentLostItems) {
                        tableModel.addRow(new Object[]{
                                l.getLostItemId(),
                                l.getItemName(),
                                l.getCategory(),
                                l.getLostLocation(),
                                l.getLostDate(),
                                l.getStatus()
                        });
                    }
                }
            }
        };
        worker.execute();
    }

    private void viewSelectedDetails() {
        int row = resultsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to view details.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isFound = itemTypeCombo.getSelectedIndex() == 0;
        int id = (int) tableModel.getValueAt(row, 0);

        if (isFound) {
            FoundItem item = currentFoundItems.stream().filter(f -> f.getFoundItemId() == id).findFirst().orElse(null);
            if (item != null) {
                showFoundDetailsDialog(item);
            }
        } else {
            LostItem item = currentLostItems.stream().filter(l -> l.getLostItemId() == id).findFirst().orElse(null);
            if (item != null) {
                showLostDetailsDialog(item);
            }
        }
    }

    private void showFoundDetailsDialog(FoundItem item) {
        String message = String.format(
                "Item Name: %s\n" +
                "Category: %s\n" +
                "Found Location: %s\n" +
                "Found Date: %s\n" +
                "Current Status: %s\n\n" +
                "Description:\n%s\n\n" +
                "(Note: Identifying details are verified privately during claim processing to prevent fraud.)",
                item.getItemName(), item.getCategory(), item.getFoundLocation(),
                item.getFoundDate(), item.getStatus(), item.getDescription()
        );
        JOptionPane.showMessageDialog(this, message, "Found Item Details (#" + item.getFoundItemId() + ")", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLostDetailsDialog(LostItem item) {
        String message = String.format(
                "Item Name: %s\n" +
                "Category: %s\n" +
                "Reported Lost Location: %s\n" +
                "Lost Date: %s\n" +
                "Current Status: %s\n\n" +
                "Description:\n%s\n",
                item.getItemName(), item.getCategory(), item.getLostLocation(),
                item.getLostDate(), item.getStatus(), item.getDescription()
        );
        JOptionPane.showMessageDialog(this, message, "Lost Item Details (#" + item.getLostItemId() + ")", JOptionPane.INFORMATION_MESSAGE);
    }

    private void claimSelectedItem() {
        int row = resultsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table to submit a claim.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isFound = itemTypeCombo.getSelectedIndex() == 0;
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String status = (String) tableModel.getValueAt(row, 5);

        if ("CLAIMED".equalsIgnoreCase(status) || "CLOSED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This item has already been claimed or closed.", "Item Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer lostId = !isFound ? id : null;
        Integer foundId = isFound ? id : null;

        new ClaimFrame(currentUser, lostId, foundId, name, () -> performSearch()).setVisible(true);
    }
}

