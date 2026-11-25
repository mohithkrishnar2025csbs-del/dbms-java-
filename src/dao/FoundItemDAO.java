package dao;

import model.FoundItem;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for FOUND_ITEMS table.
 */
public class FoundItemDAO {

    /**
     * Inserts a new found item record.
     */
    public boolean insert(FoundItem item) throws SQLException {
        String sql = "INSERT INTO FOUND_ITEMS (user_id, item_name, category, description, found_location, found_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getUserId());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getCategory());
            ps.setString(4, item.getDescription());
            ps.setString(5, item.getFoundLocation());
            ps.setDate(6, item.getFoundDate());
            ps.setString(7, item.getStatus() != null ? item.getStatus() : "AVAILABLE");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setFoundItemId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Retrieves a single found item with reporter information.
     */
    public FoundItem findById(int id) throws SQLException {
        String sql = "SELECT f.found_item_id, f.user_id, f.item_name, f.category, f.description, " +
                     "f.found_location, f.found_date, f.status, f.created_at, " +
                     "u.name AS reporter_name, u.email AS reporter_email, u.register_number AS reporter_reg " +
                     "FROM FOUND_ITEMS f " +
                     "JOIN USERS u ON f.user_id = u.user_id " +
                     "WHERE f.found_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowWithUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all found items reported by a specific user.
     */
    public List<FoundItem> findByUserId(int userId) throws SQLException {
        List<FoundItem> list = new ArrayList<>();
        String sql = "SELECT f.found_item_id, f.user_id, f.item_name, f.category, f.description, " +
                     "f.found_location, f.found_date, f.status, f.created_at, " +
                     "u.name AS reporter_name, u.email AS reporter_email, u.register_number AS reporter_reg " +
                     "FROM FOUND_ITEMS f " +
                     "JOIN USERS u ON f.user_id = u.user_id " +
                     "WHERE f.user_id = ? " +
                     "ORDER BY f.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithUser(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all found items.
     */
    public List<FoundItem> findAll() throws SQLException {
        List<FoundItem> list = new ArrayList<>();
        String sql = "SELECT f.found_item_id, f.user_id, f.item_name, f.category, f.description, " +
                     "f.found_location, f.found_date, f.status, f.created_at, " +
                     "u.name AS reporter_name, u.email AS reporter_email, u.register_number AS reporter_reg " +
                     "FROM FOUND_ITEMS f " +
                     "JOIN USERS u ON f.user_id = u.user_id " +
                     "ORDER BY f.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowWithUser(rs));
            }
        }
        return list;
    }

    /**
     * Dynamically filters found items using PreparedStatement.
     */
    public List<FoundItem> search(String keyword, String category, String location, Date date) throws SQLException {
        List<FoundItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT f.found_item_id, f.user_id, f.item_name, f.category, f.description, " +
                "f.found_location, f.found_date, f.status, f.created_at, " +
                "u.name AS reporter_name, u.email AS reporter_email, u.register_number AS reporter_reg " +
                "FROM FOUND_ITEMS f " +
                "JOIN USERS u ON f.user_id = u.user_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(f.item_name) LIKE ? OR LOWER(f.description) LIKE ?) ");
            String pat = "%" + keyword.toLowerCase().trim() + "%";
            params.add(pat);
            params.add(pat);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All Categories")) {
            sql.append("AND LOWER(f.category) = LOWER(?) ");
            params.add(category.trim());
        }

        if (location != null && !location.trim().isEmpty()) {
            sql.append("AND LOWER(f.found_location) LIKE ? ");
            params.add("%" + location.toLowerCase().trim() + "%");
        }

        if (date != null) {
            sql.append("AND f.found_date = ? ");
            params.add(date);
        }

        sql.append("ORDER BY f.created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithUser(rs));
                }
            }
        }
        return list;
    }

    /**
     * Updates status of a found item.
     */
    public boolean updateStatus(int foundItemId, String status) throws SQLException {
        String sql = "UPDATE FOUND_ITEMS SET status = ? WHERE found_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, foundItemId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a found item record.
     */
    public boolean delete(int foundItemId) throws SQLException {
        String sql = "DELETE FROM FOUND_ITEMS WHERE found_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, foundItemId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Counts total found items.
     */
    public int countTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM FOUND_ITEMS";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Counts found items reported by specific user.
     */
    public int countByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM FOUND_ITEMS WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Retrieves recent found items.
     */
    public List<FoundItem> findRecent(int limit) throws SQLException {
        List<FoundItem> list = new ArrayList<>();
        String sql = "SELECT f.found_item_id, f.user_id, f.item_name, f.category, f.description, " +
                     "f.found_location, f.found_date, f.status, f.created_at, " +
                     "u.name AS reporter_name, u.email AS reporter_email, u.register_number AS reporter_reg " +
                     "FROM FOUND_ITEMS f " +
                     "JOIN USERS u ON f.user_id = u.user_id " +
                     "ORDER BY f.created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowWithUser(rs));
                }
            }
        }
        return list;
    }

    private FoundItem mapRowWithUser(ResultSet rs) throws SQLException {
        FoundItem item = new FoundItem(
                rs.getInt("found_item_id"),
                rs.getInt("user_id"),
                rs.getString("item_name"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("found_location"),
                rs.getDate("found_date"),
                rs.getString("status"),
                rs.getTimestamp("created_at")
        );
        item.setReporterName(rs.getString("reporter_name"));
        item.setReporterEmail(rs.getString("reporter_email"));
        item.setReporterRegisterNumber(rs.getString("reporter_reg"));
        return item;
    }
}

