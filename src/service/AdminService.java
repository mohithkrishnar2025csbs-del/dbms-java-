package service;

import dao.ActivityLogDAO;
import dao.ClaimDAO;
import dao.FoundItemDAO;
import dao.LostItemDAO;
import dao.UserDAO;
import model.ActivityLog;
import model.Claim;
import model.FoundItem;
import model.LostItem;
import model.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for administrator operations: statistics, student management, item moderation,
 * claim verification, and audit logging.
 */
public class AdminService {
    private final UserDAO userDAO;
    private final LostItemDAO lostItemDAO;
    private final FoundItemDAO foundItemDAO;
    private final ClaimDAO claimDAO;
    private final ActivityLogDAO activityLogDAO;

    public AdminService() {
        this.userDAO = new UserDAO();
        this.lostItemDAO = new LostItemDAO();
        this.foundItemDAO = new FoundItemDAO();
        this.claimDAO = new ClaimDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }

    /**
     * Aggregates real-time dashboard statistics from database queries.
     */
    public Map<String, Integer> getDashboardStatistics() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalStudents", userDAO.countTotalStudents());
        stats.put("totalLostItems", lostItemDAO.countTotal());
        stats.put("totalFoundItems", foundItemDAO.countTotal());
        stats.put("pendingClaims", claimDAO.countByStatus("PENDING"));
        stats.put("approvedClaims", claimDAO.countByStatus("APPROVED"));
        stats.put("rejectedClaims", claimDAO.countByStatus("REJECTED"));
        return stats;
    }

    // -------------------------------------------------------------------------
    // Student Management Operations
    // -------------------------------------------------------------------------

    public List<User> getAllStudents() throws SQLException {
        return userDAO.findAllStudents();
    }

    public List<User> searchStudents(String keyword, String statusFilter) throws SQLException {
        return userDAO.searchStudents(keyword, statusFilter);
    }

    public List<User> searchStudents(String keyword) throws SQLException {
        return userDAO.searchStudents(keyword, "ALL");
    }

    public boolean updateStudentStatus(int adminId, int userId, String status) throws SQLException {
        boolean ok = userDAO.updateAccountStatus(userId, status);
        if (ok) {
            String action = "ACTIVE".equalsIgnoreCase(status) ? "STUDENT_ACTIVATED" : "STUDENT_DEACTIVATED";
            logActivity(adminId, action, "Student Account #" + userId + " set to " + status,
                    "Admin changed account status to " + status);
        }
        return ok;
    }

    public boolean updateStudentStatus(int userId, String status) throws SQLException {
        return userDAO.updateAccountStatus(userId, status);
    }

    // -------------------------------------------------------------------------
    // Item Moderation Operations
    // -------------------------------------------------------------------------

    public List<LostItem> getAllLostReports() throws SQLException {
        return lostItemDAO.findAll();
    }

    public List<FoundItem> getAllFoundReports() throws SQLException {
        return foundItemDAO.findAll();
    }

    public List<LostItem> searchLostItems(String keyword, String category, String location, java.sql.Date date) throws SQLException {
        return lostItemDAO.search(keyword, category, location, date);
    }

    public List<FoundItem> searchFoundItems(String keyword, String category, String location, java.sql.Date date) throws SQLException {
        return foundItemDAO.search(keyword, category, location, date);
    }

    public List<LostItem> getRecentLostItems(int limit) throws SQLException {
        return lostItemDAO.findRecent(limit);
    }

    public List<FoundItem> getRecentFoundItems(int limit) throws SQLException {
        return foundItemDAO.findRecent(limit);
    }

    public boolean updateLostItemStatus(int adminId, int lostItemId, String status) throws SQLException {
        boolean ok = lostItemDAO.updateStatus(lostItemId, status);
        if (ok) {
            logActivity(adminId, "ITEM_STATUS_UPDATED", "Lost Item #" + lostItemId + " status set to " + status,
                    "Admin updated lost item status to " + status);
        }
        return ok;
    }

    public boolean updateFoundItemStatus(int adminId, int foundItemId, String status) throws SQLException {
        boolean ok = foundItemDAO.updateStatus(foundItemId, status);
        if (ok) {
            logActivity(adminId, "ITEM_STATUS_UPDATED", "Found Item #" + foundItemId + " status set to " + status,
                    "Admin updated found item status to " + status);
        }
        return ok;
    }

    public boolean deleteLostItem(int adminId, int lostItemId, String itemName) throws SQLException {
        boolean ok = lostItemDAO.delete(lostItemId);
        if (ok) {
            logActivity(adminId, "ITEM_DELETED", "Deleted Lost Item #" + lostItemId + " (" + itemName + ")",
                    "Admin permanently removed record (duplicate/inappropriate report)");
        }
        return ok;
    }

    public boolean deleteFoundItem(int adminId, int foundItemId, String itemName) throws SQLException {
        boolean ok = foundItemDAO.delete(foundItemId);
        if (ok) {
            logActivity(adminId, "ITEM_DELETED", "Deleted Found Item #" + foundItemId + " (" + itemName + ")",
                    "Admin permanently removed record (duplicate/inappropriate report)");
        }
        return ok;
    }

    // -------------------------------------------------------------------------
    // Claim Management Operations
    // -------------------------------------------------------------------------

    public List<Claim> getRecentClaims(int limit) throws SQLException {
        return claimDAO.findRecent(limit);
    }

    public List<Claim> getAllClaims(String statusFilter) throws SQLException {
        return claimDAO.findAll(statusFilter);
    }

    public List<Claim> searchClaims(String statusFilter, String searchQuery) throws SQLException {
        return claimDAO.searchClaims(statusFilter, searchQuery);
    }

    public boolean approveClaim(int adminId, int claimId, String remarks) throws SQLException {
        boolean ok = claimDAO.approveClaim(claimId, remarks);
        if (ok) {
            logActivity(adminId, "CLAIM_APPROVED", "Claim #" + claimId + " APPROVED", remarks);
        }
        return ok;
    }

    public boolean rejectClaim(int adminId, int claimId, String remarks) throws SQLException {
        boolean ok = claimDAO.rejectClaim(claimId, remarks);
        if (ok) {
            logActivity(adminId, "CLAIM_REJECTED", "Claim #" + claimId + " REJECTED", remarks);
        }
        return ok;
    }

    public boolean addAdminRemarks(int adminId, int claimId, String remarks) throws SQLException {
        boolean ok = claimDAO.updateRemarks(claimId, remarks);
        if (ok) {
            logActivity(adminId, "ADMIN_REMARKS_ADDED", "Remarks saved on Claim #" + claimId, remarks);
        }
        return ok;
    }

    // -------------------------------------------------------------------------
    // Activity History Operations
    // -------------------------------------------------------------------------

    /**
     * Logs an administrative action to ADMIN_ACTIVITY_LOG.
     * Silently swallows DB errors so that logging never breaks the main operation.
     */
    public void logActivity(int adminId, String actionType, String targetDescription, String details) {
        try {
            activityLogDAO.logActivity(adminId, actionType, targetDescription, details);
        } catch (SQLException e) {
            System.err.println("[INFO] Activity log notice: " + e.getMessage());
        }
    }

    public List<ActivityLog> getRecentActivityLog(int limit) throws SQLException {
        return activityLogDAO.findRecent(limit);
    }

    public List<ActivityLog> searchActivityLog(String actionType, String keyword) throws SQLException {
        return activityLogDAO.search(actionType, keyword);
    }
}
