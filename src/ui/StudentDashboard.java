package ui;

import dao.ClaimDAO;
import dao.FoundItemDAO;
import dao.LostItemDAO;
import model.Claim;
import model.LostItem;
import model.User;
import service.AuthService;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern Student Dashboard featuring real-time KPI metrics, navigation sidebar,
 * recent campus reports, claim tracking, and portal switching.
 */
public class StudentDashboard extends JFrame {
    private final User currentUser;
    private final LostItemDAO lostItemDAO;
    private final FoundItemDAO foundItemDAO;
    private final ClaimDAO claimDAO;

    // KPI Card Labels
    private JLabel lostCountLbl;
    private JLabel foundCountLbl;
    private JLabel pendingClaimsLbl;
    private JLabel approvedClaimsLbl;

    // Tables
    private JTable recentLostTable;
    private DefaultTableModel recentLostModel;
    private JTable recentClaimsTable;
    private DefaultTableModel recentClaimsModel;

    private JLabel studentWelcomeLbl;

    public StudentDashboard(User currentUser) {
        this.currentUser = currentUser;
        this.lostItemDAO = new LostItemDAO();
        this.foundItemDAO = new FoundItemDAO();
        this.claimDAO = new ClaimDAO();
        initComponents();
        refreshDashboardData();
    }

    private void initComponents() {
        setTitle("Student Dashboard - Smart Lost-and-Found Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1040, 660));
        setLocationRelativeTo(null);

        JPanel rootPane = new JPanel(new BorderLayout());
        rootPane.setBackground(UITheme.COLOR_BG);

        // ----------------------------------------------------
        // 1. Left Sidebar Navigation
        // ----------------------------------------------------
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.COLOR_PRIMARY);
        sidebar.setPreferredSize(new Dimension(240, 720));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 15, 25, 15));

        JLabel brandTitle = new JLabel("LOST & FOUND");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSub = new JLabel("STUDENT PORTAL");
        brandSub.setFont(UITheme.FONT_SMALL);
        brandSub.setForeground(UITheme.COLOR_ACCENT);
        brandSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(brandTitle);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(brandSub);
        sidebar.add(Box.createVerticalStrut(22));

        // Navigation Buttons
        JButton btnHome = UITheme.createNavButton("  Dashboard Overview", true);
        JButton btnReportLost = UITheme.createNavButton("  Report Lost Item", false);
        JButton btnReportFound = UITheme.createNavButton("  Report Found Item", false);
        JButton btnSearch = UITheme.createNavButton("  Search & Claim Items", false);
        JButton btnMyReports = UITheme.createNavButton("  My Reports", false);
        JButton btnMyClaims = UITheme.createNavButton("  My Claim History", false);
        JButton btnProfile = UITheme.createNavButton("  My Profile", false);
        JButton btnSwitchPortal = UITheme.createNavButton("  Switch to Admin Portal", false);
        JButton btnReturnHome = UITheme.createNavButton("  Return to Home", false);
        JButton btnLogout = UITheme.createNavButton("  Sign Out", false);

        btnHome.addActionListener(e -> refreshDashboardData());
        btnReportLost.addActionListener(e -> openReportLost());
        btnReportFound.addActionListener(e -> openReportFound());
        btnSearch.addActionListener(e -> openSearch());
        btnMyReports.addActionListener(e -> openMyReports());
        btnMyClaims.addActionListener(e -> openMyClaims());
        btnProfile.addActionListener(e -> openProfile());
        btnSwitchPortal.addActionListener(e -> performSwitchPortal());
        btnReturnHome.addActionListener(e -> performReturnHome());
        btnLogout.addActionListener(e -> performLogout());

        sidebar.add(btnHome);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnReportLost);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnReportFound);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnSearch);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnMyReports);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnMyClaims);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnProfile);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnSwitchPortal);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnReturnHome);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnLogout);

        // ----------------------------------------------------
        // 2. Right Content Area
        // ----------------------------------------------------
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBackground(UITheme.COLOR_BG);
        mainContent.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel studentInfo = new JPanel();
        studentInfo.setLayout(new BoxLayout(studentInfo, BoxLayout.Y_AXIS));
        studentInfo.setOpaque(false);

        studentWelcomeLbl = new JLabel("Welcome back, " + currentUser.getName());
        studentWelcomeLbl.setFont(UITheme.FONT_HEADER);
        studentWelcomeLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel regLabel = new JLabel("Student ID: " + currentUser.getRegisterNumber() + "  |  " + currentUser.getEmail());
        regLabel.setFont(UITheme.FONT_REGULAR);
        regLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        studentInfo.add(studentWelcomeLbl);
        studentInfo.add(Box.createVerticalStrut(3));
        studentInfo.add(regLabel);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);
        JButton btnQuickSearch = UITheme.createAccentButton("Quick Search Catalog");
        btnQuickSearch.addActionListener(e -> openSearch());
        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> refreshDashboardData());
        headerActions.add(btnQuickSearch);
        headerActions.add(btnRefresh);

        topHeader.add(studentInfo, BorderLayout.WEST);
        topHeader.add(headerActions, BorderLayout.EAST);

        // Center Scrollable View
        JPanel bodyContainer = new JPanel();
        bodyContainer.setLayout(new BoxLayout(bodyContainer, BoxLayout.Y_AXIS));
        bodyContainer.setOpaque(false);

        // KPI Cards Grid (4 cards)
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        lostCountLbl = new JLabel("0");
        foundCountLbl = new JLabel("0");
        pendingClaimsLbl = new JLabel("0");
        approvedClaimsLbl = new JLabel("0");

        kpiGrid.add(buildKpiCard("My Lost Reports", lostCountLbl, UITheme.COLOR_DANGER));
        kpiGrid.add(buildKpiCard("My Found Reports", foundCountLbl, UITheme.COLOR_ACCENT));
        kpiGrid.add(buildKpiCard("Pending Claims", pendingClaimsLbl, UITheme.COLOR_WARNING));
        kpiGrid.add(buildKpiCard("Approved Claims", approvedClaimsLbl, UITheme.COLOR_SUCCESS));

        // Two Section Split: Recent Campus Lost Reports & My Recent Claim Activities
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setOpaque(false);

        // Left Table: Recent Campus Reports
        JPanel leftCard = new JPanel(new BorderLayout(8, 8));
        leftCard.setBackground(Color.WHITE);
        leftCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel leftTitle = new JLabel("Recent Campus Reports");
        leftTitle.setFont(UITheme.FONT_SUBHEADER);
        leftTitle.setForeground(UITheme.COLOR_PRIMARY);

        recentLostModel = new DefaultTableModel(new String[]{"ID", "Item Name", "Category", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recentLostTable = new JTable(recentLostModel);
        UITheme.styleTable(recentLostTable);
        JScrollPane leftScroll = new JScrollPane(recentLostTable);
        leftScroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1));

        leftCard.add(leftTitle, BorderLayout.NORTH);
        leftCard.add(leftScroll, BorderLayout.CENTER);

        // Right Table: My Recent Claims
        JPanel rightCard = new JPanel(new BorderLayout(8, 8));
        rightCard.setBackground(Color.WHITE);
        rightCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel rightTitle = new JLabel("My Recent Claim Activities");
        rightTitle.setFont(UITheme.FONT_SUBHEADER);
        rightTitle.setForeground(UITheme.COLOR_PRIMARY);

        recentClaimsModel = new DefaultTableModel(new String[]{"ID", "Target Item", "Status", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recentClaimsTable = new JTable(recentClaimsModel);
        UITheme.styleTable(recentClaimsTable);
        JScrollPane rightScroll = new JScrollPane(recentClaimsTable);
        rightScroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1));

        rightCard.add(rightTitle, BorderLayout.NORTH);
        rightCard.add(rightScroll, BorderLayout.CENTER);

        tablesPanel.add(leftCard);
        tablesPanel.add(rightCard);

        bodyContainer.add(kpiGrid);
        bodyContainer.add(Box.createVerticalStrut(20));
        bodyContainer.add(tablesPanel);

        mainContent.add(topHeader, BorderLayout.NORTH);
        mainContent.add(bodyContainer, BorderLayout.CENTER);

        rootPane.add(sidebar, BorderLayout.WEST);
        rootPane.add(mainContent, BorderLayout.CENTER);

        setContentPane(rootPane);
    }

    private JPanel buildKpiCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(UITheme.FONT_SMALL);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_STAT_VALUE);
        valueLabel.setForeground(color);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshDashboardData() {
        studentWelcomeLbl.setText("Welcome back, " + currentUser.getName());

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private int lostCount;
            private int foundCount;
            private int pendingCount;
            private int approvedCount;
            private List<LostItem> recentLost;
            private List<Claim> recentClaims;

            @Override
            protected Void doInBackground() throws Exception {
                lostCount = lostItemDAO.countByUserId(currentUser.getUserId());
                foundCount = foundItemDAO.countByUserId(currentUser.getUserId());
                pendingCount = claimDAO.countByUserAndStatus(currentUser.getUserId(), "PENDING");
                approvedCount = claimDAO.countByUserAndStatus(currentUser.getUserId(), "APPROVED");
                recentLost = lostItemDAO.findRecent(6);
                recentClaims = claimDAO.findByUserId(currentUser.getUserId());
                return null;
            }

            @Override
            protected void done() {
                lostCountLbl.setText(String.valueOf(lostCount));
                foundCountLbl.setText(String.valueOf(foundCount));
                pendingClaimsLbl.setText(String.valueOf(pendingCount));
                approvedClaimsLbl.setText(String.valueOf(approvedCount));

                // Populate recent items
                recentLostModel.setRowCount(0);
                if (recentLost != null) {
                    for (LostItem item : recentLost) {
                        recentLostModel.addRow(new Object[]{
                                item.getLostItemId(),
                                item.getItemName(),
                                item.getCategory(),
                                item.getStatus()
                        });
                    }
                }

                // Populate recent claims
                recentClaimsModel.setRowCount(0);
                if (recentClaims != null) {
                    int count = 0;
                    for (Claim c : recentClaims) {
                        if (count++ >= 6) break;
                        recentClaimsModel.addRow(new Object[]{
                                c.getClaimId(),
                                c.getTargetItemName(),
                                c.getClaimStatus(),
                                c.getClaimDate()
                        });
                    }
                }
            }
        };
        worker.execute();
    }

    private void openReportLost() {
        new LostItemFrame(currentUser, () -> refreshDashboardData()).setVisible(true);
    }

    private void openReportFound() {
        new FoundItemFrame(currentUser, () -> refreshDashboardData()).setVisible(true);
    }

    private void openSearch() {
        new SearchItemsFrame(currentUser).setVisible(true);
    }

    private void openMyReports() {
        new MyReportsFrame(currentUser).setVisible(true);
    }

    private void openMyClaims() {
        new MyClaimsFrame(currentUser).setVisible(true);
    }

    private void openProfile() {
        new ProfileFrame(currentUser, () -> refreshDashboardData()).setVisible(true);
    }

    private void performSwitchPortal() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Do you want to switch to the Admin Portal?",
                "Switch Portal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            dispose();
            new AdminLoginFrame().setVisible(true);
        }
    }

    private void performReturnHome() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Return to Main Home Page?",
                "Return to Home",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            dispose();
            new HomeFrame().setVisible(true);
        }
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of your session?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            dispose();
            new HomeFrame().setVisible(true);
        }
    }
}
