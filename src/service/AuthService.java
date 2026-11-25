package service;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;
import util.ValidationUtil;

import java.sql.SQLException;

/**
 * Service class handling authentication, registration, session management, and profile operations.
 */
public class AuthService {
    private final UserDAO userDAO;
    private static User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user by email and password.
     * @return User object on success
     * @throws IllegalArgumentException on invalid input or credentials
     * @throws SQLException on database error
     */
    public User login(String email, String password) throws SQLException, IllegalArgumentException {
        if (ValidationUtil.isEmpty(email) || ValidationUtil.isEmpty(password)) {
            throw new IllegalArgumentException("Email and password cannot be empty.");
        }

        User user = userDAO.findByEmail(email.trim());
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getAccountStatus())) {
            throw new IllegalArgumentException("Your account is currently " + user.getAccountStatus().toLowerCase() + ". Please contact administrator.");
        }

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        currentUser = user;
        return user;
    }

    /**
     * Registers a new student account with validation and PBKDF2 password hashing.
     */
    public boolean registerStudent(String name, String regNo, String email, String password, String confirmPassword)
            throws SQLException, IllegalArgumentException {
        if (!ValidationUtil.isValidName(name)) {
            throw new IllegalArgumentException("Please enter a valid full name (letters and spaces only, 2-100 characters).");
        }
        if (!ValidationUtil.isValidRegisterNumber(regNo)) {
            throw new IllegalArgumentException("Please enter a valid register number (3-30 characters).");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters in length.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        if (userDAO.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }
        if (userDAO.existsByRegisterNumber(regNo)) {
            throw new IllegalArgumentException("An account with this register number already exists.");
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        User student = new User(name.trim(), regNo.trim().toUpperCase(), email.trim().toLowerCase(), hashedPassword, "STUDENT");

        return userDAO.register(student);
    }

    /**
     * Updates student's profile information.
     */
    public boolean updateProfile(int userId, String name, String email) throws SQLException, IllegalArgumentException {
        if (!ValidationUtil.isValidName(name)) {
            throw new IllegalArgumentException("Please enter a valid full name.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        User existing = userDAO.findByEmail(email);
        if (existing != null && existing.getUserId() != userId) {
            throw new IllegalArgumentException("The email address is already in use by another account.");
        }

        boolean updated = userDAO.updateProfile(userId, name, email);
        if (updated && currentUser != null && currentUser.getUserId() == userId) {
            currentUser.setName(name);
            currentUser.setEmail(email);
        }
        return updated;
    }

    /**
     * Changes user password verifying the old password.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword, String confirmNewPassword)
            throws SQLException, IllegalArgumentException {
        if (ValidationUtil.isEmpty(oldPassword) || ValidationUtil.isEmpty(newPassword)) {
            throw new IllegalArgumentException("Passwords cannot be empty.");
        }
        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("New password must be at least 6 characters in length.");
        }
        if (!newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException("New passwords do not match.");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect current password.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, newHash);
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }
}

