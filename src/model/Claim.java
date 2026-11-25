package model;

import java.sql.Timestamp;

/**
 * Model class representing a Claim submitted by a student for a lost or found item.
 */
public class Claim {
    private int claimId;
    private int userId;
    private Integer lostItemId;
    private Integer foundItemId;
    private String claimDescription;
    private Timestamp claimDate;
    private String claimStatus; // "PENDING", "APPROVED", "REJECTED"
    private String adminRemarks;

    // Joined presentation details
    private String claimantName;
    private String claimantEmail;
    private String claimantRegisterNumber;
    private String targetItemName;
    private String targetItemCategory;
    private String targetItemType; // "LOST" or "FOUND"
    private String targetItemLocation;
    private String targetItemStatus;

    public Claim() {
    }

    public Claim(int claimId, int userId, Integer lostItemId, Integer foundItemId,
                 String claimDescription, Timestamp claimDate, String claimStatus, String adminRemarks) {
        this.claimId = claimId;
        this.userId = userId;
        this.lostItemId = lostItemId;
        this.foundItemId = foundItemId;
        this.claimDescription = claimDescription;
        this.claimDate = claimDate;
        this.claimStatus = claimStatus;
        this.adminRemarks = adminRemarks;
    }

    public Claim(int userId, Integer lostItemId, Integer foundItemId, String claimDescription) {
        this.userId = userId;
        this.lostItemId = lostItemId;
        this.foundItemId = foundItemId;
        this.claimDescription = claimDescription;
        this.claimStatus = "PENDING";
    }

    // Getters and Setters
    public int getClaimId() {
        return claimId;
    }

    public void setClaimId(int claimId) {
        this.claimId = claimId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getLostItemId() {
        return lostItemId;
    }

    public void setLostItemId(Integer lostItemId) {
        this.lostItemId = lostItemId;
    }

    public Integer getFoundItemId() {
        return foundItemId;
    }

    public void setFoundItemId(Integer foundItemId) {
        this.foundItemId = foundItemId;
    }

    public String getClaimDescription() {
        return claimDescription;
    }

    public void setClaimDescription(String claimDescription) {
        this.claimDescription = claimDescription;
    }

    public Timestamp getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(Timestamp claimDate) {
        this.claimDate = claimDate;
    }

    public String getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(String claimStatus) {
        this.claimStatus = claimStatus;
    }

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }

    public String getClaimantEmail() {
        return claimantEmail;
    }

    public void setClaimantEmail(String claimantEmail) {
        this.claimantEmail = claimantEmail;
    }

    public String getClaimantRegisterNumber() {
        return claimantRegisterNumber;
    }

    public void setClaimantRegisterNumber(String claimantRegisterNumber) {
        this.claimantRegisterNumber = claimantRegisterNumber;
    }

    public String getTargetItemName() {
        return targetItemName;
    }

    public void setTargetItemName(String targetItemName) {
        this.targetItemName = targetItemName;
    }

    public String getTargetItemCategory() {
        return targetItemCategory;
    }

    public void setTargetItemCategory(String targetItemCategory) {
        this.targetItemCategory = targetItemCategory;
    }

    public String getTargetItemType() {
        return targetItemType;
    }

    public void setTargetItemType(String targetItemType) {
        this.targetItemType = targetItemType;
    }

    public String getTargetItemLocation() {
        return targetItemLocation;
    }

    public void setTargetItemLocation(String targetItemLocation) {
        this.targetItemLocation = targetItemLocation;
    }

    public String getTargetItemStatus() {
        return targetItemStatus;
    }

    public void setTargetItemStatus(String targetItemStatus) {
        this.targetItemStatus = targetItemStatus;
    }

    @Override
    public String toString() {
        return "Claim{" +
                "claimId=" + claimId +
                ", userId=" + userId +
                ", status='" + claimStatus + '\'' +
                '}';
    }
}

