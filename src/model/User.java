package model;

import java.sql.Timestamp;

/**
 * Model class representing a User (Student or Administrator) in the system.
 */
public class User {
    private int userId;
    private String name;
    private String registerNumber;
    private String email;
    private String passwordHash;
    private String role; // "STUDENT" or "ADMIN"
    private String accountStatus; // "ACTIVE", "INACTIVE", "SUSPENDED"
    private Timestamp createdAt;

    public User() {
    }

    public User(int userId, String name, String registerNumber, String email, String passwordHash, String role, String accountStatus, Timestamp createdAt) {
        this.userId = userId;
        this.name = name;
        this.registerNumber = registerNumber;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    public User(String name, String registerNumber, String email, String passwordHash, String role) {
        this.name = name;
        this.registerNumber = registerNumber;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.accountStatus = "ACTIVE";
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", registerNumber='" + registerNumber + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                '}';
    }
}

