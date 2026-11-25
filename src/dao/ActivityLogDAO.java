package dao;

import model.ActivityLog;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the ADMIN_ACTIVITY_LOG table.
 * Provides methods to record and query administrative activity entries.
 * Automatically ensures the table exists in the database.
 */
public class ActivityLogDAO {

    public ActivityLogDAO() {
        ensureTableExists();
    }

    /**
     * Creates the ADMIN_ACTIVITY_LOG table if it does not already exist.
     */
    public void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS ADMIN_ACTIVITY_LOG (" +
                     "    log_id             INT           PRIMARY KEY AUTO_INCREMENT," +
                     "    admin_id           INT           NOT NULL," +
                     "    action_type        VARCHAR(60)   NOT NULL," +
                     "    target_description VARCHAR(255)  NOT NULL," +
                     "    details            TEXT," +
                     "    performed_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP," +
                     "    CONSTRAINT fk_activity_log_admin FOREIGN KEY (admin_id) " +
                     "        REFERENCES USERS(user_id) ON DELETE CASCADE," +
                     "    INDEX idx_activity_log_admin    (admin_id)," +
                     "    INDEX idx_activity_log_type     (action_type)," +
                     "    INDEX idx_activity_log_time     (performed_at)" +
                     ") ENGINE=InnoDB";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            // Silently log warning, do not crash application
            System.err.println("[INFO] Note on ADMIN_ACTIVITY_LOG table check: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Write Operations
    // -------------------------------------------------------------------------

    /**
     * Inserts a new activity log entry for the given admin.
     *
     * @param adminId           the user_id of the administrator performing the action
     * @param actionType        a short constant describing the action (e.g. "CLAIM_APPROVED")
     * @param targetDescription human-readable description of the subject acted upon
     * @param details           optional extra context; may be null
     * @throws SQLException if a database error occurs
     */
    public void logActivity(int adminId, String actionType,
                            String targetDescription, String details) throws SQLException {
        String sql = "INSERT INTO ADMIN_ACTIVITY_LOG " +
                     "(admin_id, action_type, target_description, details) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            ps.setString(2, actionType);
            ps.setString(3, targetDescription);
            if (details != null) {
                ps.setString(4, details);
            } else {
                ps.setNull(4, Types.VARCHAR);
            }
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Read Operations
    // -------------------------------------------------------------------------

    /**
     * Retrieves the most recent activity log entries across all administrators,
     * joined with the USERS table to include the admin's display name.
     *
     * @param limit maximum number of records to return
     * @return list of {@link ActivityLog} objects ordered newest-first
     * @throws SQLException if a database error occurs
     */
    public List<ActivityLog> findRecent(int limit) throws SQLException {
        String sql = "SELECT al.log_id, al.admin_id, u.name AS admin_name, " +
                     "       al.action_type, al.target_description, al.details, al.performed_at " +
                     "FROM ADMIN_ACTIVITY_LOG al " +
                     "JOIN USERS u ON al.admin_id = u.user_id " +
                     "ORDER BY al.performed_at DESC " +
                     "LIMIT ?";

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Searches activity log by action type filter and/or search keyword.
     */
    public List<ActivityLog> search(String actionTypeFilter, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT al.log_id, al.admin_id, u.name AS admin_name, " +
                "       al.action_type, al.target_description, al.details, al.performed_at " +
                "FROM ADMIN_ACTIVITY_LOG al " +
                "JOIN USERS u ON al.admin_id = u.user_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (actionTypeFilter != null && !actionTypeFilter.trim().isEmpty() && !actionTypeFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND al.action_type = ? ");
            params.add(actionTypeFilter.trim());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(al.target_description) LIKE ? OR LOWER(al.details) LIKE ? OR LOWER(u.name) LIKE ?) ");
            String pat = "%" + keyword.toLowerCase().trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        sql.append("ORDER BY al.performed_at DESC LIMIT 200");

        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Retrieves the most recent activity log entries for a specific administrator.
     *
     * @param adminId the user_id of the administrator whose logs are requested
     * @param limit   maximum number of records to return
     * @return list of {@link ActivityLog} objects ordered newest-first
     * @throws SQLException if a database error occurs
     */
    public List<ActivityLog> findByAdminId(int adminId, int limit) throws SQLException {
        String sql = "SELECT al.log_id, al.admin_id, u.name AS admin_name, " +
                     "       al.action_type, al.target_description, al.details, al.performed_at " +
                     "FROM ADMIN_ACTIVITY_LOG al " +
                     "JOIN USERS u ON al.admin_id = u.user_id " +
                     "WHERE al.admin_id = ? " +
                     "ORDER BY al.performed_at DESC " +
                     "LIMIT ?";

        List<ActivityLog> logs = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Returns the total number of activity log entries in the table.
     *
     * @return total count
     * @throws SQLException if a database error occurs
     */
    public int countTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ADMIN_ACTIVITY_LOG";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // -------------------------------------------------------------------------
    // Private Helpers
    // -------------------------------------------------------------------------

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setLogId(rs.getInt("log_id"));
        log.setAdminId(rs.getInt("admin_id"));
        log.setAdminName(rs.getString("admin_name"));
        log.setActionType(rs.getString("action_type"));
        log.setTargetDescription(rs.getString("target_description"));
        log.setDetails(rs.getString("details"));
        log.setPerformedAt(rs.getTimestamp("performed_at"));
        return log;
    }
}
