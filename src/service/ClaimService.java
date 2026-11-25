package service;

import dao.ClaimDAO;
import model.Claim;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class handling claim submission, validation, retrieval, and status verification.
 */
public class ClaimService {
    private final ClaimDAO claimDAO;

    public ClaimService() {
        this.claimDAO = new ClaimDAO();
    }

    public ClaimService(ClaimDAO claimDAO) {
        this.claimDAO = claimDAO;
    }

    /**
     * Submits a claim for an item with ownership details.
     */
    public boolean submitClaim(int userId, Integer lostItemId, Integer foundItemId, String claimDescription)
            throws SQLException, IllegalArgumentException {
        if ((lostItemId == null && foundItemId == null) || (lostItemId != null && foundItemId != null)) {
            throw new IllegalArgumentException("A claim must reference exactly one item (either lost or found).");
        }
        if (ValidationUtil.isEmpty(claimDescription) || claimDescription.trim().length() < 10) {
            throw new IllegalArgumentException("Please provide a detailed claim explanation and proof of ownership (minimum 10 characters).");
        }

        if (claimDAO.hasExistingClaim(userId, lostItemId, foundItemId)) {
            throw new IllegalArgumentException("You have already submitted an active or pending claim for this item.");
        }

        Claim claim = new Claim(userId, lostItemId, foundItemId, claimDescription.trim());
        return claimDAO.submitClaim(claim);
    }

    public List<Claim> getStudentClaims(int userId) throws SQLException {
        return claimDAO.findByUserId(userId);
    }

    public List<Claim> getAllClaims(String statusFilter) throws SQLException {
        return claimDAO.findAll(statusFilter);
    }

    public List<Claim> searchClaims(String statusFilter, String searchQuery) throws SQLException {
        return claimDAO.searchClaims(statusFilter, searchQuery);
    }

    public Claim getClaimDetails(int claimId) throws SQLException {
        return claimDAO.findById(claimId);
    }

    public boolean updateRemarks(int claimId, String remarks) throws SQLException, IllegalArgumentException {
        if (ValidationUtil.isEmpty(remarks)) {
            throw new IllegalArgumentException("Administrator remarks cannot be empty.");
        }
        return claimDAO.updateRemarks(claimId, remarks.trim());
    }

    public boolean approveClaim(int claimId, String remarks) throws SQLException, IllegalArgumentException {
        if (ValidationUtil.isEmpty(remarks)) {
            throw new IllegalArgumentException("Please provide administrator remarks for approving this claim.");
        }
        return claimDAO.approveClaim(claimId, remarks.trim());
    }

    public boolean rejectClaim(int claimId, String remarks) throws SQLException, IllegalArgumentException {
        if (ValidationUtil.isEmpty(remarks)) {
            throw new IllegalArgumentException("Please provide administrator remarks explaining the reason for rejection.");
        }
        return claimDAO.rejectClaim(claimId, remarks.trim());
    }
}
