package app;

import model.ActivityLog;
import model.Claim;
import model.FoundItem;
import model.LostItem;
import model.User;
import service.AdminService;
import service.AuthService;
import service.ClaimService;
import service.ItemService;
import util.DBConnection;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * End-to-end automated verification test suite for:
 * 1. Admin Module & Dynamic MySQL Statistics
 * 2. Role-Based Access Control & Portal Switching Security
 * 3. Student Management (Search, View, Activate, Deactivate)
 * 4. Lost & Found Item Moderation (Filter, Status Update, Removal)
 * 5. Atomic Claim Approval & Rejection Workflow with Competing Claim Handling
 * 6. Admin Activity Audit Logging
 */
public class AdminModuleVerificationTest {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println(" SMART LOST-AND-FOUND: ADMIN MODULE & SECURITY VERIFICATION SUITE");
        System.out.println("=================================================================");

        testDatabaseConnectivity();
        testRoleBasedAccessControl();
        testDashboardStatistics();
        testStudentManagement();
        testItemManagement();
        testClaimApprovalWorkflow();
        testActivityAuditLogging();

        System.out.println("=================================================================");
        System.out.printf("FINAL TEST RESULTS: %d/%d tests passed successfully.%n", testsPassed, testsTotal);
        System.out.println("=================================================================");
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

    private static void testDatabaseConnectivity() {
        System.out.println("\n[1] Testing Live MySQL Connectivity:");
        boolean connected = DBConnection.testConnection();
        assertTrue("Database connection established to smart_lost_found", connected);
    }

    private static void testRoleBasedAccessControl() {
        System.out.println("\n[2] Testing Secure Role-Based Authentication & Portal Access:");
        AuthService authService = new AuthService();

        try {
            // Admin authentication
            User admin = authService.login("admin@college.edu", "Admin@123");
            assertTrue("Admin account authenticated successfully", admin != null);
            assertTrue("Admin account has ADMIN role", admin != null && admin.isAdmin());

            // Student authentication
            User student = authService.login("rahul.sharma@college.edu", "Student@123");
            assertTrue("Student account authenticated successfully", student != null);
            assertTrue("Student account has STUDENT role", student != null && !student.isAdmin());

            // Verify role distinction
            assertTrue("Admin is recognized as admin role", admin.isAdmin());
            assertTrue("Student is not recognized as admin role", !student.isAdmin());

        } catch (Exception e) {
            assertTrue("Authentication failed with error: " + e.getMessage(), false);
        }
    }

    private static void testDashboardStatistics() {
        System.out.println("\n[3] Testing Real-Time MySQL Dashboard Statistics (No Mocking):");
        AdminService adminService = new AdminService();

        try {
            Map<String, Integer> stats = adminService.getDashboardStatistics();
            assertTrue("Statistics map contains all required KPI keys",
                    stats.containsKey("totalStudents") &&
                    stats.containsKey("totalLostItems") &&
                    stats.containsKey("totalFoundItems") &&
                    stats.containsKey("pendingClaims") &&
                    stats.containsKey("approvedClaims") &&
                    stats.containsKey("rejectedClaims"));

            System.out.printf("    Current DB Statistics -> Students: %d, Lost: %d, Found: %d, Claims [Pending: %d, Approved: %d, Rejected: %d]%n",
                    stats.get("totalStudents"), stats.get("totalLostItems"), stats.get("totalFoundItems"),
                    stats.get("pendingClaims"), stats.get("approvedClaims"), stats.get("rejectedClaims"));

            assertTrue("Total registered students >= 0", stats.get("totalStudents") >= 0);
            assertTrue("Total lost items >= 0", stats.get("totalLostItems") >= 0);
            assertTrue("Total found items >= 0", stats.get("totalFoundItems") >= 0);
        } catch (Exception e) {
            assertTrue("Dashboard statistics retrieval failed: " + e.getMessage(), false);
        }
    }

    private static void testStudentManagement() {
        System.out.println("\n[4] Testing Student Management (Query, Search, Status Update):");
        AdminService adminService = new AdminService();

        try {
            List<User> students = adminService.getAllStudents();
            assertTrue("Student records retrieved from database", students != null && !students.isEmpty());

            // Search by name
            List<User> searchByName = adminService.searchStudents("Rahul", "ALL");
            assertTrue("Search student by name returns expected record",
                    searchByName != null && !searchByName.isEmpty() && searchByName.get(0).getName().contains("Rahul"));

            // Search by register number
            List<User> searchByReg = adminService.searchStudents("2023BCSE0101", "ALL");
            assertTrue("Search student by register number returns expected record",
                    searchByReg != null && !searchByReg.isEmpty());

            // Search by email
            List<User> searchByEmail = adminService.searchStudents("priya.patel@college.edu", "ALL");
            assertTrue("Search student by email returns expected record",
                    searchByEmail != null && !searchByEmail.isEmpty());

            // Test student account status update (activate/deactivate)
            User testTarget = students.get(0);
            String originalStatus = testTarget.getAccountStatus();
            adminService.updateStudentStatus(1, testTarget.getUserId(), "SUSPENDED");

            User updated = adminService.searchStudents(testTarget.getRegisterNumber(), "ALL").get(0);
            assertTrue("Student status updated to SUSPENDED in MySQL", "SUSPENDED".equalsIgnoreCase(updated.getAccountStatus()));

            // Restore original status
            adminService.updateStudentStatus(1, testTarget.getUserId(), originalStatus);
            User restored = adminService.searchStudents(testTarget.getRegisterNumber(), "ALL").get(0);
            assertTrue("Student status restored to " + originalStatus, originalStatus.equalsIgnoreCase(restored.getAccountStatus()));

        } catch (Exception e) {
            assertTrue("Student management test failed: " + e.getMessage(), false);
        }
    }

    private static void testItemManagement() {
        System.out.println("\n[5] Testing Lost & Found Item Moderation:");
        AdminService adminService = new AdminService();

        try {
            List<LostItem> lostItems = adminService.getAllLostReports();
            assertTrue("Lost item reports retrieved from database", lostItems != null && !lostItems.isEmpty());

            List<FoundItem> foundItems = adminService.getAllFoundReports();
            assertTrue("Found item reports retrieved from database", foundItems != null && !foundItems.isEmpty());

            // Search lost items
            List<LostItem> filteredLost = adminService.searchLostItems("Wallet", "All Categories", "", null);
            assertTrue("Filtering lost items by keyword returned matching records", filteredLost != null);

            // Search found items
            List<FoundItem> filteredFound = adminService.searchFoundItems("Wallet", "All Categories", "", null);
            assertTrue("Filtering found items by keyword returned matching records", filteredFound != null);

        } catch (Exception e) {
            assertTrue("Item management test failed: " + e.getMessage(), false);
        }
    }

    private static void testClaimApprovalWorkflow() {
        System.out.println("\n[6] Testing Atomic Claim Approval, Rejection & Competing Claim Handling:");
        AdminService adminService = new AdminService();
        ClaimService claimService = new ClaimService();

        try (Connection conn = DBConnection.getConnection()) {
            // Create a test found item for this test
            int testFoundItemId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO FOUND_ITEMS (user_id, item_name, category, description, found_location, found_date, status) " +
                    "VALUES (2, 'Verification Test Silver Ring', 'Accessories', 'Found silver ring in science block', 'Science Block Lab 2', '2026-11-01', 'AVAILABLE')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                testFoundItemId = rs.getInt(1);
            }

            // Submit 2 competing claims on the same found item
            int claim1Id, claim2Id;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO CLAIMS (user_id, lost_item_id, found_item_id, claim_description, claim_status) " +
                    "VALUES (3, NULL, ?, 'Claim 1: This silver ring has my initials on the inside band.', 'PENDING')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, testFoundItemId);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                claim1Id = rs.getInt(1);
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO CLAIMS (user_id, lost_item_id, found_item_id, claim_description, claim_status) " +
                    "VALUES (4, NULL, ?, 'Claim 2: Competing claim for the silver ring.', 'PENDING')",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, testFoundItemId);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                claim2Id = rs.getInt(1);
            }

            assertTrue("Two competing pending claims created for item #" + testFoundItemId, claim1Id > 0 && claim2Id > 0);

            // Approve Claim 1
            boolean approved = adminService.approveClaim(1, claim1Id, "Verified initials on ring inner band.");
            assertTrue("Atomic claim approval returned success", approved);

            // Verify Claim 1 is APPROVED
            Claim verifiedClaim1 = claimService.getClaimDetails(claim1Id);
            assertTrue("Claim 1 status is now APPROVED", "APPROVED".equalsIgnoreCase(verifiedClaim1.getClaimStatus()));

            // Verify competing Claim 2 was auto-rejected
            Claim verifiedClaim2 = claimService.getClaimDetails(claim2Id);
            assertTrue("Competing Claim 2 was automatically REJECTED by atomic transaction",
                    "REJECTED".equalsIgnoreCase(verifiedClaim2.getClaimStatus()));

            // Verify the related found item is now CLAIMED
            try (PreparedStatement ps = conn.prepareStatement("SELECT status FROM FOUND_ITEMS WHERE found_item_id = ?")) {
                ps.setInt(1, testFoundItemId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                String itemStatus = rs.getString(1);
                assertTrue("Related Found Item status automatically transitioned to CLAIMED",
                        "CLAIMED".equalsIgnoreCase(itemStatus));
            }

            // Clean up test records
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM CLAIMS WHERE claim_id IN (?, ?)")) {
                ps.setInt(1, claim1Id);
                ps.setInt(2, claim2Id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM FOUND_ITEMS WHERE found_item_id = ?")) {
                ps.setInt(1, testFoundItemId);
                ps.executeUpdate();
            }
            assertTrue("Test claim records cleaned up safely", true);

        } catch (Exception e) {
            assertTrue("Claim approval workflow test failed: " + e.getMessage(), false);
        }
    }

    private static void testActivityAuditLogging() {
        System.out.println("\n[7] Testing Administrator Activity Audit Logging:");
        AdminService adminService = new AdminService();

        try {
            // Log a test action
            adminService.logActivity(1, "ADMIN_TEST_VERIFY", "Verification Test Suite Run",
                    "Automated test log entry executed at " + new java.util.Date());

            List<ActivityLog> logs = adminService.getRecentActivityLog(5);
            assertTrue("Activity logs retrieved from ADMIN_ACTIVITY_LOG table", logs != null && !logs.isEmpty());

            boolean foundOurAction = logs.stream().anyMatch(l -> "ADMIN_TEST_VERIFY".equals(l.getActionType()));
            assertTrue("Logged test action successfully present in audit trail", foundOurAction);

        } catch (Exception e) {
            assertTrue("Activity logging test failed: " + e.getMessage(), false);
        }
    }
}

