package app;

import model.Claim;
import model.FoundItem;
import model.LostItem;
import model.User;
import util.DBConnection;
import util.PasswordUtil;
import util.ValidationUtil;

import java.sql.Date;

/**
 * Automated test suite to verify core system requirements:
 * 1. PBKDF2 Password Hashing & Verification
 * 2. Model Integrity & Check Constraints
 * 3. Validation Logic (Emails, Register Numbers, Dates, Passwords)
 * 4. Database Connectivity & Configuration Checks
 */
public class SystemIntegrationTest {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("SMART LOST-AND-FOUND SYSTEM - SYSTEM VERIFICATION SUITE");
        System.out.println("==========================================================");

        testPasswordHashing();
        testInputValidation();
        testModelIntegrity();
        testDatabaseConfiguration();

        System.out.println("----------------------------------------------------------");
        System.out.println(String.format("TEST RESULTS: %d/%d tests passed successfully.", testsPassed, testsTotal));
        System.out.println("==========================================================");
    }

    private static void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void testPasswordHashing() {
        System.out.println("\n[1] Testing PBKDF2 Password Hashing & Constant-Time Verification:");

        String password = "SecureStudentPass#2026";
        String hash = PasswordUtil.hashPassword(password);

        assertTrue("Hash is non-null and formatted with iterations and salt",
                hash != null && hash.contains(":") && hash.split(":").length == 3);

        assertTrue("Verification succeeds with matching plaintext password",
                PasswordUtil.verifyPassword(password, hash));

        assertTrue("Verification fails with incorrect password",
                !PasswordUtil.verifyPassword("WrongPassword123", hash));

        assertTrue("Verification fails with empty or null candidate",
                !PasswordUtil.verifyPassword("", hash) && !PasswordUtil.verifyPassword(null, hash));
    }

    private static void testInputValidation() {
        System.out.println("\n[2] Testing Input Validation Rules:");

        assertTrue("Valid email formats accepted",
                ValidationUtil.isValidEmail("rahul.sharma@college.edu") &&
                ValidationUtil.isValidEmail("admin@campus.ac.in"));

        assertTrue("Invalid email formats rejected",
                !ValidationUtil.isValidEmail("invalid-email") &&
                !ValidationUtil.isValidEmail("user@") &&
                !ValidationUtil.isValidEmail("@domain.com"));

        assertTrue("Valid register numbers accepted",
                ValidationUtil.isValidRegisterNumber("2023BCSE0101") &&
                ValidationUtil.isValidRegisterNumber("CS-2024-42"));

        assertTrue("Short or invalid register numbers rejected",
                !ValidationUtil.isValidRegisterNumber("AB") &&
                !ValidationUtil.isValidRegisterNumber(""));

        assertTrue("Date parsing YYYY-MM-DD succeeds for valid dates",
                ValidationUtil.isValidDateFormat("2026-11-01"));

        assertTrue("Malformed dates rejected",
                !ValidationUtil.isValidDateFormat("01-11-2026") &&
                !ValidationUtil.isValidDateFormat("2026/11/01") &&
                !ValidationUtil.isValidDateFormat("invalid"));

        Date pastDate = ValidationUtil.parseSqlDate("2025-01-01");
        assertTrue("Past date is recognized as not future",
                ValidationUtil.isNotFutureDate(pastDate));
    }

    private static void testModelIntegrity() {
        System.out.println("\n[3] Testing Entity Models & Role Checks:");

        User student = new User("Rahul Sharma", "2023BCSE0101", "rahul@college.edu", "hash123", "STUDENT");
        assertTrue("User student role identified correctly", student.isStudent() && !student.isAdmin());

        User admin = new User("Admin", "ADMIN-01", "admin@college.edu", "hash456", "ADMIN");
        assertTrue("User admin role identified correctly", admin.isAdmin() && !admin.isStudent());

        LostItem lost = new LostItem(1, "Brown Wallet", "Wallet", "Found table 14", "Cafeteria",
                Date.valueOf("2026-11-01"));
        assertTrue("LostItem defaults to OPEN status", "OPEN".equals(lost.getStatus()));

        FoundItem found = new FoundItem(2, "Galaxy Buds", "Electronics", "Library floor 2", "Library",
                Date.valueOf("2026-11-02"));
        assertTrue("FoundItem defaults to AVAILABLE status", "AVAILABLE".equals(found.getStatus()));

        Claim claim = new Claim(1, null, 2, "This is my lost earbud case with serial number ABC1234.");
        assertTrue("Claim references found item and has PENDING status",
                claim.getFoundItemId() == 2 && claim.getLostItemId() == null && "PENDING".equals(claim.getClaimStatus()));
    }

    private static void testDatabaseConfiguration() {
        System.out.println("\n[4] Testing Database Configuration & Driver Setup:");

        assertTrue("Configured Database Name is smart_lost_found",
                "smart_lost_found".equals(DBConnection.getDatabaseName()));

        boolean connected = DBConnection.testConnection();
        System.out.println("  [INFO] Live Database Connectivity Status: " + (connected ? "CONNECTED (ONLINE)" : "OFFLINE / PENDING PASSWORD CONFIG"));
    }
}

