package model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model class representing a reported Lost Item.
 */
public class LostItem {
    private int lostItemId;
    private int userId;
    private String itemName;
    private String category;
    private String description;
    private String lostLocation;
    private Date lostDate;
    private String status; // "OPEN", "CLAIMED", "CLOSED"
    private Timestamp createdAt;

    // Joined helper attributes
    private String reporterName;
    private String reporterEmail;
    private String reporterRegisterNumber;

    public LostItem() {
    }

    public LostItem(int lostItemId, int userId, String itemName, String category, String description,
                    String lostLocation, Date lostDate, String status, Timestamp createdAt) {
        this.lostItemId = lostItemId;
        this.userId = userId;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.lostLocation = lostLocation;
        this.lostDate = lostDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public LostItem(int userId, String itemName, String category, String description,
                    String lostLocation, Date lostDate) {
        this.userId = userId;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.lostLocation = lostLocation;
        this.lostDate = lostDate;
        this.status = "OPEN";
    }

    // Getters and Setters
    public int getLostItemId() {
        return lostItemId;
    }

    public void setLostItemId(int lostItemId) {
        this.lostItemId = lostItemId;
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

    public String getLostLocation() {
        return lostLocation;
    }

    public void setLostLocation(String lostLocation) {
        this.lostLocation = lostLocation;
    }

    public Date getLostDate() {
        return lostDate;
    }

    public void setLostDate(Date lostDate) {
        this.lostDate = lostDate;
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
        return "LostItem{" +
                "lostItemId=" + lostItemId +
                ", itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

