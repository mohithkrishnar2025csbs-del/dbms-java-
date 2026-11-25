package service;

import dao.FoundItemDAO;
import dao.LostItemDAO;
import model.FoundItem;
import model.LostItem;
import util.ValidationUtil;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class handling reporting, searching, filtering, and status management for lost and found items.
 */
public class ItemService {
    private final LostItemDAO lostItemDAO;
    private final FoundItemDAO foundItemDAO;

    public ItemService() {
        this.lostItemDAO = new LostItemDAO();
        this.foundItemDAO = new FoundItemDAO();
    }

    public ItemService(LostItemDAO lostItemDAO, FoundItemDAO foundItemDAO) {
        this.lostItemDAO = lostItemDAO;
        this.foundItemDAO = foundItemDAO;
    }

    /**
     * Reports a lost item.
     */
    public boolean reportLostItem(int userId, String itemName, String category, String description,
                                  String location, String dateStr) throws SQLException, IllegalArgumentException {
        if (ValidationUtil.isEmpty(itemName)) {
            throw new IllegalArgumentException("Item name is required.");
        }
        if (ValidationUtil.isEmpty(category) || category.equalsIgnoreCase("Select Category")) {
            throw new IllegalArgumentException("Please select a valid category.");
        }
        if (ValidationUtil.isEmpty(location)) {
            throw new IllegalArgumentException("Lost location is required.");
        }
        Date date = ValidationUtil.parseSqlDate(dateStr);
        if (date == null) {
            throw new IllegalArgumentException("Please enter a valid date in YYYY-MM-DD format.");
        }
        if (!ValidationUtil.isNotFutureDate(date)) {
            throw new IllegalArgumentException("Lost date cannot be in the future.");
        }

        LostItem item = new LostItem(userId, itemName.trim(), category.trim(),
                description != null ? description.trim() : "", location.trim(), date);
        return lostItemDAO.insert(item);
    }

    /**
     * Reports a found item.
     */
    public boolean reportFoundItem(int userId, String itemName, String category, String description,
                                   String location, String dateStr) throws SQLException, IllegalArgumentException {
        if (ValidationUtil.isEmpty(itemName)) {
            throw new IllegalArgumentException("Item name is required.");
        }
        if (ValidationUtil.isEmpty(category) || category.equalsIgnoreCase("Select Category")) {
            throw new IllegalArgumentException("Please select a valid category.");
        }
        if (ValidationUtil.isEmpty(location)) {
            throw new IllegalArgumentException("Found location is required.");
        }
        Date date = ValidationUtil.parseSqlDate(dateStr);
        if (date == null) {
            throw new IllegalArgumentException("Please enter a valid date in YYYY-MM-DD format.");
        }
        if (!ValidationUtil.isNotFutureDate(date)) {
            throw new IllegalArgumentException("Found date cannot be in the future.");
        }

        FoundItem item = new FoundItem(userId, itemName.trim(), category.trim(),
                description != null ? description.trim() : "", location.trim(), date);
        return foundItemDAO.insert(item);
    }

    public LostItem getLostItem(int id) throws SQLException {
        return lostItemDAO.findById(id);
    }

    public FoundItem getFoundItem(int id) throws SQLException {
        return foundItemDAO.findById(id);
    }

    public List<LostItem> getStudentLostReports(int userId) throws SQLException {
        return lostItemDAO.findByUserId(userId);
    }

    public List<FoundItem> getStudentFoundReports(int userId) throws SQLException {
        return foundItemDAO.findByUserId(userId);
    }

    public List<LostItem> getAllLostReports() throws SQLException {
        return lostItemDAO.findAll();
    }

    public List<FoundItem> getAllFoundReports() throws SQLException {
        return foundItemDAO.findAll();
    }

    public List<LostItem> searchLostItems(String keyword, String category, String location, String dateStr) throws SQLException {
        Date date = ValidationUtil.parseSqlDate(dateStr);
        return lostItemDAO.search(keyword, category, location, date);
    }

    public List<FoundItem> searchFoundItems(String keyword, String category, String location, String dateStr) throws SQLException {
        Date date = ValidationUtil.parseSqlDate(dateStr);
        return foundItemDAO.search(keyword, category, location, date);
    }

    public boolean updateLostStatus(int id, String status) throws SQLException {
        return lostItemDAO.updateStatus(id, status);
    }

    public boolean updateFoundStatus(int id, String status) throws SQLException {
        return foundItemDAO.updateStatus(id, status);
    }

    public boolean deleteLostItem(int id) throws SQLException {
        return lostItemDAO.delete(id);
    }

    public boolean deleteFoundItem(int id) throws SQLException {
        return foundItemDAO.delete(id);
    }
}

