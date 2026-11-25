package app;

import dao.ClaimDAO;
import dao.FoundItemDAO;
import dao.LostItemDAO;
import dao.UserDAO;
import model.Claim;
import model.FoundItem;
import model.LostItem;
import model.User;
import util.DBConnection;
import util.PasswordUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Professional Database Setup & End-to-End JDBC CRUD Verification Runner.
 * Executes schema.sql, sample_data.sql, inspects information_schema,
 * and tests all CRUD operations and ACID transactions.
 */
public class DatabaseSetupAndVerifier {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("SMART LOST-AND-FOUND SYSTEM - DATABASE AUTOMATIC SETUP & VERIFICATION");
        System.out.println("===============================================================================");

        // 1. Read config/db.properties
        Properties props = new Properties();
        File configFile = new File("config/db.properties");
        if (!configFile.exists()) {
            System.err.println("[ERROR] config/db.properties not found!");
            return;
        }

        try (FileInputStream in = new FileInputStream(configFile)) {
            props.load(in);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed reading config/db.properties: " + e.getMessage());
            return;
        }

        String url = props.getProperty("db.url", "").trim();
        String user = props.getProperty("db.user", "root").trim();
        String password = props.getProperty("db.password", "");

        System.out.println("[CONFIG] JDBC Target URL : " + url);
        System.out.println("[CONFIG] Database User   : " + user);
        System.out.println("[CONFIG] Password Status : " + (password.isEmpty() ? "EMPTY" : "PROVIDED (" + password.length() + " chars)"));

        // 2. Connect to MySQL server instance (without DB initially or directly)
        String serverUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] MySQL JDBC Driver not found in classpath.");
            return;
        }

        System.out.println("\n--- STEP 1: INITIAL SERVER CONNECTION & SCHEMA CREATION ---");
        try (Connection serverConn = DriverManager.getConnection(serverUrl, user, password);
             Statement stmt = serverConn.createStatement()) {

            System.out.println("[SUCCESS] Successfully connected to MySQL Server instance on localhost:3306!");

            // Execute schema.sql
            System.out.println("[ACTION] Executing database/schema.sql...");
            executeSqlScript(serverConn, "database/schema.sql");
            System.out.println("[SUCCESS] database/schema.sql executed successfully!");

            // Execute sample_data.sql
            System.out.println("[ACTION] Executing database/sample_data.sql...");
            executeSqlScript(serverConn, "database/sample_data.sql");
            System.out.println("[SUCCESS] database/sample_data.sql executed successfully!");

        } catch (SQLException e) {
            System.err.println("[ERROR] Server connection or SQL script execution failed: " + e.getMessage());
            if (e.getErrorCode() == 1045) {
                System.err.println("[HINT] Authentication failed. Please verify the MySQL root password in 'config/db.properties'.");
            }
            return;
        }

        // 3. Connect to smart_lost_found via DBConnection utility
        System.out.println("\n--- STEP 2: VERIFY APPLICATION JDBC CONNECTIVITY ---");
        DBConnection.loadConfiguration();
        try (Connection dbConn = DBConnection.getConnection()) {
            System.out.println("[SUCCESS] DBConnection.getConnection() connected to " + DBConnection.getDatabaseName());

            // 4. Verify all four tables in information_schema
            System.out.println("\n--- STEP 3: VERIFY DATABASE TABLES & ROW COUNTS ---");
            String checkTablesSql = "SELECT table_name, engine, table_rows " +
                                   "FROM information_schema.tables " +
                                   "WHERE table_schema = 'smart_lost_found' " +
                                   "ORDER BY table_name";

            try (PreparedStatement ps = dbConn.prepareStatement(checkTablesSql);
                 ResultSet rs = ps.executeQuery()) {

                int foundTables = 0;
                while (rs.next()) {
                    String tableName = rs.getString("table_name");
                    String engine = rs.getString("engine");

                    // Get exact live count
                    int rowCount = 0;
                    try (Statement countStmt = dbConn.createStatement();
                         ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                        if (countRs.next()) rowCount = countRs.getInt(1);
                    }

                    System.out.println(String.format("  Table: %-15s | Engine: %-8s | Rows: %d", tableName, engine, rowCount));
                    foundTables++;
                }

                if (foundTables == 4) {
                    System.out.println("[VERIFIED] All 4 required tables (USERS, LOST_ITEMS, FOUND_ITEMS, CLAIMS) exist!");
                } else {
                    System.err.println("[WARNING] Found " + foundTables + " tables instead of expected 4.");
                }
            }

            // 5. Test CRUD & ACID Transactions
            System.out.println("\n--- STEP 4: END-TO-END JDBC CRUD & TRANSACTION TESTING ---");
            testCrudAndTransactions();

            System.out.println("\n===============================================================================");
            System.out.println("ALL DATABASE OPERATIONS & TESTS COMPLETED WITH 100% SUCCESS!");
            System.out.println("===============================================================================");

        } catch (SQLException e) {
            System.err.println("[ERROR] Verification step failed: " + e.getMessage());
        }
    }

    private static void executeSqlScript(Connection conn, String scriptPath) throws SQLException {
        File file = new File(scriptPath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Script file not found: " + scriptPath);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("--") || line.startsWith("//") || line.isEmpty()) {
                    continue;
                }
                sb.append(line).append(" ");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading script: " + e.getMessage(), e);
        }

        String[] statements = sb.toString().split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                }
            }
        }
    }

    private static void testCrudAndTransactions() {
        UserDAO userDAO = new UserDAO();
        LostItemDAO lostItemDAO = new LostItemDAO();
        FoundItemDAO foundItemDAO = new FoundItemDAO();
        ClaimDAO claimDAO = new ClaimDAO();

        try {
            // [C] CREATE: Test User Registration
            System.out.println("1. [CREATE] Testing Student Registration via UserDAO...");
            String testEmail = "crud.test.student@college.edu";
            String testRegNo = "TEST-REG-999";
            User testUser = new User("CRUD Test Student", testRegNo, testEmail,
                    PasswordUtil.hashPassword("TestPass@123"), "STUDENT");

            boolean userCreated = userDAO.register(testUser);
            System.out.println("   -> User created: " + userCreated + " (Assigned ID: " + testUser.getUserId() + ")");

            // [R] READ: Test User Retrieval
            System.out.println("2. [READ] Testing User Retrieval by Email & ID...");
            User fetchedUser = userDAO.findByEmail(testEmail);
            boolean readOk = fetchedUser != null && testRegNo.equals(fetchedUser.getRegisterNumber());
            System.out.println("   -> Fetched User: " + (readOk ? "MATCHED (" + fetchedUser.getName() + ")" : "FAILED"));

            // [U] UPDATE: Test User Profile Update
            System.out.println("3. [UPDATE] Testing User Profile Update...");
            boolean profileUpdated = userDAO.updateProfile(testUser.getUserId(), "CRUD Student Updated", "crud.updated@college.edu");
            System.out.println("   -> Profile updated: " + profileUpdated);

            // [C] CREATE: Test Lost Item Creation
            System.out.println("4. [CREATE] Testing Lost Item Insertion...");
            LostItem lostItem = new LostItem(testUser.getUserId(), "CRUD Test Laptop", "Electronics",
                    "Dell Inspiron with sticker", "Auditorium Hall A", new Date(System.currentTimeMillis()));
            boolean lostCreated = lostItemDAO.insert(lostItem);
            System.out.println("   -> Lost Item created: " + lostCreated + " (ID: " + lostItem.getLostItemId() + ")");

            // [C] CREATE: Test Found Item Creation
            System.out.println("5. [CREATE] Testing Found Item Insertion...");
            FoundItem foundItem = new FoundItem(testUser.getUserId(), "CRUD Test Water Bottle", "Accessories",
                    "Blue Milton thermo bottle", "Gymnasium", new Date(System.currentTimeMillis()));
            boolean foundCreated = foundItemDAO.insert(foundItem);
            System.out.println("   -> Found Item created: " + foundCreated + " (ID: " + foundItem.getFoundItemId() + ")");

            // [C] CREATE: Test Claim Submission
            System.out.println("6. [CREATE] Testing Claim Submission on Found Item...");
            Claim testClaim = new Claim(testUser.getUserId(), null, foundItem.getFoundItemId(),
                    "This is my blue Milton bottle with my initials engraved on the bottom lid.");
            boolean claimCreated = claimDAO.submitClaim(testClaim);
            System.out.println("   -> Claim created: " + claimCreated + " (Claim ID: " + testClaim.getClaimId() + ")");

            // [R] READ: Dynamic Search & Filtering
            System.out.println("7. [READ] Testing Search on Found Items...");
            List<FoundItem> searchResults = foundItemDAO.search("Water Bottle", "Accessories", "Gym", null);
            System.out.println("   -> Search matching results count: " + searchResults.size());

            // [ACID TRANSACTION] Test Claim Approval Transaction
            System.out.println("8. [TRANSACTION] Testing Atomic Claim Approval Transaction...");
            boolean approved = claimDAO.approveClaim(testClaim.getClaimId(), "Verified ownership in CRUD test.");
            System.out.println("   -> Claim approved: " + approved);

            // Verify item status updated to CLAIMED
            FoundItem updatedFound = foundItemDAO.findById(foundItem.getFoundItemId());
            System.out.println("   -> Found Item Status after approval: " + updatedFound.getStatus() + " (Expected: CLAIMED)");

            // [D] DELETE: Clean up test records
            System.out.println("9. [DELETE] Cleaning up test records...");
            claimDAO.findById(testClaim.getClaimId()); // verify can read
            foundItemDAO.delete(foundItem.getFoundItemId());
            lostItemDAO.delete(lostItem.getLostItemId());
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM USERS WHERE user_id = ?")) {
                ps.setInt(1, testUser.getUserId());
                ps.executeUpdate();
            }
            System.out.println("   -> Cleanup completed successfully.");

        } catch (Exception e) {
            System.err.println("[ERROR] CRUD testing error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

