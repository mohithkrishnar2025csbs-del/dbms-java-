package ui;

import model.Claim;
import model.User;
import service.AdminService;
import service.ClaimService;
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
 * Administrator Claim Review & Verification Interface with atomic transactional approvals.
 * Supports searching by claim ID, student name, item name, filtering by status,
 * adding remarks, and logging all operations into the activity audit trail.
 */
public class ClaimManagementFrame extends JFrame {
    private final User adminUser;
    private final ClaimService claimService;
    private final AdminService adminService;
    private final Runnable onClaimProcessed;

    private JComboBox<String> statusFilterCombo;
    private JTextField searchField;
    private JTable claimsTable;
    private DefaultTableModel tableModel;
    private List<Claim> claimsList = new ArrayList<>();

    // Details Panel Components
    private JLabel claimantNameLbl;
    private JLabel claimantRegLbl;
    private JLabel claimantEmailLbl;
    private JLabel targetItemLbl;
    private JLabel targetLocationLbl;
    private JLabel claimStatusLbl;
    private JTextArea claimExplanationArea;
    private JTextArea adminRemarksArea;

    private JButton approveBtn;
    private JButton rejectBtn;
    private JButton saveRemarksBtn;
    private JButton refreshBtn;
    private JButton viewDetailsBtn;

    public ClaimManagementFrame(User adminUser, Runnable onClaimProcessed) {
        this.adminUser = adminUser;
        this.onClaimProcessed = onClaimProcessed;
        this.claimService = new ClaimService();
        this.adminService = new AdminService();
        initComponents();
        loadClaims();
    }

    private void initComponents() {
        setTitle("Claim Verification & Management - Administrator");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1140, 720);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBackground(UITheme.COLOR_BG);
        contentPane.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Top Filter & Search Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel titleLbl = new JLabel("Claim Verification & Decision Processing");
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.COLOR_PRIMARY);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);

        statusFilterCombo = new JComboBox<>(new String[]{"ALL", "PENDING", "APPROVED", "REJECTED"});
        statusFilterCombo.setFont(UITheme.FONT_REGULAR);
        statusFilterCombo.setBackground(Color.WHITE);
        statusFilterCombo.addActionListener(e -> loadClaims());

        searchField = UITheme.createTextField();
        searchField.setPreferredSize(new Dimension(180, 36));
        searchField.setToolTipText("Search Claim ID, Student Name, Item Name");

        JButton searchBtn = UITheme.createPrimaryButton("Search");
        searchBtn.addActionListener(e -> loadClaims());

        refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedItem("ALL");
            loadClaims();
        });

        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilterCombo);
        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(searchField);
        filterPanel.add(searchBtn);
        filterPanel.add(refreshBtn);

        topBar.add(titleLbl, BorderLayout.WEST);
        topBar.add(filterPanel, BorderLayout.EAST);

        // Center Split: Left Table, Right Verification Details
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 18, 0));
        centerPanel.setOpaque(false);

        // Left Table
        String[] cols = {
                "Claim ID", "Student Name", "Register Number",
                "Related Item Name", "Item Type", "Claim Description",
                "Claim Date", "Claim Status", "Admin Remarks"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        claimsTable = new JTable(tableModel);
        UITheme.styleTable(claimsTable);
        JScrollPane tableScroll = new JScrollPane(claimsTable);
        tableScroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1));

        claimsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                displaySelectedClaim();
            }
        });

        // Right Verification Card
        JPanel reviewCard = new JPanel();
        reviewCard.setBackground(Color.WHITE);
        reviewCard.setLayout(new BoxLayout(reviewCard, BoxLayout.Y_AXIS));
        reviewCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JLabel reviewTitle = new JLabel("Claim & Ownership Verification");
        reviewTitle.setFont(UITheme.FONT_SUBHEADER);
        reviewTitle.setForeground(UITheme.COLOR_PRIMARY);

        claimantNameLbl = new JLabel("-");
        claimantRegLbl = new JLabel("-");
        claimantEmailLbl = new JLabel("-");
        targetItemLbl = new JLabel("-");
        targetLocationLbl = new JLabel("-");
        claimStatusLbl = new JLabel("-");

        claimExplanationArea = UITheme.createTextArea(3, 20);
        claimExplanationArea.setEditable(false);
        claimExplanationArea.setBackground(new Color(248, 250, 252));
        JScrollPane expScroll = new JScrollPane(claimExplanationArea);
        expScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        adminRemarksArea = UITheme.createTextArea(3, 20);
        JScrollPane remScroll = new JScrollPane(adminRemarksArea);
        remScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionRow.setOpaque(false);

        viewDetailsBtn = UITheme.createSecondaryButton("View Details");
        saveRemarksBtn = UITheme.createSecondaryButton("Add Remarks");
        rejectBtn = UITheme.createDangerButton("Reject Claim");
        approveBtn = UITheme.createAccentButton("Approve Claim");

        approveBtn.setEnabled(false);
        rejectBtn.setEnabled(false);
        saveRemarksBtn.setEnabled(false);

        viewDetailsBtn.addActionListener(e -> viewDetailsDialog());
        saveRemarksBtn.addActionListener(e -> addAdminRemarksOnly());
        approveBtn.addActionListener(e -> processApproval());
        rejectBtn.addActionListener(e -> processRejection());

        actionRow.add(viewDetailsBtn);
        actionRow.add(saveRemarksBtn);
        actionRow.add(rejectBtn);
        actionRow.add(approveBtn);

        reviewCard.add(reviewTitle);
        reviewCard.add(Box.createVerticalStrut(10));

        addDetailRow(reviewCard, "Claimant Name:", claimantNameLbl);
        addDetailRow(reviewCard, "Register Number:", claimantRegLbl);
        addDetailRow(reviewCard, "Campus Email:", claimantEmailLbl);
        addDetailRow(reviewCard, "Target Item:", targetItemLbl);
        addDetailRow(reviewCard, "Reported Location:", targetLocationLbl);
        addDetailRow(reviewCard, "Current Status:", claimStatusLbl);

        JLabel explHeader = new JLabel("Claimant's Proof / Ownership Description:");
        explHeader.setFont(UITheme.FONT_BOLD);
        explHeader.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        reviewCard.add(explHeader);
        reviewCard.add(Box.createVerticalStrut(3));
        reviewCard.add(expScroll);
        reviewCard.add(Box.createVerticalStrut(8));

        JLabel remHeader = new JLabel("Administrator Remarks / Decision Justification:");
        remHeader.setFont(UITheme.FONT_BOLD);
        remHeader.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        reviewCard.add(remHeader);
        reviewCard.add(Box.createVerticalStrut(3));
        reviewCard.add(remScroll);
        reviewCard.add(Box.createVerticalStrut(12));
        reviewCard.add(actionRow);

        centerPanel.add(tableScroll);
        centerPanel.add(reviewCard);

        contentPane.add(topBar, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);

        setContentPane(contentPane);
    }

    private void addDetailRow(JPanel panel, String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(5, 5));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.COLOR_TEXT_MUTED);
        l.setPreferredSize(new Dimension(135, 20));

        value.setFont(UITheme.FONT_REGULAR);
        value.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        row.add(l, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        panel.add(row);
        panel.add(Box.createVerticalStrut(3));
    }

    public void loadClaims() {
        String filter = (String) statusFilterCombo.getSelectedItem();
        String query = searchField.getText().trim();
        tableModel.setRowCount(0);
        clearDetails();

        SwingWorker<List<Claim>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Claim> doInBackground() throws Exception {
                return adminService.searchClaims(filter, query);
            }

            @Override
            protected void done() {
                try {
                    claimsList = get();
                    if (claimsList != null) {
                        for (Claim c : claimsList) {
                            tableModel.addRow(new Object[]{
                                    c.getClaimId(),
                                    c.getClaimantName(),
                                    c.getClaimantRegisterNumber(),
                                    c.getTargetItemName(),
                                    c.getTargetItemType(),
                                    c.getClaimDescription(),
                                    c.getClaimDate(),
                                    c.getClaimStatus(),
                                    c.getAdminRemarks() != null ? c.getAdminRemarks() : ""
                            });
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ClaimManagementFrame.this, "Error loading claims: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void displaySelectedClaim() {
        int row = claimsTable.getSelectedRow();
        if (row < 0) {
            clearDetails();
            return;
        }

        int claimId = (int) tableModel.getValueAt(row, 0);
        Claim claim = claimsList.stream().filter(c -> c.getClaimId() == claimId).findFirst().orElse(null);

        if (claim != null) {
            claimantNameLbl.setText(claim.getClaimantName());
            claimantRegLbl.setText(claim.getClaimantRegisterNumber());
            claimantEmailLbl.setText(claim.getClaimantEmail());
            targetItemLbl.setText(claim.getTargetItemName() + " (" + claim.getTargetItemType() + ")");
            targetLocationLbl.setText(claim.getTargetItemLocation() != null ? claim.getTargetItemLocation() : "-");
            claimStatusLbl.setText(claim.getClaimStatus());
            claimExplanationArea.setText(claim.getClaimDescription());
            adminRemarksArea.setText(claim.getAdminRemarks() != null ? claim.getAdminRemarks() : "");

            boolean isPending = "PENDING".equalsIgnoreCase(claim.getClaimStatus());
            approveBtn.setEnabled(isPending);
            rejectBtn.setEnabled(isPending);
            saveRemarksBtn.setEnabled(true);
        }
    }

    private void clearDetails() {
        claimantNameLbl.setText("-");
        claimantRegLbl.setText("-");
        claimantEmailLbl.setText("-");
        targetItemLbl.setText("-");
        targetLocationLbl.setText("-");
        claimStatusLbl.setText("-");
        claimExplanationArea.setText("");
        adminRemarksArea.setText("");
        approveBtn.setEnabled(false);
        rejectBtn.setEnabled(false);
        saveRemarksBtn.setEnabled(false);
    }

    private void viewDetailsDialog() {
        int row = claimsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a claim from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int claimId = (int) tableModel.getValueAt(row, 0);
        Claim c = claimsList.stream().filter(cl -> cl.getClaimId() == claimId).findFirst().orElse(null);
        if (c == null) return;

        String msg = String.format(
                "Claim Details:\n\n" +
                "Claim ID: #%d\n" +
                "Claimant Student: %s (Register No: %s)\n" +
                "Student Email: %s\n\n" +
                "Related Item: %s\n" +
                "Item Type: %s Item\n" +
                "Reported Location: %s\n" +
                "Current Item Status: %s\n\n" +
                "Claim Submission Date: %s\n" +
                "Claim Status: %s\n\n" +
                "Ownership Proof Description:\n\"%s\"\n\n" +
                "Admin Remarks:\n\"%s\"\n",
                c.getClaimId(), c.getClaimantName(), c.getClaimantRegisterNumber(),
                c.getClaimantEmail(), c.getTargetItemName(), c.getTargetItemType(),
                c.getTargetItemLocation() != null ? c.getTargetItemLocation() : "-",
                c.getTargetItemStatus() != null ? c.getTargetItemStatus() : "-",
                c.getClaimDate(), c.getClaimStatus(), c.getClaimDescription(),
                c.getAdminRemarks() != null ? c.getAdminRemarks() : "[None]"
        );

        JOptionPane.showMessageDialog(this, msg, "Claim Record #" + claimId, JOptionPane.INFORMATION_MESSAGE);
    }

    private void addAdminRemarksOnly() {
        int row = claimsTable.getSelectedRow();
        if (row < 0) return;

        int claimId = (int) tableModel.getValueAt(row, 0);
        String remarks = adminRemarksArea.getText().trim();

        if (remarks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter administrator remarks before saving.", "Remarks Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean ok = adminService.addAdminRemarks(adminUser.getUserId(), claimId, remarks);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Administrator remarks saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
                loadClaims();
                if (onClaimProcessed != null) onClaimProcessed.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving remarks: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processApproval() {
        int row = claimsTable.getSelectedRow();
        if (row < 0) return;

        int claimId = (int) tableModel.getValueAt(row, 0);
        String remarks = adminRemarksArea.getText().trim();

        if (remarks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter administrator verification remarks before approving.", "Remarks Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Approve Claim #" + claimId + "?\n\n" +
                "This action will:\n" +
                "1. Update this claim to APPROVED.\n" +
                "2. Mark the related item as CLAIMED in the database.\n" +
                "3. Automatically reject any conflicting pending claims.\n" +
                "4. Record the approval in the administration activity audit log.",
                "Confirm Approval",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = adminService.approveClaim(adminUser.getUserId(), claimId, remarks);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Claim approved successfully and item marked as CLAIMED.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadClaims();
                    if (onClaimProcessed != null) onClaimProcessed.run();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error approving claim: " + ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void processRejection() {
        int row = claimsTable.getSelectedRow();
        if (row < 0) return;

        int claimId = (int) tableModel.getValueAt(row, 0);
        String remarks = adminRemarksArea.getText().trim();

        if (remarks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the reason for rejection in Administrator Remarks.", "Remarks Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reject Claim #" + claimId + "?",
                "Confirm Rejection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = adminService.rejectClaim(adminUser.getUserId(), claimId, remarks);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Claim has been rejected.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadClaims();
                    if (onClaimProcessed != null) onClaimProcessed.run();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error rejecting claim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
