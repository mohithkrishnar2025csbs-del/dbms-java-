package dao;

import model.Claim;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for CLAIMS table with transactional claim verification.
 */
public class ClaimDAO {

    /**
     * Submits a new claim.
     */
    public boolean submitClaim(Claim claim) throws SQLException {
        // Enforce rule: exactly one item ID must be populated
        if ((claim.getLostItemId() == null && claim.getFoundItemId() == null) ||
            (claim.getLostItemId() != null && claim.getFoundItemId() != null)) {
            throw new IllegalArgumentException("A claim must reference either a lost item or a found item, but not both.");
        }

        String sql = "INSERT INTO CLAIMS (user_id, lost_item_id, found_item_id, claim_description, claim_status) " +
                     "VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, claim.getUserId());
            if (claim.getLostItemId() != null) {
                ps.setInt(2, claim.getLostItemId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (claim.getFoundItemId() != null) {
                ps.setInt(3, claim.getFoundItemId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, claim.getClaimDescription());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        claim.setClaimId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Checks if a user already has an active or pending claim for this item.
     */
    public boolean hasExistingClaim(int userId, Integer lostItemId, Integer foundItemId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM CLAIMS WHERE user_id = ? AND claim_status IN ('PENDING', 'APPROVED') AND ");
        if (lostItemId != null) {
            sql.append("lost_item_id = ?");
        } else if (foundItemId != null) {
            sql.append("found_item_id = ?");
        } else {
            return false;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, userId);
            ps.setInt(2, lostItemId != null ? lostItemId : foundItemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Retrieves a claim by ID with full joined details.
     */
    public Claim findById(int claimId) throws SQLException {
        String sql = "SELECT c.claim_id, c.user_id, c.lost_item_id, c.found_item_id, c.claim_description, " +
                     "c.claim_date, c.claim_status, c.admin_remarks, " +
                     "u.name AS claimant_name, u.email AS claimant_email, u.register_number AS claimant_reg, " +
                     "COALESCE(l.item_name, f.item_name) AS target_item_name, " +
                     "COALESCE(l.category, f.category) AS target_item_category, " +
                     "CASE WHEN c.lost_item_id IS NOT NULL THEN 'LOST' ELSE 'FOUND' END AS target_item_type, " +
                     "COALESCE(l.lost_location, f.found_location) AS target_item_location, " +
                     "COALESCE(l.status, f.status) AS target_item_status " +
                     "FROM CLAIMS c " +
                     "JOIN USERS u ON c.user_id = u.user_id " +
                     "LEFT JOIN LOST_ITEMS l ON c.lost_item_id = l.lost_item_id " +
                     "LEFT JOIN FOUND_ITEMS f ON c.found_item_id = f.found_item_id " +
                     "WHERE c.claim_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowWithDetails(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all claims submitted by a specific student.
     */
    public List<Claim> findByUserId(int userId) throws SQLException {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT c.claim_id, c.user_id, c.lost_item_id, c.found_item_id, c.claim_description, " +
                     "c.claim_date, c.claim_status, c.admin_remarks, " +
                     "u.name AS claimant_name, u.email AS claimant_email, u.register_number AS claimant_reg, " +
                     "COALESCE(l.item_name, f.item_name) AS target_item_name, " +
                     "COALESCE(l.category, f.category) AS target_item_category, " +
                     "CASE WHEN c.lost_item_id IS NOT NULL THEN 'LOST' ELSE 'FOUND' END AS target_item_type, " +
                     "COALESCE(l.lost_location, f.found_location) AS target_item_location, " +
                     "COALESCE(l.status, f.status) AS target_item_status " +
                     "FROM CLAIMS c " +
                     "JOIN USERS u ON c.user_id = u.user_id " +
                     "LEFT JOIN LOST_ITEMS l ON c.lost_item_id = l.lost_item_id " +
                     "LEFT JOIN FOUND_ITEMS f ON c.found_item_id = f.found_item_id " +
                     "WHERE c.user_id = ? " +
                     "ORDER BY c.claim_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithDetails(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all claims for admin verification, filtered by status.
     */
    public List<Claim> findAll(String statusFilter) throws SQLException {
        return searchClaims(statusFilter, null);
    }

    /**
     * Searches claims by status and keyword (matches claim ID, claimant name, reg number, or item name).
     */
    public List<Claim> searchClaims(String statusFilter, String searchQuery) throws SQLException {
        List<Claim> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.claim_id, c.user_id, c.lost_item_id, c.found_item_id, c.claim_description, " +
                "c.claim_date, c.claim_status, c.admin_remarks, " +
                "u.name AS claimant_name, u.email AS claimant_email, u.register_number AS claimant_reg, " +
                "COALESCE(l.item_name, f.item_name) AS target_item_name, " +
                "COALESCE(l.category, f.category) AS target_item_category, " +
                "CASE WHEN c.lost_item_id IS NOT NULL THEN 'LOST' ELSE 'FOUND' END AS target_item_type, " +
                "COALESCE(l.lost_location, f.found_location) AS target_item_location, " +
                "COALESCE(l.status, f.status) AS target_item_status " +
                "FROM CLAIMS c " +
                "JOIN USERS u ON c.user_id = u.user_id " +
                "LEFT JOIN LOST_ITEMS l ON c.lost_item_id = l.lost_item_id " +
                "LEFT JOIN FOUND_ITEMS f ON c.found_item_id = f.found_item_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND c.claim_status = ? ");
            params.add(statusFilter.trim());
        }

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String q = searchQuery.trim();
            // Check if search query is integer (claim ID)
            try {
                int id = Integer.parseInt(q);
                sql.append("AND (c.claim_id = ? OR LOWER(u.name) LIKE ? OR LOWER(u.register_number) LIKE ? OR LOWER(COALESCE(l.item_name, f.item_name)) LIKE ?) ");
                params.add(id);
                String pattern = "%" + q.toLowerCase() + "%";
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            } catch (NumberFormatException nfe) {
                sql.append("AND (LOWER(u.name) LIKE ? OR LOWER(u.register_number) LIKE ? OR LOWER(COALESCE(l.item_name, f.item_name)) LIKE ?) ");
                String pattern = "%" + q.toLowerCase() + "%";
                params.add(pattern);
                params.add(pattern);
                params.add(pattern);
            }
        }

        sql.append("ORDER BY c.claim_date DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithDetails(rs));
                }
            }
        }
        return list;
    }

    /**
     * Updates administrator remarks on a claim.
     */
    public boolean updateRemarks(int claimId, String remarks) throws SQLException {
        String sql = "UPDATE CLAIMS SET admin_remarks = ? WHERE claim_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, remarks);
            ps.setInt(2, claimId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Approves a claim using a strict database transaction:
     * 1. Updates claim to APPROVED with remarks
     * 2. Updates item status to CLAIMED
     * 3. Automatically rejects other competing pending claims on the same item
     */
    public boolean approveClaim(int claimId, String remarks) throws SQLException {
        Claim claim = findById(claimId);
        if (claim == null) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Approve selected claim
            String sqlApprove = "UPDATE CLAIMS SET claim_status = 'APPROVED', admin_remarks = ? WHERE claim_id = ?";
            try (PreparedStatement psApprove = conn.prepareStatement(sqlApprove)) {
                psApprove.setString(1, remarks);
                psApprove.setInt(2, claimId);
                psApprove.executeUpdate();
            }

            // 2. Mark item as CLAIMED
            if (claim.getLostItemId() != null) {
                String sqlItem = "UPDATE LOST_ITEMS SET status = 'CLAIMED' WHERE lost_item_id = ?";
                try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                    psItem.setInt(1, claim.getLostItemId());
                    psItem.executeUpdate();
                }

                // 3. Reject other pending claims on this lost item
                String sqlRejectOthers = "UPDATE CLAIMS SET claim_status = 'REJECTED', " +
                        "admin_remarks = 'Closed automatically: another verified claim was approved for this item.' " +
                        "WHERE lost_item_id = ? AND claim_id != ? AND claim_status = 'PENDING'";
                try (PreparedStatement psReject = conn.prepareStatement(sqlRejectOthers)) {
                    psReject.setInt(1, claim.getLostItemId());
                    psReject.setInt(2, claimId);
                    psReject.executeUpdate();
                }
            } else if (claim.getFoundItemId() != null) {
                String sqlItem = "UPDATE FOUND_ITEMS SET status = 'CLAIMED' WHERE found_item_id = ?";
                try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                    psItem.setInt(1, claim.getFoundItemId());
                    psItem.executeUpdate();
                }

                // 3. Reject other pending claims on this found item
                String sqlRejectOthers = "UPDATE CLAIMS SET claim_status = 'REJECTED', " +
                        "admin_remarks = 'Closed automatically: another verified claim was approved for this item.' " +
                        "WHERE found_item_id = ? AND claim_id != ? AND claim_status = 'PENDING'";
                try (PreparedStatement psReject = conn.prepareStatement(sqlRejectOthers)) {
                    psReject.setInt(1, claim.getFoundItemId());
                    psReject.setInt(2, claimId);
                    psReject.executeUpdate();
                }
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Rejects a claim with administrator remarks.
     */
    public boolean rejectClaim(int claimId, String remarks) throws SQLException {
        String sql = "UPDATE CLAIMS SET claim_status = 'REJECTED', admin_remarks = ? WHERE claim_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, remarks);
            ps.setInt(2, claimId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Counts claims by status (PENDING, APPROVED, REJECTED).
     */
    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CLAIMS WHERE claim_status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Counts claims by student user and status.
     */
    public int countByUserAndStatus(int userId, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CLAIMS WHERE user_id = ? AND claim_status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Retrieves recent claims for dashboard display.
     */
    public List<Claim> findRecent(int limit) throws SQLException {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT c.claim_id, c.user_id, c.lost_item_id, c.found_item_id, c.claim_description, " +
                     "c.claim_date, c.claim_status, c.admin_remarks, " +
                     "u.name AS claimant_name, u.email AS claimant_email, u.register_number AS claimant_reg, " +
                     "COALESCE(l.item_name, f.item_name) AS target_item_name, " +
                     "COALESCE(l.category, f.category) AS target_item_category, " +
                     "CASE WHEN c.lost_item_id IS NOT NULL THEN 'LOST' ELSE 'FOUND' END AS target_item_type, " +
                     "COALESCE(l.lost_location, f.found_location) AS target_item_location, " +
                     "COALESCE(l.status, f.status) AS target_item_status " +
                     "FROM CLAIMS c " +
                     "JOIN USERS u ON c.user_id = u.user_id " +
                     "LEFT JOIN LOST_ITEMS l ON c.lost_item_id = l.lost_item_id " +
                     "LEFT JOIN FOUND_ITEMS f ON c.found_item_id = f.found_item_id " +
                     "ORDER BY c.claim_date DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithDetails(rs));
                }
            }
        }
        return list;
    }

    private Claim mapRowWithDetails(ResultSet rs) throws SQLException {
        Claim claim = new Claim(
                rs.getInt("claim_id"),
                rs.getInt("user_id"),
                rs.getObject("lost_item_id") != null ? rs.getInt("lost_item_id") : null,
                rs.getObject("found_item_id") != null ? rs.getInt("found_item_id") : null,
                rs.getString("claim_description"),
                rs.getTimestamp("claim_date"),
                rs.getString("claim_status"),
                rs.getString("admin_remarks")
        );
        claim.setClaimantName(rs.getString("claimant_name"));
        claim.setClaimantEmail(rs.getString("claimant_email"));
        claim.setClaimantRegisterNumber(rs.getString("claimant_reg"));
        claim.setTargetItemName(rs.getString("target_item_name"));
        claim.setTargetItemCategory(rs.getString("target_item_category"));
        claim.setTargetItemType(rs.getString("target_item_type"));
        claim.setTargetItemLocation(rs.getString("target_item_location"));
        claim.setTargetItemStatus(rs.getString("target_item_status"));
        return claim;
    }
}
