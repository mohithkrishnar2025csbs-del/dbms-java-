package ui;

import model.Claim;
import model.User;
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
 * Screen displaying claims submitted by the logged-in student and their administrative verification status.
 */
public class MyClaimsFrame extends JFrame {
    private final User currentUser;
    private final ClaimService claimService;

    private JTable claimsTable;
    private DefaultTableModel tableModel;
    private List<Claim> currentClaims = new ArrayList<>();

    public MyClaimsFrame(User currentUser) {
        this.currentUser = currentUser;
        this.claimService = new ClaimService();
        initComponents();
        loadClaims();
    }

    private void initComponents() {
        setTitle("My Claims History - Smart Lost-and-Found Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(960, 600);
        setMinimumSize(new Dimension(840, 500));
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBackground(UITheme.COLOR_BG);
        contentPane.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLbl = new JLabel("My Ownership Claims");
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.COLOR_PRIMARY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton viewBtn = UITheme.createSecondaryButton("View Full Claim Details");
        viewBtn.addActionListener(e -> viewSelectedClaim());

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadClaims());

        btnPanel.add(viewBtn);
        btnPanel.add(refreshBtn);

        header.add(titleLbl, BorderLayout.WEST);
        header.add(btnPanel, BorderLayout.EAST);

        // Table
        String[] cols = {"Claim ID", "Target Item", "Type", "Claim Date", "Status", "Admin Remarks"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        claimsTable = new JTable(tableModel);
        UITheme.styleTable(claimsTable);
        JScrollPane scrollPane = new JScrollPane(claimsTable);
        scrollPane.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1));

        contentPane.add(header, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        setContentPane(contentPane);
    }

    public void loadClaims() {
        SwingWorker<List<Claim>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Claim> doInBackground() throws Exception {
                return claimService.getStudentClaims(currentUser.getUserId());
            }

            @Override
            protected void done() {
                try {
                    currentClaims = get();
                    tableModel.setRowCount(0);
                    if (currentClaims != null) {
                        for (Claim c : currentClaims) {
                            tableModel.addRow(new Object[]{
                                    c.getClaimId(),
                                    c.getTargetItemName(),
                                    c.getTargetItemType(),
                                    c.getClaimDate(),
                                    c.getClaimStatus(),
                                    c.getAdminRemarks() != null ? c.getAdminRemarks() : "(Pending Review)"
                            });
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void viewSelectedClaim() {
        int row = claimsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a claim from the table to view details.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int claimId = (int) tableModel.getValueAt(row, 0);
        Claim claim = currentClaims.stream().filter(c -> c.getClaimId() == claimId).findFirst().orElse(null);

        if (claim != null) {
            String details = String.format(
                    "Claim ID: #%d\n" +
                    "Target Item: %s (%s)\n" +
                    "Submission Date: %s\n" +
                    "Status: %s\n\n" +
                    "Your Ownership Explanation:\n%s\n\n" +
                    "Administrator Remarks:\n%s\n",
                    claim.getClaimId(),
                    claim.getTargetItemName(),
                    claim.getTargetItemType(),
                    claim.getClaimDate(),
                    claim.getClaimStatus(),
                    claim.getClaimDescription(),
                    claim.getAdminRemarks() != null ? claim.getAdminRemarks() : "Your claim is currently under review by campus security."
            );
            JOptionPane.showMessageDialog(this, details, "Claim Details (#" + claim.getClaimId() + ")", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

