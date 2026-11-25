package model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model class representing a reported Found Item.
 */
public class FoundItem {
    private int foundItemId;
    private int userId;
    private String itemName;
    private String category;
    private String description;
    private String foundLocation;
    private Date foundDate;
    private String status; // "AVAILABLE", "CLAIMED", "CLOSED"
    private Timestamp createdAt;

    // Joined helper attributes
    private String reporterName;
    private String reporterEmail;
    private String reporterRegisterNumber;

    public FoundItem() {
    }

    public FoundItem(int foundItemId, int userId, String itemName, String category, String description,
                     String foundLocation, Date foundDate, String status, Timestamp createdAt) {
        this.foundItemId = foundItemId;
        this.userId = userId;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.foundLocation = foundLocation;
        this.foundDate = foundDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public FoundItem(int userId, String itemName, String category, String description,
                     String foundLocation, Date foundDate) {
        this.userId = userId;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.foundLocation = foundLocation;
        this.foundDate = foundDate;
        this.status = "AVAILABLE";
    }

    // Getters and Setters
    public int getFoundItemId() {
        return foundItemId;
    }

    public void setFoundItemId(int foundItemId) {
        this.foundItemId = foundItemId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFoundLocation() {
        return foundLocation;
    }

    public void setFoundLocation(String foundLocation) {
        this.foundLocation = foundLocation;
    }

    public Date getFoundDate() {
        return foundDate;
    }

    public void setFoundDate(Date foundDate) {
        this.foundDate = foundDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public String getReporterRegisterNumber() {
        return reporterRegisterNumber;
    }

    public void setReporterRegisterNumber(String reporterRegisterNumber) {
        this.reporterRegisterNumber = reporterRegisterNumber;
    }

    @Override
    public String toString() {
        return "FoundItem{" +
                "foundItemId=" + foundItemId +
                ", itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

