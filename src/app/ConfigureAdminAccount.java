package app;

import model.User;
import service.AuthService;
import ui.AdminDashboard;
import util.DBConnection;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility to configure and verify default administrator accounts.
 * Passwords are never hardcoded in source code — provided via CLI arguments.
 */
public class ConfigureAdminAccount {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java app.ConfigureAdminAccount <email> <password>");
            System.exit(1);
        }

        String targetEmail = args[0].trim();
        String rawPassword = args[1];

        System.out.println("==========================================================");
        System.out.println("ADMINISTRATOR ACCOUNT CONFIGURATION & VERIFICATION");
        System.out.println("==========================================================");
        System.out.println("Target Admin Email : " + targetEmail);
        System.out.println("Database Target    : smart_lost_found @ localhost:3306");
        System.out.println("----------------------------------------------------------");

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Inspect existing USERS table for this email or related account
            Integer existingUserId = null;
            String existingRole = null;
            String existingName = null;
            String existingReg = null;

            String checkSql = "SELECT user_id, name, register_number, email, role, account_status FROM USERS " +
                              "WHERE LOWER(email) = LOWER(?) OR LOWER(email) = 'mohithkrishna.r.2025.csbs@rajalakshmi.edu.in'";

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, targetEmail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingUserId = rs.getInt("user_id");
                        existingName = rs.getString("name");
                        existingReg = rs.getString("register_number");
                        existingRole = rs.getString("role");
                        System.out.printf("[INFO] Existing account found: ID #%d | Name: %s | Reg: %s | Current Role: %s%n",
                                existingUserId, existingName, existingReg, existingRole);
                    }
                }
            }

            // 2. Hash password securely using existing PBKDF2 implementation
            String hashedPassword = PasswordUtil.hashPassword(rawPassword);
            System.out.println("[INFO] Password hashed securely using PBKDF2WithHmacSHA256 (65536 iterations).");

            if (existingUserId != null) {
                // Account exists -> update role to ADMIN, set active, update password hash, and set email to targetEmail
                System.out.println("[INFO] Updating existing account (ID #" + existingUserId + ") to ADMIN...");
                String updateSql = "UPDATE USERS SET email = ?, role = 'ADMIN', password_hash = ?, account_status = 'ACTIVE' " +
                                  "WHERE user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, targetEmail);
                    ps.setString(2, hashedPassword);
                    ps.setInt(3, existingUserId);
                    int rows = ps.executeUpdate();
                    System.out.println("[PASS] Account successfully updated to ADMIN! (Rows affected: " + rows + ")");
                }
            } else {
                // Account does not exist -> insert new ADMIN account
                System.out.println("[INFO] Account does not exist. Creating new ADMIN account...");
                String insertSql = "INSERT INTO USERS (name, register_number, email, password_hash, role, account_status) " +
                                  "VALUES ('Mohith Krishna R', 'ADMIN-MK', ?, ?, 'ADMIN', 'ACTIVE')";
                try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, targetEmail);
                    ps.setString(2, hashedPassword);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            existingUserId = rs.getInt(1);
                        }
                    }
                    System.out.println("[PASS] New ADMIN account created with ID #" + existingUserId);
                }
            }

            // 3. Verify in database using PreparedStatement
            System.out.println("\n[INFO] Verifying database record...");
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT user_id, name, register_number, email, role, account_status FROM USERS WHERE LOWER(email) = LOWER(?)")) {
                ps.setString(1, targetEmail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.printf("  Verified DB Record -> ID: %d | Name: %s | Reg: %s | Email: %s | Role: %s | Status: %s%n",
                                rs.getInt("user_id"),
                                rs.getString("name"),
                                rs.getString("register_number"),
                                rs.getString("email"),
                                rs.getString("role"),
                                rs.getString("account_status"));

                        if (!"ADMIN".equalsIgnoreCase(rs.getString("role"))) {
                            throw new RuntimeException("Role verification failed: expected ADMIN, got " + rs.getString("role"));
                        }
                        if (!"ACTIVE".equalsIgnoreCase(rs.getString("account_status"))) {
                            throw new RuntimeException("Status verification failed: expected ACTIVE, got " + rs.getString("account_status"));
                        }
                    } else {
                        throw new RuntimeException("Account not found after update!");
                    }
                }
            }

            // 4. Test authentication through AuthService.java
            System.out.println("\n[INFO] Testing authentication through AuthService.login()...");
            AuthService authService = new AuthService();
            User authenticatedUser = authService.login(targetEmail, rawPassword);

            if (authenticatedUser == null) {
                throw new RuntimeException("AuthService.login returned null!");
            }

            System.out.println("  [PASS] AuthService authenticated user: " + authenticatedUser.getName());
            System.out.println("  [PASS] Authenticated user role: " + authenticatedUser.getRole());
            System.out.println("  [PASS] user.isAdmin() check: " + authenticatedUser.isAdmin());

            if (!authenticatedUser.isAdmin()) {
                throw new RuntimeException("AuthService failed to recognize user as ADMIN!");
            }

            // 5. Test AdminDashboard initialization for this user
            System.out.println("\n[INFO] Testing AdminDashboard.java initialization for this user...");
            AdminDashboard dashboard = new AdminDashboard(authenticatedUser);
            dashboard.dispose(); // clean up resources immediately
            System.out.println("  [PASS] AdminDashboard initialized and disposed successfully for " + authenticatedUser.getEmail());

            System.out.println("\n==========================================================");
            System.out.println("ADMINISTRATOR ACCOUNT CONFIGURATION: 100% SUCCESS");
            System.out.println("==========================================================");

        } catch (Exception e) {
            System.err.println("\n[ERROR] Configuration or verification failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

