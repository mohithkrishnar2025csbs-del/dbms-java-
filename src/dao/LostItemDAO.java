package dao;

import model.LostItem;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for LOST_ITEMS table.
 */
public class LostItemDAO {

    /**
     * Inserts a new lost item record.
     */
    public boolean insert(LostItem item) throws SQLException {
        String sql = "INSERT INTO LOST_ITEMS (user_id, item_name, category, description, lost_location, lost_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getUserId());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getCategory());
            ps.setString(4, item.getDescription());
            ps.setString(5, item.getLostLocation());
            ps.setDate(6, item.getLostDate());
            ps.setString(7, item.getStatus() != null ? item.getStatus() : "OPEN");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setLostItemId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Retrieves a single lost item with reporter information.
     */
    public LostItem findById(int id) throws SQLException {
        String sql = "SELECT l.lost_item_id, l.user_id, l.item_name, l.category, l.description, " +
                     "l.lost_location, l.lost_date, l.status, l.created_at, " +
                     "u.name AS reporter_name, u.email AS reporter_email, u.register_number AS reporter_reg " +
                     "FROM LOST_ITEMS l " +
                     "JOIN USERS u ON l.user_id = u.user_id " +
                     "WHERE l.lost_item_id = ?";
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
     * Retrieves all lost items reported by a specific user.
     */
    public List<LostItem> findByUserId(int userId) throws SQLException {
        List<LostItem> list = new ArrayList<>();
        String sql = "SELECT l.lost_item_id, l.user_id, l.item_name, l.category, l.description, " +
                     "l.lost_location, l.lost_date, l.status, l.created_at, " +
                     "u.name AS reporter_name, u.email AS reporter_email, u.register_number AS reporter_reg " +
                     "FROM LOST_ITEMS l " +
                     "JOIN USERS u ON l.user_id = u.user_id " +
                     "WHERE l.user_id = ? " +
                     "ORDER BY l.created_at DESC";
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
     * Retrieves all lost items for admin or general browsing.
     */
    public List<LostItem> findAll() throws SQLException {
        List<LostItem> list = new ArrayList<>();
        String sql = "SELECT l.lost_item_id, l.user_id, l.item_name, l.category, l.description, " +
                     "l.lost_location, l.lost_date, l.status, l.created_at, " +
                     "u.name AS reporter_name, u.email AS reporter_email, u.register_number AS reporter_reg " +
                     "FROM LOST_ITEMS l " +
                     "JOIN USERS u ON l.user_id = u.user_id " +
                     "ORDER BY l.created_at DESC";
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
     * Dynamically filters lost items using PreparedStatement.
     */
    public List<LostItem> search(String keyword, String category, String location, Date date) throws SQLException {
        List<LostItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT l.lost_item_id, l.user_id, l.item_name, l.category, l.description, " +
                "l.lost_location, l.lost_date, l.status, l.created_at, " +
                "u.name AS reporter_name, u.email AS reporter_email, u.register_number AS reporter_reg " +
                "FROM LOST_ITEMS l " +
                "JOIN USERS u ON l.user_id = u.user_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(l.item_name) LIKE ? OR LOWER(l.description) LIKE ?) ");
            String pat = "%" + keyword.toLowerCase().trim() + "%";
            params.add(pat);
            params.add(pat);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All Categories")) {
            sql.append("AND LOWER(l.category) = LOWER(?) ");
            params.add(category.trim());
        }

        if (location != null && !location.trim().isEmpty()) {
            sql.append("AND LOWER(l.lost_location) LIKE ? ");
            params.add("%" + location.toLowerCase().trim() + "%");
        }

        if (date != null) {
            sql.append("AND l.lost_date = ? ");
            params.add(date);
        }

        sql.append("ORDER BY l.created_at DESC");

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
     * Updates status of a lost item.
     */
    public boolean updateStatus(int lostItemId, String status) throws SQLException {
        String sql = "UPDATE LOST_ITEMS SET status = ? WHERE lost_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, lostItemId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a lost item record.
     */
    public boolean delete(int lostItemId) throws SQLException {
        String sql = "DELETE FROM LOST_ITEMS WHERE lost_item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lostItemId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Counts total lost items.
     */
    public int countTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM LOST_ITEMS";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Counts lost items reported by specific user.
     */
    public int countByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LOST_ITEMS WHERE user_id = ?";
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
     * Retrieves recent lost items.
     */
    public List<LostItem> findRecent(int limit) throws SQLException {
        List<LostItem> list = new ArrayList<>();
        String sql = "SELECT l.lost_item_id, l.user_id, l.item_name, l.category, l.description, " +
                     "l.lost_location, l.lost_date, l.status, l.created_at, " +
                     "u.name AS reporter_name, u.email AS reporter_email, u.register_number AS reporter_reg " +
                     "FROM LOST_ITEMS l " +
                     "JOIN USERS u ON l.user_id = u.user_id " +
                     "ORDER BY l.created_at DESC LIMIT ?";
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

    private LostItem mapRowWithUser(ResultSet rs) throws SQLException {
        LostItem item = new LostItem(
                rs.getInt("lost_item_id"),
                rs.getInt("user_id"),
                rs.getString("item_name"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("lost_location"),
                rs.getDate("lost_date"),
                rs.getString("status"),
                rs.getTimestamp("created_at")
        );
        item.setReporterName(rs.getString("reporter_name"));
        item.setReporterEmail(rs.getString("reporter_email"));
        item.setReporterRegisterNumber(rs.getString("reporter_reg"));
        return item;
    }
}

