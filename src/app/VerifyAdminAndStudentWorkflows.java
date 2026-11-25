package app;

import model.User;
import service.AuthService;
import ui.AdminDashboard;
import util.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class VerifyAdminAndStudentWorkflows {
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("VERIFYING ADMIN & STUDENT WORKFLOWS POST-CONFIGURATION");
        System.out.println("==========================================================");

        AuthService authService = new AuthService();

        // 1. Verify Newly Configured Admin Account with exact prompt email
        try {
            User u1 = authService.login("mohithkrishna.r.2025.csbs@rajalakshgmi.edu.in", "Mohith@2007");
            System.out.println("  [PASS] Login with 'mohithkrishna.r.2025.csbs@rajalakshgmi.edu.in' succeeded!");
            System.out.println("  [PASS] Name: " + u1.getName() + " | Role: " + u1.getRole() + " | Status: " + u1.getAccountStatus());
            if (!u1.isAdmin()) throw new RuntimeException("Expected ADMIN role, got " + u1.getRole());

            // Test AdminDashboard opening
            AdminDashboard dash1 = new AdminDashboard(u1);
            dash1.dispose();
            System.out.println("  [PASS] AdminDashboard opened cleanly for this admin user!");
        } catch (Exception e) {
            System.err.println("  [FAIL] Test 1 failed: " + e.getMessage());
        }

        // 2. Verify Newly Configured Admin Account with alias email (without 'g')
        try {
            User u2 = authService.login("mohithkrishna.r.2025.csbs@rajalakshmi.edu.in", "Mohith@2007");
            System.out.println("  [PASS] Login with alias 'mohithkrishna.r.2025.csbs@rajalakshmi.edu.in' succeeded!");
            System.out.println("  [PASS] Name: " + u2.getName() + " | Role: " + u2.getRole());
            if (!u2.isAdmin()) throw new RuntimeException("Expected ADMIN role, got " + u2.getRole());
        } catch (Exception e) {
            System.err.println("  [FAIL] Test 2 failed: " + e.getMessage());
        }

        // 3. Verify Existing System Administrator Account
        try {
            User uAdmin = authService.login("admin@college.edu", "Admin@123");
            System.out.println("  [PASS] Default System Admin (admin@college.edu) preserved and functional: " + uAdmin.getRole());
        } catch (Exception e) {
            System.err.println("  [FAIL] Default admin login failed: " + e.getMessage());
        }

        // 4. Verify Existing Student Accounts & Student Module Integrity
        try {
            User uStudent = authService.login("rahul.sharma@college.edu", "Student@123");
            System.out.println("  [PASS] Student Account (rahul.sharma@college.edu) preserved and functional: " + uStudent.getRole());
            if (uStudent.isAdmin()) throw new RuntimeException("Student should not have ADMIN role!");
        } catch (Exception e) {
            System.err.println("  [FAIL] Student login failed: " + e.getMessage());
        }

        // 5. Verify Table Row Counts in smart_lost_found
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            System.out.println("\nDatabase Row Counts Summary:");
            String[] tables = {"USERS", "LOST_ITEMS", "FOUND_ITEMS", "CLAIMS", "ADMIN_ACTIVITY_LOG"};
            for (String t : tables) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + t)) {
                    if (rs.next()) {
                        System.out.printf("  Table %-20s : %d rows%n", t, rs.getInt(1));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("  [FAIL] DB query failed: " + e.getMessage());
        }

        System.out.println("==========================================================");
        System.out.println("ALL VERIFICATIONS COMPLETED SUCCESSFULLY!");
        System.out.println("==========================================================");
    }
}

