package model;

import java.sql.Timestamp;

/**
 * Model class representing an entry in the ADMIN_ACTIVITY_LOG table.
 * Stores auditable records of administrative actions performed within the system.
 */
public class ActivityLog {

    private int logId;
    private int adminId;
    private String adminName;          // Joined from USERS table
    private String actionType;
    private String targetDescription;
    private String details;
    private Timestamp performedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public ActivityLog() {
    }

    public ActivityLog(int logId, int adminId, String adminName, String actionType,
                       String targetDescription, String details, Timestamp performedAt) {
        this.logId = logId;
        this.adminId = adminId;
        this.adminName = adminName;
        this.actionType = actionType;
        this.targetDescription = targetDescription;
        this.details = details;
        this.performedAt = performedAt;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTargetDescription() {
        return targetDescription;
    }

    public void setTargetDescription(String targetDescription) {
        this.targetDescription = targetDescription;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Timestamp getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(Timestamp performedAt) {
        this.performedAt = performedAt;
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "ActivityLog{" +
                "logId=" + logId +
                ", adminId=" + adminId +
                ", adminName='" + adminName + '\'' +
                ", actionType='" + actionType + '\'' +
                ", targetDescription='" + targetDescription + '\'' +
                ", details='" + details + '\'' +
                ", performedAt=" + performedAt +
                '}';
    }
}
