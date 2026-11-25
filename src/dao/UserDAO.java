package dao;

import model.User;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for USERS table.
 */
public class UserDAO {

    /**
     * Registers a new user.
     */
    public boolean register(User user) throws SQLException {
        String sql = "INSERT INTO USERS (name, register_number, email, password_hash, role, account_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getRegisterNumber());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole());
            ps.setString(6, user.getAccountStatus() != null ? user.getAccountStatus() : "ACTIVE");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Finds a user by email (case-insensitive, with domain alias resilience).
     */
    public User findByEmail(String email) throws SQLException {
        String target = email.trim().toLowerCase();
        String alias = target;
        if (target.equals("mohithkrishna.r.2025.csbs@rajalakshgmi.edu.in")) {
            alias = "mohithkrishna.r.2025.csbs@rajalakshmi.edu.in";
        } else if (target.equals("mohithkrishna.r.2025.csbs@rajalakshmi.edu.in")) {
            alias = "mohithkrishna.r.2025.csbs@rajalakshgmi.edu.in";
        }

        String sql = "SELECT user_id, name, register_number, email, password_hash, role, account_status, created_at " +
                     "FROM USERS WHERE LOWER(email) = ? OR LOWER(email) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, target);
            ps.setString(2, alias);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds a user by ID.
     */
    public User findById(int userId) throws SQLException {
        String sql = "SELECT user_id, name, register_number, email, password_hash, role, account_status, created_at " +
                     "FROM USERS WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Checks if email is already registered.
     */
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM USERS WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Checks if register number is already registered.
     */
    public boolean existsByRegisterNumber(String regNo) throws SQLException {
        String sql = "SELECT 1 FROM USERS WHERE LOWER(register_number) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Updates student profile (name and email).
     */
    public boolean updateProfile(int userId, String name, String email) throws SQLException {
        String sql = "UPDATE USERS SET name = ?, email = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, email.trim());
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates user password hash.
     */
    public boolean updatePassword(int userId, String passwordHash) throws SQLException {
        String sql = "UPDATE USERS SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates account status (e.g. ACTIVE, SUSPENDED, INACTIVE).
     */
    public boolean updateAccountStatus(int userId, String status) throws SQLException {
        String sql = "UPDATE USERS SET account_status = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all student users.
     */
    public List<User> findAllStudents() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, name, register_number, email, password_hash, role, account_status, created_at " +
                     "FROM USERS WHERE role = 'STUDENT' ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Searches students by name, register number, or email with optional status filter.
     */
    public List<User> searchStudents(String keyword, String statusFilter) throws SQLException {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT user_id, name, register_number, email, password_hash, role, account_status, created_at " +
                "FROM USERS WHERE role = 'STUDENT' "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(register_number) LIKE ? OR LOWER(email) LIKE ?) ");
            String pattern = "%" + keyword.toLowerCase().trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND account_status = ? ");
            params.add(statusFilter.trim());
        }

        sql.append("ORDER BY name ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Searches students by keyword (name, register number, or email).
     */
    public List<User> searchStudents(String keyword) throws SQLException {
        return searchStudents(keyword, "ALL");
    }

    /**
     * Counts total registered students.
     */
    public int countTotalStudents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM USERS WHERE role = 'STUDENT'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Helper to map a ResultSet row to User entity.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("register_number"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getString("account_status"),
                rs.getTimestamp("created_at")
        );
    }
}
