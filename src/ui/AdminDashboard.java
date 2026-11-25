package ui;

import model.ActivityLog;
import model.Claim;
import model.FoundItem;
import model.LostItem;
import model.User;
import service.AdminService;
import service.AuthService;
import service.ClaimService;
import service.ItemService;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Modern Comprehensive Administrator Dashboard.
 * Includes Overview KPIs, Student Management, Lost Item Moderation,
 * Found Item Moderation, Claim Management, and Activity Audit Log.
 */
public class AdminDashboard extends JFrame {
    private final User adminUser;
    private final AdminService adminService;
    private final ItemService itemService;
    private final ClaimService claimService;

    // CardLayout container
    private CardLayout cardLayout;
    private JPanel contentCards;

    // Sidebar navigation buttons
    private JButton btnNavDashboard;
    private JButton btnNavStudents;
    private JButton btnNavLostItems;
    private JButton btnNavFoundItems;
    private JButton btnNavClaims;
    private JButton btnNavActivity;

    // 1. Overview KPI Labels & Models
    private JLabel totalStudentsLbl;
    private JLabel totalLostLbl;
    private JLabel totalFoundLbl;
    private JLabel pendingClaimsLbl;
    private JLabel approvedClaimsLbl;
    private JLabel rejectedClaimsLbl;
    private DefaultTableModel recentLostModel;
    private DefaultTableModel recentClaimsModel;

    // 2. Student Management Components
    private JTextField studentSearchField;
    private JComboBox<String> studentStatusFilterCombo;
    private DefaultTableModel studentsModel;
    private JTable studentsTable;
    private List<User> currentStudentsList = new ArrayList<>();

    // 3. Lost Items Components
    private JTextField lostSearchField;
    private JComboBox<String> lostCategoryCombo;
    private JTextField lostLocationField;
    private JTextField lostDateField;
    private DefaultTableModel lostModel;
    private JTable lostTable;
    private List<LostItem> currentLostList = new ArrayList<>();

    // 4. Found Items Components
    private JTextField foundSearchField;
    private JComboBox<String> foundCategoryCombo;
    private JTextField foundLocationField;
    private JTextField foundDateField;
    private DefaultTableModel foundModel;
    private JTable foundTable;
    private List<FoundItem> currentFoundList = new ArrayList<>();

    // 5. Claim Management Components
    private JComboBox<String> claimStatusCombo;
    private JTextField claimSearchField;
    private DefaultTableModel claimsModel;
    private JTable claimsTable;
    private List<Claim> currentClaimsList = new ArrayList<>();
    private JLabel claimTargetItemLbl;
    private JLabel claimStudentNameLbl;
    private JLabel claimRegNoLbl;
    private JLabel claimEmailLbl;
    private JTextArea claimProofArea;
    private JTextArea claimAdminRemarksArea;
    private JButton btnApproveClaim;
    private JButton btnRejectClaim;
    private JButton btnSaveRemarks;

    // 6. Activity History Components
    private JComboBox<String> activityFilterCombo;
    private JTextField activitySearchField;
    private DefaultTableModel activityModel;
    private JTable activityTable;

    public AdminDashboard(User adminUser) {
        this.adminUser = adminUser;
        this.adminService = new AdminService();
        this.itemService = new ItemService();
        this.claimService = new ClaimService();
        initComponents();
        loadOverviewData();
    }

    private void initComponents() {
        setTitle("Campus Security Administration - Smart Lost-and-Found Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 780);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel rootPane = new JPanel(new BorderLayout());
        rootPane.setBackground(UITheme.COLOR_BG);

        // ----------------------------------------------------
        // 1. Sidebar Navigation
        // ----------------------------------------------------
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.COLOR_PRIMARY_DARK);
        sidebar.setPreferredSize(new Dimension(245, 750));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 14, 25, 14));

        JLabel brandTitle = new JLabel("CAMPUS ADMIN");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSub = new JLabel("SECURITY & MANAGEMENT");
        brandSub.setFont(UITheme.FONT_SMALL);
        brandSub.setForeground(UITheme.COLOR_ACCENT);
        brandSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(brandTitle);
        sidebar.add(Box.createVerticalStrut(3));
        sidebar.add(brandSub);
        sidebar.add(Box.createVerticalStrut(22));

        btnNavDashboard = UITheme.createNavButton("  Dashboard", true);
        btnNavStudents = UITheme.createNavButton("  Student Management", false);
        btnNavLostItems = UITheme.createNavButton("  Lost Items", false);
        btnNavFoundItems = UITheme.createNavButton("  Found Items", false);
        btnNavClaims = UITheme.createNavButton("  Claim Management", false);
        btnNavActivity = UITheme.createNavButton("  Activity History", false);

        JButton btnSwitchPortal = UITheme.createNavButton("  Switch to Student Portal", false);
        JButton btnReturnHome = UITheme.createNavButton("  Return to Home", false);
        JButton btnLogout = UITheme.createNavButton("  Sign Out", false);

        btnNavDashboard.addActionListener(e -> { setActiveNav(btnNavDashboard); showCard("DASHBOARD"); loadOverviewData(); });
        btnNavStudents.addActionListener(e -> { setActiveNav(btnNavStudents); showCard("STUDENTS"); loadStudentsData(); });
        btnNavLostItems.addActionListener(e -> { setActiveNav(btnNavLostItems); showCard("LOST_ITEMS"); loadLostItems(); });
        btnNavFoundItems.addActionListener(e -> { setActiveNav(btnNavFoundItems); showCard("FOUND_ITEMS"); loadFoundItems(); });
        btnNavClaims.addActionListener(e -> { setActiveNav(btnNavClaims); showCard("CLAIMS"); loadClaimsData(); });
        btnNavActivity.addActionListener(e -> { setActiveNav(btnNavActivity); showCard("ACTIVITY"); loadActivityData(); });

        btnSwitchPortal.addActionListener(e -> performSwitchPortal());
        btnReturnHome.addActionListener(e -> performReturnHome());
        btnLogout.addActionListener(e -> performLogout());

        sidebar.add(btnNavDashboard);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnNavStudents);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnNavLostItems);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnNavFoundItems);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnNavClaims);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnNavActivity);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnSwitchPortal);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnReturnHome);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnLogout);

        // ----------------------------------------------------
        // 2. Right Main Area: Top Bar + CardLayout Center
        // ----------------------------------------------------
        JPanel mainArea = new JPanel(new BorderLayout(15, 15));
        mainArea.setBackground(UITheme.COLOR_BG);
        mainArea.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel adminMeta = new JPanel();
        adminMeta.setLayout(new BoxLayout(adminMeta, BoxLayout.Y_AXIS));
        adminMeta.setOpaque(false);

        JLabel adminWelcome = new JLabel("Administrator Control Center");
        adminWelcome.setFont(UITheme.FONT_HEADER);
        adminWelcome.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel roleLabel = new JLabel("Signed in as: " + adminUser.getName() + "  |  " + adminUser.getEmail());
        roleLabel.setFont(UITheme.FONT_REGULAR);
        roleLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        adminMeta.add(adminWelcome);
        adminMeta.add(Box.createVerticalStrut(2));
        adminMeta.add(roleLabel);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);

        JButton btnQuickClaimReview = UITheme.createAccentButton("Review Pending Claims");
        btnQuickClaimReview.addActionListener(e -> {
            setActiveNav(btnNavClaims);
            showCard("CLAIMS");
            claimStatusCombo.setSelectedItem("PENDING");
            loadClaimsData();
        });

        JButton btnRefreshOverview = UITheme.createSecondaryButton("Refresh All");
        btnRefreshOverview.addActionListener(e -> loadOverviewData());

        headerActions.add(btnQuickClaimReview);
        headerActions.add(btnRefreshOverview);

        topHeader.add(adminMeta, BorderLayout.WEST);
        topHeader.add(headerActions, BorderLayout.EAST);

        // Center Cards Container
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setOpaque(false);

        contentCards.add(buildDashboardPanel(), "DASHBOARD");
        contentCards.add(buildStudentsPanel(), "STUDENTS");
        contentCards.add(buildLostItemsPanel(), "LOST_ITEMS");
        contentCards.add(buildFoundItemsPanel(), "FOUND_ITEMS");
        contentCards.add(buildClaimsPanel(), "CLAIMS");
        contentCards.add(buildActivityPanel(), "ACTIVITY");

        mainArea.add(topHeader, BorderLayout.NORTH);
        mainArea.add(contentCards, BorderLayout.CENTER);

        rootPane.add(sidebar, BorderLayout.WEST);
        rootPane.add(mainArea, BorderLayout.CENTER);

        setContentPane(rootPane);
    }

    private void setActiveNav(JButton activeBtn) {
        JButton[] buttons = {btnNavDashboard, btnNavStudents, btnNavLostItems, btnNavFoundItems, btnNavClaims, btnNavActivity};
        for (JButton b : buttons) {
            boolean isActive = (b == activeBtn);
            b.setBackground(isActive ? UITheme.COLOR_ACCENT : UITheme.COLOR_PRIMARY_DARK);
            b.setForeground(isActive ? Color.BLACK : Color.WHITE);
        }
    }

    private void showCard(String name) {
        cardLayout.show(contentCards, name);
    }

    // =========================================================================
    // MODULE 1: DASHBOARD OVERVIEW
    // =========================================================================
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // KPI Grid (6 dynamic cards)
        JPanel kpiGrid = new JPanel(new GridLayout(2, 3, 14, 12));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));

        totalStudentsLbl = new JLabel("0");
        totalLostLbl = new JLabel("0");
        totalFoundLbl = new JLabel("0");
        pendingClaimsLbl = new JLabel("0");
        approvedClaimsLbl = new JLabel("0");
        rejectedClaimsLbl = new JLabel("0");

        kpiGrid.add(buildKpiCard("Total Registered Students", totalStudentsLbl, UITheme.COLOR_PRIMARY));
        kpiGrid.add(buildKpiCard("Total Lost Items", totalLostLbl, UITheme.COLOR_DANGER));
        kpiGrid.add(buildKpiCard("Total Found Items", totalFoundLbl, UITheme.COLOR_ACCENT));
        kpiGrid.add(buildKpiCard("Pending Claims", pendingClaimsLbl, UITheme.COLOR_WARNING));
        kpiGrid.add(buildKpiCard("Approved Claims", approvedClaimsLbl, UITheme.COLOR_SUCCESS));
        kpiGrid.add(buildKpiCard("Rejected Claims", rejectedClaimsLbl, UITheme.COLOR_TEXT_MUTED));

        // Two Split Overview Tables
        JPanel splitTables = new JPanel(new GridLayout(1, 2, 18, 0));
        splitTables.setOpaque(false);

        // Recent Lost & Found Reports
        JPanel reportsCard = new JPanel(new BorderLayout(8, 8));
        reportsCard.setBackground(Color.WHITE);
        reportsCard.setBorder(new CompoundBorder(new LineBorder(UITheme.COLOR_BORDER, 1), new EmptyBorder(14, 15, 14, 15)));
        JLabel repTitle = new JLabel("Recent Lost & Found Reports");
        repTitle.setFont(UITheme.FONT_SUBHEADER);
        repTitle.setForeground(UITheme.COLOR_PRIMARY);

        recentLostModel = new DefaultTableModel(new String[]{"ID", "Item Name", "Reporter", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable repTable = new JTable(recentLostModel);
        UITheme.styleTable(repTable);
        reportsCard.add(repTitle, BorderLayout.NORTH);
        reportsCard.add(new JScrollPane(repTable), BorderLayout.CENTER);

        // Recent Claim Submissions
        JPanel claimsCard = new JPanel(new BorderLayout(8, 8));
        claimsCard.setBackground(Color.WHITE);
        claimsCard.setBorder(new CompoundBorder(new LineBorder(UITheme.COLOR_BORDER, 1), new EmptyBorder(14, 15, 14, 15)));
        JLabel clmTitle = new JLabel("Recent Claim Submissions");
        clmTitle.setFont(UITheme.FONT_SUBHEADER);
        clmTitle.setForeground(UITheme.COLOR_PRIMARY);

        recentClaimsModel = new DefaultTableModel(new String[]{"ID", "Target Item", "Claimant", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable clmTable = new JTable(recentClaimsModel);
        UITheme.styleTable(clmTable);
        claimsCard.add(clmTitle, BorderLayout.NORTH);
        claimsCard.add(new JScrollPane(clmTable), BorderLayout.CENTER);

        splitTables.add(reportsCard);
        splitTables.add(claimsCard);

        panel.add(kpiGrid);
        panel.add(Box.createVerticalStrut(18));
        panel.add(splitTables);
        return panel;
    }

    private JPanel buildKpiCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)
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

    private void loadOverviewData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private Map<String, Integer> stats;
            private List<LostItem> recentLost;
            private List<Claim> recentClaims;

            @Override
            protected Void doInBackground() throws Exception {
                stats = adminService.getDashboardStatistics();
                recentLost = adminService.getRecentLostItems(8);
                recentClaims = adminService.getRecentClaims(8);
                return null;
            }

            @Override
            protected void done() {
                if (stats != null) {
                    totalStudentsLbl.setText(String.valueOf(stats.getOrDefault("totalStudents", 0)));
                    totalLostLbl.setText(String.valueOf(stats.getOrDefault("totalLostItems", 0)));
                    totalFoundLbl.setText(String.valueOf(stats.getOrDefault("totalFoundItems", 0)));
                    pendingClaimsLbl.setText(String.valueOf(stats.getOrDefault("pendingClaims", 0)));
                    approvedClaimsLbl.setText(String.valueOf(stats.getOrDefault("approvedClaims", 0)));
                    rejectedClaimsLbl.setText(String.valueOf(stats.getOrDefault("rejectedClaims", 0)));
                }

                recentLostModel.setRowCount(0);
                if (recentLost != null) {
                    for (LostItem l : recentLost) {
                        recentLostModel.addRow(new Object[]{
                                l.getLostItemId(),
                                l.getItemName(),
                                l.getReporterName(),
                                l.getStatus()
                        });
                    }
                }

                recentClaimsModel.setRowCount(0);
                if (recentClaims != null) {
                    for (Claim c : recentClaims) {
                        recentClaimsModel.addRow(new Object[]{
                                c.getClaimId(),
                                c.getTargetItemName(),
                                c.getClaimantName(),
                                c.getClaimStatus()
                        });
                    }
                }
            }
        };
        worker.execute();
    }

    // =========================================================================
    // MODULE 2: STUDENT MANAGEMENT MODULE
    // =========================================================================
    private JPanel buildStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Registered Student Management");
        title.setFont(UITheme.FONT_SUBHEADER);
        title.setForeground(UITheme.COLOR_PRIMARY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        studentSearchField = UITheme.createTextField();
        studentSearchField.setPreferredSize(new Dimension(180, 36));
        studentSearchField.setToolTipText("Search by Name, Register No, or Email");

        studentStatusFilterCombo = new JComboBox<>(new String[]{"ALL", "ACTIVE", "SUSPENDED", "INACTIVE"});
        studentStatusFilterCombo.setFont(UITheme.FONT_REGULAR);
        studentStatusFilterCombo.setBackground(Color.WHITE);
        studentStatusFilterCombo.addActionListener(e -> loadStudentsData());

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> loadStudentsData());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> {
            studentSearchField.setText("");
            studentStatusFilterCombo.setSelectedItem("ALL");
            loadStudentsData();
        });

        controls.add(new JLabel("Status:"));
        controls.add(studentStatusFilterCombo);
        controls.add(new JLabel("Search:"));
        controls.add(studentSearchField);
        controls.add(btnSearch);
        controls.add(btnRefresh);

        header.add(title, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        String[] cols = {"User ID", "Student Name", "Register Number", "Email", "Account Status", "Registration Date"};
        studentsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        studentsTable = new JTable(studentsModel);
        UITheme.styleTable(studentsTable);

        // Actions panel
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnViewStudent = UITheme.createSecondaryButton("View Student Details");
        JButton btnActivate = UITheme.createAccentButton("Activate Account");
        JButton btnDeactivate = UITheme.createDangerButton("Deactivate / Suspend");

        btnViewStudent.addActionListener(e -> viewSelectedStudentDetails());
        btnActivate.addActionListener(e -> changeStudentStatus("ACTIVE"));
        btnDeactivate.addActionListener(e -> changeStudentStatus("SUSPENDED"));

        actions.add(btnViewStudent);
        actions.add(btnActivate);
        actions.add(btnDeactivate);

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void loadStudentsData() {
        String keyword = studentSearchField.getText().trim();
        String filter = (String) studentStatusFilterCombo.getSelectedItem();

        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return adminService.searchStudents(keyword, filter);
            }

            @Override
            protected void done() {
                try {
                    currentStudentsList = get();
                    studentsModel.setRowCount(0);
                    if (currentStudentsList != null) {
                        for (User u : currentStudentsList) {
                            studentsModel.addRow(new Object[]{
                                    u.getUserId(),
                                    u.getName(),
                                    u.getRegisterNumber(),
                                    u.getEmail(),
                                    u.getAccountStatus(),
                                    u.getCreatedAt()
                            });
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Error loading students: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void viewSelectedStudentDetails() {
        int row = studentsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) studentsModel.getValueAt(row, 0);
        User student = currentStudentsList.stream().filter(u -> u.getUserId() == userId).findFirst().orElse(null);
        if (student == null) return;

        String msg = String.format(
                "Student Information:\n\n" +
                "User ID: %d\n" +
                "Full Name: %s\n" +
                "Register Number: %s\n" +
                "Email: %s\n" +
                "Account Role: %s\n" +
                "Account Status: %s\n" +
                "Registered Since: %s\n",
                student.getUserId(), student.getName(), student.getRegisterNumber(),
                student.getEmail(), student.getRole(), student.getAccountStatus(), student.getCreatedAt()
        );

        JOptionPane.showMessageDialog(this, msg, "Student Profile - " + student.getName(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void changeStudentStatus(String newStatus) {
        int row = studentsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) studentsModel.getValueAt(row, 0);
        String name = (String) studentsModel.getValueAt(row, 1);

        // Security check: cannot change own status
        if (userId == adminUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "Administrators cannot change their own account status.", "Action Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String verb = "ACTIVE".equalsIgnoreCase(newStatus) ? "activate" : "suspend / deactivate";
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to " + verb + " the student account for " + name + " (ID #" + userId + ")?",
                "Confirm Account Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = adminService.updateStudentStatus(adminUser.getUserId(), userId, newStatus);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Student account successfully set to " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                    loadStudentsData();
                    loadOverviewData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // MODULE 3: LOST ITEM MANAGEMENT MODULE
    // =========================================================================
    private JPanel buildLostItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Lost Item Reports Moderation");
        title.setFont(UITheme.FONT_SUBHEADER);
        title.setForeground(UITheme.COLOR_PRIMARY);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterBar.setOpaque(false);

        lostSearchField = UITheme.createTextField();
        lostSearchField.setPreferredSize(new Dimension(140, 36));
        lostSearchField.setToolTipText("Search Item Name");

        lostCategoryCombo = new JComboBox<>(new String[]{
                "All Categories", "ID Card", "Wallet", "Mobile Phone", "Keys",
                "Books", "Electronics", "Bags", "Accessories", "Other"
        });
        lostCategoryCombo.setFont(UITheme.FONT_REGULAR);
        lostCategoryCombo.setBackground(Color.WHITE);

        lostLocationField = UITheme.createTextField();
        lostLocationField.setPreferredSize(new Dimension(120, 36));
        lostLocationField.setToolTipText("Filter Location");

        lostDateField = UITheme.createTextField();
        lostDateField.setPreferredSize(new Dimension(100, 36));
        lostDateField.setToolTipText("YYYY-MM-DD");

        JButton btnFilter = UITheme.createPrimaryButton("Filter");
        btnFilter.addActionListener(e -> loadLostItems());

        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            lostSearchField.setText("");
            lostCategoryCombo.setSelectedIndex(0);
            lostLocationField.setText("");
            lostDateField.setText("");
            loadLostItems();
        });

        filterBar.add(new JLabel("Search:"));
        filterBar.add(lostSearchField);
        filterBar.add(lostCategoryCombo);
        filterBar.add(new JLabel("Loc:"));
        filterBar.add(lostLocationField);
        filterBar.add(new JLabel("Date:"));
        filterBar.add(lostDateField);
        filterBar.add(btnFilter);
        filterBar.add(btnReset);

        header.add(title, BorderLayout.WEST);
        header.add(filterBar, BorderLayout.EAST);

        String[] cols = {"Lost Item ID", "Student Name", "Item Name", "Category", "Description", "Lost Location", "Lost Date", "Status"};
        lostModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        lostTable = new JTable(lostModel);
        UITheme.styleTable(lostTable);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnDetails = UITheme.createSecondaryButton("View Item Details");
        JButton btnMarkOpen = UITheme.createSecondaryButton("Set Status OPEN");
        JButton btnMarkClaimed = UITheme.createSecondaryButton("Set Status CLAIMED");
        JButton btnMarkClosed = UITheme.createSecondaryButton("Set Status CLOSED");
        JButton btnDelete = UITheme.createDangerButton("Remove Record");

        btnDetails.addActionListener(e -> viewLostItemDetails());
        btnMarkOpen.addActionListener(e -> updateLostStatus("OPEN"));
        btnMarkClaimed.addActionListener(e -> updateLostStatus("CLAIMED"));
        btnMarkClosed.addActionListener(e -> updateLostStatus("CLOSED"));
        btnDelete.addActionListener(e -> deleteSelectedLostItem());

        actions.add(btnDetails);
        actions.add(btnMarkOpen);
        actions.add(btnMarkClaimed);
        actions.add(btnMarkClosed);
        actions.add(btnDelete);

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(lostTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void loadLostItems() {
        String keyword = lostSearchField.getText().trim();
        String cat = (String) lostCategoryCombo.getSelectedItem();
        String loc = lostLocationField.getText().trim();
        String dateStr = lostDateField.getText().trim();
        Date date = null;
        if (!dateStr.isEmpty()) {
            try { date = Date.valueOf(dateStr); } catch (Exception ignored) {}
        }

        final Date fDate = date;
        SwingWorker<List<LostItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<LostItem> doInBackground() throws Exception {
                return adminService.searchLostItems(keyword, cat, loc, fDate);
            }

            @Override
            protected void done() {
                try {
                    currentLostList = get();
                    lostModel.setRowCount(0);
                    if (currentLostList != null) {
                        for (LostItem item : currentLostList) {
                            lostModel.addRow(new Object[]{
                                    item.getLostItemId(),
                                    item.getReporterName() + " (" + item.getReporterRegisterNumber() + ")",
                                    item.getItemName(),
                                    item.getCategory(),
                                    item.getDescription(),
                                    item.getLostLocation(),
                                    item.getLostDate(),
                                    item.getStatus()
                            });
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Error loading lost items: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void viewLostItemDetails() {
        int row = lostTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int itemId = (int) lostModel.getValueAt(row, 0);
        LostItem item = currentLostList.stream().filter(l -> l.getLostItemId() == itemId).findFirst().orElse(null);
        if (item == null) return;

        String msg = String.format(
                "Lost Item Details:\n\n" +
                "Item ID: %d\n" +
                "Item Name: %s\n" +
                "Category: %s\n" +
                "Description: %s\n" +
                "Reported Lost Location: %s\n" +
                "Lost Date: %s\n" +
                "Current Status: %s\n\n" +
                "Reporting Student: %s (%s)\n" +
                "Student Email: %s\n",
                item.getLostItemId(), item.getItemName(), item.getCategory(),
                item.getDescription(), item.getLostLocation(), item.getLostDate(),
                item.getStatus(), item.getReporterName(), item.getReporterRegisterNumber(),
                item.getReporterEmail()
        );

        JOptionPane.showMessageDialog(this, msg, "Lost Item Record #" + itemId, JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateLostStatus(String newStatus) {
        int row = lostTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int itemId = (int) lostModel.getValueAt(row, 0);
        try {
            boolean ok = adminService.updateLostItemStatus(adminUser.getUserId(), itemId, newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Lost item status updated to " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadLostItems();
                loadOverviewData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedLostItem() {
        int row = lostTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int itemId = (int) lostModel.getValueAt(row, 0);
        String itemName = (String) lostModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Permanently delete Lost Item #" + itemId + " (" + itemName + ")?\nThis action cannot be undone.",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = adminService.deleteLostItem(adminUser.getUserId(), itemId, itemName);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Item record removed successfully.", "Removed", JOptionPane.INFORMATION_MESSAGE);
                    loadLostItems();
                    loadOverviewData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // MODULE 4: FOUND ITEM MANAGEMENT MODULE
    // =========================================================================
    private JPanel buildFoundItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Found Item Reports Moderation");
        title.setFont(UITheme.FONT_SUBHEADER);
        title.setForeground(UITheme.COLOR_PRIMARY);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterBar.setOpaque(false);

        foundSearchField = UITheme.createTextField();
        foundSearchField.setPreferredSize(new Dimension(140, 36));
        foundSearchField.setToolTipText("Search Item Name");

        foundCategoryCombo = new JComboBox<>(new String[]{
                "All Categories", "ID Card", "Wallet", "Mobile Phone", "Keys",
                "Books", "Electronics", "Bags", "Accessories", "Other"
        });
        foundCategoryCombo.setFont(UITheme.FONT_REGULAR);
        foundCategoryCombo.setBackground(Color.WHITE);

        foundLocationField = UITheme.createTextField();
        foundLocationField.setPreferredSize(new Dimension(120, 36));
        foundLocationField.setToolTipText("Filter Location");

        foundDateField = UITheme.createTextField();
        foundDateField.setPreferredSize(new Dimension(100, 36));
        foundDateField.setToolTipText("YYYY-MM-DD");

        JButton btnFilter = UITheme.createPrimaryButton("Filter");
        btnFilter.addActionListener(e -> loadFoundItems());

        JButton btnReset = UITheme.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            foundSearchField.setText("");
            foundCategoryCombo.setSelectedIndex(0);
            foundLocationField.setText("");
            foundDateField.setText("");
            loadFoundItems();
        });

        filterBar.add(new JLabel("Search:"));
        filterBar.add(foundSearchField);
        filterBar.add(foundCategoryCombo);
        filterBar.add(new JLabel("Loc:"));
        filterBar.add(foundLocationField);
        filterBar.add(new JLabel("Date:"));
        filterBar.add(foundDateField);
        filterBar.add(btnFilter);
        filterBar.add(btnReset);

        header.add(title, BorderLayout.WEST);
        header.add(filterBar, BorderLayout.EAST);

        String[] cols = {"Found Item ID", "Student Name", "Item Name", "Category", "Description", "Found Location", "Found Date", "Status"};
        foundModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        foundTable = new JTable(foundModel);
        UITheme.styleTable(foundTable);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnDetails = UITheme.createSecondaryButton("View Item Details");
        JButton btnMarkAvailable = UITheme.createSecondaryButton("Set Status AVAILABLE");
        JButton btnMarkClaimed = UITheme.createSecondaryButton("Set Status CLAIMED");
        JButton btnMarkClosed = UITheme.createSecondaryButton("Set Status CLOSED");
        JButton btnDelete = UITheme.createDangerButton("Remove Record");

        btnDetails.addActionListener(e -> viewFoundItemDetails());
        btnMarkAvailable.addActionListener(e -> updateFoundStatus("AVAILABLE"));
        btnMarkClaimed.addActionListener(e -> updateFoundStatus("CLAIMED"));
        btnMarkClosed.addActionListener(e -> updateFoundStatus("CLOSED"));
        btnDelete.addActionListener(e -> deleteSelectedFoundItem());

        actions.add(btnDetails);
        actions.add(btnMarkAvailable);
        actions.add(btnMarkClaimed);
        actions.add(btnMarkClosed);
        actions.add(btnDelete);

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(foundTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void loadFoundItems() {
        String keyword = foundSearchField.getText().trim();
        String cat = (String) foundCategoryCombo.getSelectedItem();
        String loc = foundLocationField.getText().trim();
        String dateStr = foundDateField.getText().trim();
        Date date = null;
        if (!dateStr.isEmpty()) {
            try { date = Date.valueOf(dateStr); } catch (Exception ignored) {}
        }

        final Date fDate = date;
        SwingWorker<List<FoundItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<FoundItem> doInBackground() throws Exception {
                return adminService.searchFoundItems(keyword, cat, loc, fDate);
            }

            @Override
            protected void done() {
                try {
                    currentFoundList = get();
                    foundModel.setRowCount(0);
                    if (currentFoundList != null) {
                        for (FoundItem item : currentFoundList) {
                            foundModel.addRow(new Object[]{
                                    item.getFoundItemId(),
                                    item.getReporterName() + " (" + item.getReporterRegisterNumber() + ")",
                                    item.getItemName(),
                                    item.getCategory(),
                                    item.getDescription(),
                                    item.getFoundLocation(),
                                    item.getFoundDate(),
                                    item.getStatus()
                            });
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Error loading found items: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void viewFoundItemDetails() {
        int row = foundTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int itemId = (int) foundModel.getValueAt(row, 0);
        FoundItem item = currentFoundList.stream().filter(f -> f.getFoundItemId() == itemId).findFirst().orElse(null);
        if (item == null) return;

        String msg = String.format(
                "Found Item Details:\n\n" +
                "Item ID: %d\n" +
                "Item Name: %s\n" +
                "Category: %s\n" +
                "Description: %s\n" +
                "Found Location: %s\n" +
                "Found Date: %s\n" +
                "Current Status: %s\n\n" +
                "Found & Reported By: %s (%s)\n" +
                "Student Email: %s\n",
                item.getFoundItemId(), item.getItemName(), item.getCategory(),
                item.getDescription(), item.getFoundLocation(), item.getFoundDate(),
                item.getStatus(), item.getReporterName(), item.getReporterRegisterNumber(),
                item.getReporterEmail()
        );

        JOptionPane.showMessageDialog(this, msg, "Found Item Record #" + itemId, JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateFoundStatus(String newStatus) {
        int row = foundTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int itemId = (int) foundModel.getValueAt(row, 0);
        try {
            boolean ok = adminService.updateFoundItemStatus(adminUser.getUserId(), itemId, newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Found item status updated to " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadFoundItems();
                loadOverviewData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedFoundItem() {
        int row = foundTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int itemId = (int) foundModel.getValueAt(row, 0);
        String itemName = (String) foundModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Permanently delete Found Item #" + itemId + " (" + itemName + ")?\nThis action cannot be undone.",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = adminService.deleteFoundItem(adminUser.getUserId(), itemId, itemName);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Item record removed successfully.", "Removed", JOptionPane.INFORMATION_MESSAGE);
                    loadFoundItems();
                    loadOverviewData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // MODULE 5: CLAIM MANAGEMENT MODULE (HIGHEST PRIORITY)
    // =========================================================================
    private JPanel buildClaimsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        // Header with Filters and Search
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Claim Requests & Ownership Verification");
        title.setFont(UITheme.FONT_SUBHEADER);
        title.setForeground(UITheme.COLOR_PRIMARY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        claimStatusCombo = new JComboBox<>(new String[]{"ALL", "PENDING", "APPROVED", "REJECTED"});
        claimStatusCombo.setFont(UITheme.FONT_REGULAR);
        claimStatusCombo.setBackground(Color.WHITE);
        claimStatusCombo.addActionListener(e -> loadClaimsData());

        claimSearchField = UITheme.createTextField();
        claimSearchField.setPreferredSize(new Dimension(170, 36));
        claimSearchField.setToolTipText("Search Claim ID, Student, or Item");

        JButton btnSearch = UITheme.createPrimaryButton("Search");
        btnSearch.addActionListener(e -> loadClaimsData());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> {
            claimSearchField.setText("");
            claimStatusCombo.setSelectedItem("ALL");
            loadClaimsData();
        });

        controls.add(new JLabel("Status:"));
        controls.add(claimStatusCombo);
        controls.add(new JLabel("Search:"));
        controls.add(claimSearchField);
        controls.add(btnSearch);
        controls.add(btnRefresh);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);

        // Center Split: Left Table, Right Verification Details Card
        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        centerSplit.setOpaque(false);

        String[] cols = {
                "Claim ID", "Student Name", "Register Number",
                "Related Item Name", "Item Type", "Claim Description",
                "Claim Date", "Claim Status", "Admin Remarks"
        };
        claimsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        claimsTable = new JTable(claimsModel);
        UITheme.styleTable(claimsTable);

        claimsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateSelectedClaimDetails();
            }
        });

        // Right Verification Card
        JPanel reviewCard = new JPanel();
        reviewCard.setBackground(Color.WHITE);
        reviewCard.setLayout(new BoxLayout(reviewCard, BoxLayout.Y_AXIS));
        reviewCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel reviewHeader = new JLabel("Claim Verification & Decision");
        reviewHeader.setFont(UITheme.FONT_SUBHEADER);
        reviewHeader.setForeground(UITheme.COLOR_PRIMARY);

        claimTargetItemLbl = new JLabel("-");
        claimStudentNameLbl = new JLabel("-");
        claimRegNoLbl = new JLabel("-");
        claimEmailLbl = new JLabel("-");

        claimProofArea = UITheme.createTextArea(3, 20);
        claimProofArea.setEditable(false);
        claimProofArea.setBackground(new Color(248, 250, 252));
        JScrollPane proofScroll = new JScrollPane(claimProofArea);
        proofScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        claimAdminRemarksArea = UITheme.createTextArea(3, 20);
        JScrollPane remarksScroll = new JScrollPane(claimAdminRemarksArea);
        remarksScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionButtons.setOpaque(false);

        JButton btnViewDetails = UITheme.createSecondaryButton("View Full Details");
        btnSaveRemarks = UITheme.createSecondaryButton("Save Remarks");
        btnRejectClaim = UITheme.createDangerButton("Reject Claim");
        btnApproveClaim = UITheme.createAccentButton("Approve Claim");

        btnApproveClaim.setEnabled(false);
        btnRejectClaim.setEnabled(false);
        btnSaveRemarks.setEnabled(false);

        btnViewDetails.addActionListener(e -> viewSelectedClaimFullDialog());
        btnSaveRemarks.addActionListener(e -> saveAdminRemarksOnly());
        btnApproveClaim.addActionListener(e -> executeClaimApproval());
        btnRejectClaim.addActionListener(e -> executeClaimRejection());

        actionButtons.add(btnViewDetails);
        actionButtons.add(btnSaveRemarks);
        actionButtons.add(btnRejectClaim);
        actionButtons.add(btnApproveClaim);

        reviewCard.add(reviewHeader);
        reviewCard.add(Box.createVerticalStrut(10));
        addMetaRow(reviewCard, "Target Item:", claimTargetItemLbl);
        addMetaRow(reviewCard, "Claimant Name:", claimStudentNameLbl);
        addMetaRow(reviewCard, "Register Number:", claimRegNoLbl);
        addMetaRow(reviewCard, "Campus Email:", claimEmailLbl);

        JLabel proofLbl = new JLabel("Ownership Proof / Explanation:");
        proofLbl.setFont(UITheme.FONT_BOLD);
        proofLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        reviewCard.add(proofLbl);
        reviewCard.add(Box.createVerticalStrut(3));
        reviewCard.add(proofScroll);
        reviewCard.add(Box.createVerticalStrut(8));

        JLabel remLbl = new JLabel("Administrator Remarks / Reason:");
        remLbl.setFont(UITheme.FONT_BOLD);
        remLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        reviewCard.add(remLbl);
        reviewCard.add(Box.createVerticalStrut(3));
        reviewCard.add(remarksScroll);
        reviewCard.add(Box.createVerticalStrut(12));
        reviewCard.add(actionButtons);

        centerSplit.add(new JScrollPane(claimsTable));
        centerSplit.add(reviewCard);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(centerSplit, BorderLayout.CENTER);
        return panel;
    }

    private void addMetaRow(JPanel panel, String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(5, 5));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.COLOR_TEXT_MUTED);
        l.setPreferredSize(new Dimension(130, 20));

        value.setFont(UITheme.FONT_REGULAR);
        value.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        row.add(l, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        panel.add(row);
        panel.add(Box.createVerticalStrut(3));
    }

    private void loadClaimsData() {
        String filter = (String) claimStatusCombo.getSelectedItem();
        String query = claimSearchField.getText().trim();

        SwingWorker<List<Claim>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Claim> doInBackground() throws Exception {
                return adminService.searchClaims(filter, query);
            }

            @Override
            protected void done() {
                try {
                    currentClaimsList = get();
                    claimsModel.setRowCount(0);
                    clearClaimDetailsPanel();

                    if (currentClaimsList != null) {
                        for (Claim c : currentClaimsList) {
                            claimsModel.addRow(new Object[]{
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
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Error loading claims: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void populateSelectedClaimDetails() {
        int row = claimsTable.getSelectedRow();
        if (row < 0) {
            clearClaimDetailsPanel();
            return;
        }

        int claimId = (int) claimsModel.getValueAt(row, 0);
        Claim claim = currentClaimsList.stream().filter(c -> c.getClaimId() == claimId).findFirst().orElse(null);

        if (claim != null) {
            claimTargetItemLbl.setText(claim.getTargetItemName() + " [" + claim.getTargetItemType() + "]");
            claimStudentNameLbl.setText(claim.getClaimantName());
            claimRegNoLbl.setText(claim.getClaimantRegisterNumber());
            claimEmailLbl.setText(claim.getClaimantEmail());
            claimProofArea.setText(claim.getClaimDescription());
            claimAdminRemarksArea.setText(claim.getAdminRemarks() != null ? claim.getAdminRemarks() : "");

            boolean isPending = "PENDING".equalsIgnoreCase(claim.getClaimStatus());
            btnApproveClaim.setEnabled(isPending);
            btnRejectClaim.setEnabled(isPending);
            btnSaveRemarks.setEnabled(true);
        }
    }

    private void clearClaimDetailsPanel() {
        claimTargetItemLbl.setText("-");
        claimStudentNameLbl.setText("-");
        claimRegNoLbl.setText("-");
        claimEmailLbl.setText("-");
        claimProofArea.setText("");
        claimAdminRemarksArea.setText("");
        btnApproveClaim.setEnabled(false);
        btnRejectClaim.setEnabled(false);
        btnSaveRemarks.setEnabled(false);
    }

    private void viewSelectedClaimFullDialog() {
        int row = claimsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a claim from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int claimId = (int) claimsModel.getValueAt(row, 0);
        Claim c = currentClaimsList.stream().filter(cl -> cl.getClaimId() == claimId).findFirst().orElse(null);
        if (c == null) return;

        String msg = String.format(
                "Claim Verification Details:\n\n" +
                "Claim ID: #%d\n" +
                "Submission Date: %s\n" +
                "Current Status: %s\n\n" +
                "Target Item: %s (%s Item)\n" +
                "Reported Item Location: %s\n" +
                "Current Item Status: %s\n\n" +
                "Claimant Student: %s (Reg: %s)\n" +
                "Student Email: %s\n\n" +
                "Proof of Ownership:\n\"%s\"\n\n" +
                "Admin Remarks:\n\"%s\"\n",
                c.getClaimId(), c.getClaimDate(), c.getClaimStatus(),
                c.getTargetItemName(), c.getTargetItemType(),
                c.getTargetItemLocation() != null ? c.getTargetItemLocation() : "-",
                c.getTargetItemStatus() != null ? c.getTargetItemStatus() : "-",
                c.getClaimantName(), c.getClaimantRegisterNumber(), c.getClaimantEmail(),
                c.getClaimDescription(),
                c.getAdminRemarks() != null ? c.getAdminRemarks() : "[None]"
        );

        JOptionPane.showMessageDialog(this, msg, "Full Claim Details - #" + claimId, JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveAdminRemarksOnly() {
        int row = claimsTable.getSelectedRow();
        if (row < 0) return;

        int claimId = (int) claimsModel.getValueAt(row, 0);
        String remarks = claimAdminRemarksArea.getText().trim();

        if (remarks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter remarks before saving.", "Remarks Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean ok = adminService.addAdminRemarks(adminUser.getUserId(), claimId, remarks);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Administrator remarks saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
                loadClaimsData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving remarks: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeClaimApproval() {
        int row = claimsTable.getSelectedRow();
        if (row < 0) return;

        int claimId = (int) claimsModel.getValueAt(row, 0);
        String remarks = claimAdminRemarksArea.getText().trim();

        if (remarks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter administrator verification remarks before approving.", "Remarks Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Approve Claim #" + claimId + "?\n\n" +
                "This action will:\n" +
                "1. Update this claim status to APPROVED.\n" +
                "2. Mark the related item as CLAIMED in the database.\n" +
                "3. Automatically close and reject any conflicting pending claims for this item.\n" +
                "4. Record this action in the administration activity audit log.",
                "Confirm Atomic Claim Approval",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = adminService.approveClaim(adminUser.getUserId(), claimId, remarks);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Claim approved successfully and item marked as CLAIMED.", "Approval Complete", JOptionPane.INFORMATION_MESSAGE);
                    loadClaimsData();
                    loadOverviewData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error approving claim: " + ex.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void executeClaimRejection() {
        int row = claimsTable.getSelectedRow();
        if (row < 0) return;

        int claimId = (int) claimsModel.getValueAt(row, 0);
        String remarks = claimAdminRemarksArea.getText().trim();

        if (remarks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide the reason for rejection in Administrator Remarks.", "Remarks Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reject Claim #" + claimId + "?\n" +
                "The student will be able to see the rejection and your explanation.",
                "Confirm Claim Rejection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = adminService.rejectClaim(adminUser.getUserId(), claimId, remarks);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Claim has been rejected.", "Rejection Recorded", JOptionPane.INFORMATION_MESSAGE);
                    loadClaimsData();
                    loadOverviewData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error rejecting claim: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // MODULE 6: ADMIN ACTIVITY HISTORY MODULE
    // =========================================================================
    private JPanel buildActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Administrator Activity & Audit History");
        title.setFont(UITheme.FONT_SUBHEADER);
        title.setForeground(UITheme.COLOR_PRIMARY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        activityFilterCombo = new JComboBox<>(new String[]{
                "ALL", "ADMIN_LOGIN", "CLAIM_APPROVED", "CLAIM_REJECTED",
                "ADMIN_REMARKS_ADDED", "ITEM_STATUS_UPDATED", "ITEM_DELETED",
                "STUDENT_ACTIVATED", "STUDENT_DEACTIVATED"
        });
        activityFilterCombo.setFont(UITheme.FONT_REGULAR);
        activityFilterCombo.setBackground(Color.WHITE);
        activityFilterCombo.addActionListener(e -> loadActivityData());

        activitySearchField = UITheme.createTextField();
        activitySearchField.setPreferredSize(new Dimension(180, 36));
        activitySearchField.setToolTipText("Search description or details");

        JButton btnSearch = UITheme.createPrimaryButton("Filter");
        btnSearch.addActionListener(e -> loadActivityData());

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> {
            activitySearchField.setText("");
            activityFilterCombo.setSelectedItem("ALL");
            loadActivityData();
        });

        controls.add(new JLabel("Action:"));
        controls.add(activityFilterCombo);
        controls.add(new JLabel("Keyword:"));
        controls.add(activitySearchField);
        controls.add(btnSearch);
        controls.add(btnRefresh);

        header.add(title, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        String[] cols = {"Activity ID", "Admin ID", "Admin Name", "Action Type", "Description", "Date and Time"};
        activityModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        activityTable = new JTable(activityModel);
        UITheme.styleTable(activityTable);

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(activityTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadActivityData() {
        String actionFilter = (String) activityFilterCombo.getSelectedItem();
        String keyword = activitySearchField.getText().trim();

        SwingWorker<List<ActivityLog>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ActivityLog> doInBackground() throws Exception {
                return adminService.searchActivityLog(actionFilter, keyword);
            }

            @Override
            protected void done() {
                try {
                    List<ActivityLog> logs = get();
                    activityModel.setRowCount(0);
                    if (logs != null) {
                        for (ActivityLog l : logs) {
                            activityModel.addRow(new Object[]{
                                    l.getLogId(),
                                    l.getAdminId(),
                                    l.getAdminName(),
                                    l.getActionType(),
                                    l.getTargetDescription() + (l.getDetails() != null ? " (" + l.getDetails() + ")" : ""),
                                    l.getPerformedAt()
                            });
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this, "Error loading activity history: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // =========================================================================
    // PORTAL SWITCHING & LOGOUT
    // =========================================================================
    private void performSwitchPortal() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Do you want to switch to the Student Portal?",
                "Switch Portal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            dispose();
            new StudentLoginFrame().setVisible(true);
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
                "Are you sure you want to log out of administrator mode?",
                "Confirm Sign Out",
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
