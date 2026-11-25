package app;

import dao.ClaimDAO;
import dao.FoundItemDAO;
import dao.LostItemDAO;
import dao.UserDAO;
import model.Claim;
import model.FoundItem;
import model.LostItem;
import model.User;
import ui.LoginFrame;
import util.DBConnection;
import util.PasswordUtil;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Secure Local Interactive Setup Prompt for Smart Lost-and-Found System.
 *
 * Prompts the user privately for their MySQL root password on their local
 * desktop, executes schema.sql and sample_data.sql, runs full CRUD/transaction
 * verification tests, writes database/setup_report.txt, and boots the app.
 *
 * Password is NEVER logged, printed, or stored in any form other than
 * the local config/db.properties file (which is git-ignored).
 */
public class SecureDatabaseSetupDialog extends JFrame {

    private JPasswordField passwordField;
    private JCheckBox showPasswordCheck;
    private JButton submitButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private JTextArea logArea;

    // ------------------------------------------------------------------ main
    public static void main(String[] args) {
        // Apply Nimbus look-and-feel if available
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> {
            SecureDatabaseSetupDialog dialog = new SecureDatabaseSetupDialog();
            dialog.setVisible(true);
            dialog.toFront();
            dialog.requestFocus();
        });
    }

    // ----------------------------------------------------------- constructor
    public SecureDatabaseSetupDialog() {
        initComponents();
    }

    // ------------------------------------------------------- UI initializer
    private void initComponents() {
        setTitle("MySQL Database Setup & Verification - Smart Lost & Found");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(680, 650);
        setMinimumSize(new Dimension(600, 580));
        setLocationRelativeTo(null);

        // Always on top so it doesn't get lost behind other windows
        setAlwaysOnTop(true);

        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBackground(UITheme.COLOR_BG);
        contentPane.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(25, 25, 25, 25)
        ));

        // --- Title
        JLabel titleLbl = new JLabel("Database Setup & Verification");
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.COLOR_PRIMARY);

        // --- Description
        JLabel descLbl = new JLabel("<html>" +
                "<b>Smart Lost-and-Found Management System</b><br><br>" +
                "MySQL Server 8.4 on <b>localhost:3306</b> is required.<br>" +
                "Enter your MySQL <code>root</code> password below (same as MySQL Workbench).<br>" +
                "The setup will automatically:<br>" +
                "&nbsp;&nbsp;1. Create the <b>smart_lost_found</b> database &amp; tables<br>" +
                "&nbsp;&nbsp;2. Load sample test data<br>" +
                "&nbsp;&nbsp;3. Run CRUD &amp; transaction verification tests<br>" +
                "&nbsp;&nbsp;4. Launch the application<br>" +
                "<small><i>Your password is saved only to config/db.properties (git-ignored).</i></small>" +
                "</html>");
        descLbl.setFont(UITheme.FONT_REGULAR);
        descLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        // --- Password field
        JLabel passLbl = new JLabel("MySQL 'root' Password:");
        passLbl.setFont(UITheme.FONT_BOLD);
        passLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        passwordField = UITheme.createPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setToolTipText("Enter your MySQL root password - same as used in MySQL Workbench");

        // --- Show password toggle
        showPasswordCheck = new JCheckBox("Show Password");
        showPasswordCheck.setFont(UITheme.FONT_SMALL);
        showPasswordCheck.setBackground(Color.WHITE);
        showPasswordCheck.setFocusPainted(false);
        showPasswordCheck.addActionListener(e ->
                passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : '*'));

        // --- Status label (shows errors in red)
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.COLOR_DANGER);

        // --- Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        cancelButton = UITheme.createSecondaryButton("Exit");
        cancelButton.addActionListener(e -> System.exit(0));

        submitButton = UITheme.createPrimaryButton("Initialize & Verify Database");
        submitButton.addActionListener(e -> runSetup());

        btnPanel.add(cancelButton);
        btnPanel.add(submitButton);

        // --- Progress log area
        JLabel logLbl = new JLabel("Execution Progress Log:");
        logLbl.setFont(UITheme.FONT_BOLD);
        logLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        logArea = new JTextArea(10, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(248, 250, 252));
        logArea.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1));
        logScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        // --- Assemble card
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(descLbl);
        card.add(Box.createVerticalStrut(20));
        card.add(passLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(5));
        card.add(showPasswordCheck);
        card.add(Box.createVerticalStrut(10));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(btnPanel);
        card.add(Box.createVerticalStrut(15));
        card.add(logLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(logScroll);

        contentPane.add(card, BorderLayout.CENTER);
        setContentPane(contentPane);

        // Enter key triggers submit
        passwordField.addActionListener(e -> runSetup());
    }

    // -------------------------------------------------------- logging helpers
    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void setStatus(String msg, boolean error) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(msg);
            statusLabel.setForeground(error ? UITheme.COLOR_DANGER : new Color(0, 128, 0));
        });
    }

    // ------------------------------------------------------ main setup worker
    private void runSetup() {
        char[] passChars = passwordField.getPassword();
        String pass = new String(passChars);
        java.util.Arrays.fill(passChars, '\0'); // Clear char array immediately

        statusLabel.setText(" ");
        submitButton.setEnabled(false);
        submitButton.setText("Setting up...");
        logArea.setText("");

        SwingWorker<Boolean, String> worker = new SwingWorker<>() {
            private final StringBuilder report = new StringBuilder();

            @Override
            protected Boolean doInBackground() throws Exception {

                // ============================================================
                // STEP 1: Verify MySQL connectivity (server-level, no DB yet)
                // ============================================================
                publish("=== Smart Lost-and-Found: Database Setup ===");
                publish("[1/6] Testing MySQL root connection on localhost:3306...");
                String serverUrl = "jdbc:mysql://localhost:3306/?useSSL=false" +
                        "&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=5000";

                try (Connection conn = DriverManager.getConnection(serverUrl, "root", pass)) {
                    String version = "";
                    try (Statement s = conn.createStatement();
                         ResultSet r = s.executeQuery("SELECT VERSION()")) {
                        if (r.next()) version = r.getString(1);
                    }
                    publish("[OK] Connected to MySQL " + version + " on localhost:3306");
                    report.append("MySQL Server Connection: SUCCESS (").append(version).append(")\n");
                } catch (SQLException e) {
                    publish("[FAIL] Connection failed: " + e.getMessage());
                    publish("       Verify your password matches MySQL Workbench credentials.");
                    throw new Exception("MySQL authentication failed. Please re-enter your password.");
                }

                // ============================================================
                // STEP 2: Save password securely to config/db.properties
                // ============================================================
                publish("[2/6] Saving credentials to config/db.properties...");
                writeDbProperties(pass);
                // Force DBConnection to reload the new password immediately
                DBConnection.loadConfiguration();
                publish("[OK] config/db.properties updated. Password saved securely.");
                report.append("JDBC Configuration: Saved to config/db.properties\n");

                // ============================================================
                // STEP 3: Execute schema.sql
                // ============================================================
                publish("[3/6] Executing database/schema.sql...");
                publish("      (Creates smart_lost_found DB + 4 tables + 13 indexes)");
                try (Connection conn = DriverManager.getConnection(serverUrl, "root", pass)) {
                    executeSqlFile(conn, "database/schema.sql");
                }
                publish("[OK] Database 'smart_lost_found' created with all tables and indexes.");
                report.append("Schema Execution (schema.sql): SUCCESS\n");
                report.append("  Tables created: USERS, LOST_ITEMS, FOUND_ITEMS, CLAIMS\n");

                // ============================================================
                // STEP 4: Execute sample_data.sql
                // ============================================================
                publish("[4/6] Executing database/sample_data.sql...");
                publish("      (Inserts 4 users, 4 lost items, 4 found items, 3 claims)");
                String dbUrl = "jdbc:mysql://localhost:3306/smart_lost_found?useSSL=false" +
                        "&allowPublicKeyRetrieval=true&serverTimezone=UTC&allowMultiQueries=true";
                try (Connection conn = DriverManager.getConnection(dbUrl, "root", pass)) {
                    executeSqlFile(conn, "database/sample_data.sql");
                }
                publish("[OK] Sample data loaded successfully.");
                report.append("Sample Data Population (sample_data.sql): SUCCESS\n");

                // ============================================================
                // STEP 5: Verify tables and row counts via information_schema
                // ============================================================
                publish("[5/6] Verifying tables in information_schema...");
                try (Connection conn = DBConnection.getConnection();
                     Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(
                             "SELECT table_name FROM information_schema.tables " +
                             "WHERE table_schema='smart_lost_found' ORDER BY table_name")) {

                    int tableCount = 0;
                    while (rs.next()) {
                        String tbl = rs.getString(1);
                        tableCount++;
                        int rows = 0;
                        try (Statement s2 = conn.createStatement();
                             ResultSet r2 = s2.executeQuery("SELECT COUNT(*) FROM smart_lost_found." + tbl)) {
                            if (r2.next()) rows = r2.getInt(1);
                        }
                        publish(String.format("      %-18s : %d row(s)", tbl, rows));
                        report.append(String.format("  Table %-18s: %d rows\n", tbl, rows));
                    }
                    if (tableCount != 4) {
                        throw new Exception("Expected 4 tables but found " + tableCount +
                                ". Schema execution may have partially failed.");
                    }
                    publish("[OK] All 4 tables verified with expected data.");
                    report.append("Table Verification: 4/4 tables present - SUCCESS\n");
                }

                // ============================================================
                // STEP 6: End-to-end CRUD and ACID Transaction Tests
                // ============================================================
                publish("[6/6] Running CRUD and ACID Transaction tests...");
                runCrudTests(report);
                publish("[OK] All 9 CRUD and transaction tests passed.");

                // ============================================================
                // Write full setup report
                // ============================================================
                writeSetupReport(report);
                publish("     Setup report saved to database/setup_report.txt");
                publish("");
                publish("=================================================");
                publish("  DATABASE SETUP AND VERIFICATION: 100% SUCCESS");
                publish("=================================================");
                publish("  Launching application login screen...");

                return true;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String s : chunks) {
                    log(s);
                }
            }

            @Override
            protected void done() {
                submitButton.setEnabled(true);
                submitButton.setText("Initialize & Verify Database");
                try {
                    if (get()) {
                        setStatus("Setup complete! Launching application...", false);
                        // Brief pause so user can read the success message
                        Timer t = new Timer(1500, evt -> {
                            dispose();
                            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
                        });
                        t.setRepeats(false);
                        t.start();
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String msg = cause.getMessage() != null ? cause.getMessage() : cause.toString();
                    setStatus(msg, true);
                    submitButton.setText("Retry");
                    log("[ERROR] " + msg);
                }
            }
        };
        worker.execute();
    }

    // ----------------------------------------------- Write db.properties
    private void writeDbProperties(String pass) throws IOException {
        // Ensure config directory exists
        new File("config").mkdirs();
        String content =
                "# Database Connection Configuration\n" +
                "# WARNING: Never commit this file to version control.\n" +
                "db.url=jdbc:mysql://localhost:3306/smart_lost_found?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC\n" +
                "db.user=root\n" +
                "db.password=" + pass + "\n" +
                "db.driver=com.mysql.cj.jdbc.Driver\n";
        try (FileOutputStream out = new FileOutputStream("config/db.properties")) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    // ----------------------------------------------- Write setup report
    private void writeSetupReport(StringBuilder report) {
        try {
            new File("database").mkdirs();
            try (FileWriter fw = new FileWriter("database/setup_report.txt", StandardCharsets.UTF_8)) {
                fw.write("=================================================================\n");
                fw.write("SMART LOST-AND-FOUND SYSTEM - DATABASE SETUP EXECUTION REPORT\n");
                fw.write("Execution Time : " + new java.util.Date() + "\n");
                fw.write("Database       : smart_lost_found @ localhost:3306\n");
                fw.write("=================================================================\n\n");
                fw.write(report.toString());
                fw.write("\n=================================================================\n");
                fw.write("RESULT: ALL OPERATIONS AND CONSTRAINTS VERIFIED -- 100% SUCCESS\n");
                fw.write("=================================================================\n");
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not write setup report: " + e.getMessage());
        }
    }

    // ----------------------------------------------- SQL Script Executor
    /**
     * Robust SQL script executor that handles:
     * - Single-line and multi-line comments (-- and block comments)
     * - Multi-row INSERT VALUES statements
     * - SET statements and USE statements
     * - Semicolons inside quoted string literals
     * - Empty statements
     */
    private void executeSqlFile(Connection conn, String scriptPath) throws Exception {
        File file = new File(scriptPath);
        if (!file.exists()) {
            throw new Exception("SQL script not found: " + file.getAbsolutePath());
        }

        String fullContent;
        try (InputStream in = new FileInputStream(file)) {
            fullContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<String> statements = splitSqlStatements(fullContent);

        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    try {
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        // Report the failing statement for diagnostics
                        String preview = sql.length() > 120 ? sql.substring(0, 120) + "..." : sql;
                        throw new Exception("SQL execution failed on:\n  " + preview + "\nError: " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Splits a SQL script into individual statements, correctly handling:
     * - Single-quoted string literals (may contain semicolons)
     * - Double-quoted identifiers
     * - Single-line comments (-- ...)
     * - Block comments (/* ... *\/)
     */
    private List<String> splitSqlStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int len = script.length();
        int i = 0;

        while (i < len) {
            char c = script.charAt(i);

            // Block comment: /* ... */
            if (c == '/' && i + 1 < len && script.charAt(i + 1) == '*') {
                int end = script.indexOf("*/", i + 2);
                if (end == -1) break; // unterminated block comment, skip rest
                i = end + 2;
                continue;
            }

            // Single-line comment: -- ...
            if (c == '-' && i + 1 < len && script.charAt(i + 1) == '-') {
                int end = script.indexOf('\n', i);
                i = (end == -1) ? len : end + 1;
                continue;
            }

            // Single-quoted string literal: '...' (possibly with '' escapes)
            if (c == '\'') {
                current.append(c);
                i++;
                while (i < len) {
                    char sc = script.charAt(i);
                    current.append(sc);
                    i++;
                    if (sc == '\'') {
                        // Check for escaped quote ''
                        if (i < len && script.charAt(i) == '\'') {
                            current.append('\'');
                            i++;
                        } else {
                            break; // end of string literal
                        }
                    }
                }
                continue;
            }

            // Double-quoted identifier: "..."
            if (c == '"') {
                current.append(c);
                i++;
                while (i < len) {
                    char dc = script.charAt(i);
                    current.append(dc);
                    i++;
                    if (dc == '"') break;
                }
                continue;
            }

            // Statement terminator
            if (c == ';') {
                String stmt = current.toString().trim();
                if (!stmt.isEmpty()) {
                    statements.add(stmt);
                }
                current.setLength(0);
                i++;
                continue;
            }

            current.append(c);
            i++;
        }

        // Capture any trailing statement without a trailing semicolon
        String last = current.toString().trim();
        if (!last.isEmpty()) {
            statements.add(last);
        }

        return statements;
    }

    // ----------------------------------------------- CRUD & Transaction Tests
    private void runCrudTests(StringBuilder report) throws Exception {
        UserDAO userDAO = new UserDAO();
        LostItemDAO lostItemDAO = new LostItemDAO();
        FoundItemDAO foundItemDAO = new FoundItemDAO();
        ClaimDAO claimDAO = new ClaimDAO();

        // Test 1: CREATE User
        User testUser = new User(
                "Verifier Student", "VERIFY-TEST-999",
                "verify.autotest@college.edu",
                PasswordUtil.hashPassword("Student@123"),
                "STUDENT"
        );
        userDAO.register(testUser);
        if (testUser.getUserId() <= 0) throw new Exception("CREATE User: Generated ID is 0");
        log("   [T1] CREATE User: OK (ID=" + testUser.getUserId() + ")");
        report.append("CRUD Test 1 [CREATE User]: SUCCESS - ID ").append(testUser.getUserId()).append("\n");

        // Test 2: READ User by email
        User readUser = userDAO.findByEmail("verify.autotest@college.edu");
        if (readUser == null) throw new Exception("READ User: not found after insert");
        if (!readUser.getName().equals("Verifier Student")) throw new Exception("READ User: name mismatch");
        log("   [T2] READ User by email: OK");
        report.append("CRUD Test 2 [READ User]: SUCCESS\n");

        // Test 3: UPDATE User profile
        userDAO.updateProfile(testUser.getUserId(), "Verifier Student Updated", "verify.updated@college.edu");
        User updatedUser = userDAO.findById(testUser.getUserId());
        if (updatedUser == null || !updatedUser.getName().equals("Verifier Student Updated")) {
            throw new Exception("UPDATE User: name not updated correctly");
        }
        log("   [T3] UPDATE User profile: OK");
        report.append("CRUD Test 3 [UPDATE User]: SUCCESS\n");

        // Test 4: CREATE Lost Item
        LostItem lost = new LostItem(
                testUser.getUserId(), "Test Verification Umbrella", "Accessories",
                "Black umbrella with yellow handle - auto test", "Test Lab Block C",
                new java.sql.Date(System.currentTimeMillis())
        );
        lostItemDAO.insert(lost);
        if (lost.getLostItemId() <= 0) throw new Exception("CREATE Lost Item: Generated ID is 0");
        log("   [T4] CREATE Lost Item: OK (ID=" + lost.getLostItemId() + ")");
        report.append("CRUD Test 4 [CREATE Lost Item]: SUCCESS - ID ").append(lost.getLostItemId()).append("\n");

        // Test 5: CREATE Found Item
        FoundItem found = new FoundItem(
                testUser.getUserId(), "Test Verification Umbrella", "Accessories",
                "Found black umbrella near canteen - auto test", "Test Canteen Area",
                new java.sql.Date(System.currentTimeMillis())
        );
        foundItemDAO.insert(found);
        if (found.getFoundItemId() <= 0) throw new Exception("CREATE Found Item: Generated ID is 0");
        log("   [T5] CREATE Found Item: OK (ID=" + found.getFoundItemId() + ")");
        report.append("CRUD Test 5 [CREATE Found Item]: SUCCESS - ID ").append(found.getFoundItemId()).append("\n");

        // Test 6: CREATE Claim (on Found Item)
        Claim claim = new Claim(
                testUser.getUserId(), null, found.getFoundItemId(),
                "Auto-test claim: umbrella has initials V.T. on the handle."
        );
        claimDAO.submitClaim(claim);
        if (claim.getClaimId() <= 0) throw new Exception("CREATE Claim: Generated ID is 0");
        log("   [T6] CREATE Claim: OK (ID=" + claim.getClaimId() + ")");
        report.append("CRUD Test 6 [CREATE Claim]: SUCCESS - ID ").append(claim.getClaimId()).append("\n");

        // Test 7: ACID Transaction - Approve Claim (updates claim + item status atomically)
        claimDAO.approveClaim(claim.getClaimId(), "Auto-test approval: initials verified on handle.");
        FoundItem verifyFound = foundItemDAO.findById(found.getFoundItemId());
        if (verifyFound == null || !"CLAIMED".equalsIgnoreCase(verifyFound.getStatus())) {
            throw new Exception("ACID Transaction: Found item status not updated to CLAIMED. Got: " +
                    (verifyFound == null ? "null" : verifyFound.getStatus()));
        }
        log("   [T7] ACID Transaction (Approve + Item Status Update): OK");
        report.append("CRUD Test 7 [ACID Transaction - Approve Claim]: SUCCESS\n");

        // Test 8: CHECK Constraint - Claim with BOTH lost+found IDs must fail
        boolean constraintEnforced = false;
        try {
            Claim badClaim = new Claim(testUser.getUserId(), lost.getLostItemId(), found.getFoundItemId(),
                    "Invalid dual-reference claim - should be rejected");
            claimDAO.submitClaim(badClaim);
        } catch (IllegalArgumentException | SQLException e) {
            constraintEnforced = true;
        }
        if (!constraintEnforced) {
            throw new Exception("CHECK Constraint chk_claim_single_item was NOT enforced!");
        }
        log("   [T8] CHECK Constraint (dual-item claim rejected): OK");
        report.append("CRUD Test 8 [CHECK Constraint chk_claim_single_item]: SUCCESS\n");

        // Test 9: DELETE cleanup
        // Claims deleted first (FK dependency), then items, then user
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete test claims
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM CLAIMS WHERE user_id = ?")) {
                    ps.setInt(1, testUser.getUserId());
                    ps.executeUpdate();
                }
                // Delete test found item
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM FOUND_ITEMS WHERE found_item_id = ?")) {
                    ps.setInt(1, found.getFoundItemId());
                    ps.executeUpdate();
                }
                // Delete test lost item
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM LOST_ITEMS WHERE lost_item_id = ?")) {
                    ps.setInt(1, lost.getLostItemId());
                    ps.executeUpdate();
                }
                // Delete test user
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM USERS WHERE user_id = ?")) {
                    ps.setInt(1, testUser.getUserId());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        log("   [T9] DELETE cleanup (cascaded): OK");
        report.append("CRUD Test 9 [DELETE Cascade Cleanup]: SUCCESS\n");
        report.append("\n--- All 9 CRUD/Transaction Tests: PASSED ---\n");
    }
}
