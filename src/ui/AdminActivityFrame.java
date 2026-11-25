package ui;

import dao.ActivityLogDAO;
import model.ActivityLog;
import model.User;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * JFrame that displays the full administration activity history.
 * Data is loaded asynchronously via SwingWorker to keep the UI responsive.
 */
public class AdminActivityFrame extends JFrame {

    private final User adminUser;

    private DefaultTableModel tableModel;
    private JTable activityTable;
    private JLabel statusLabel;
    private JButton refreshButton;

    // Column headers for the activity table
    private static final String[] COLUMNS = {"#", "Admin", "Action", "Description", "Date & Time"};

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates and displays the Admin Activity History frame.
     *
     * @param adminUser the currently logged-in administrator (used for context)
     */
    public AdminActivityFrame(User adminUser) {
        this.adminUser = adminUser;
        initComponents();
        loadData();
    }

    // -------------------------------------------------------------------------
    // UI Construction
    // -------------------------------------------------------------------------

    private void initComponents() {
        setTitle("Admin Activity History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Root content panel
        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setBackground(UITheme.COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        setContentPane(contentPanel);

        // ---- Top bar -------------------------------------------------------
        contentPanel.add(buildTopBar(), BorderLayout.NORTH);

        // ---- Centre: scrollable table -------------------------------------
        contentPanel.add(buildTablePanel(), BorderLayout.CENTER);

        // ---- Bottom: status label -----------------------------------------
        contentPanel.add(buildStatusBar(), BorderLayout.SOUTH);
    }

    /** Builds the top bar containing the page title and the Refresh button. */
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.COLOR_BG);

        JLabel titleLabel = new JLabel("Administration Activity Log");
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.COLOR_PRIMARY);
        topBar.add(titleLabel, BorderLayout.WEST);

        refreshButton = UITheme.createSecondaryButton("⟳  Refresh");
        refreshButton.addActionListener(e -> loadData());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(UITheme.COLOR_BG);
        rightPanel.add(refreshButton);
        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }

    /** Builds the centre panel holding the JScrollPane + JTable. */
    private JScrollPane buildTablePanel() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        activityTable = new JTable(tableModel);
        UITheme.styleTable(activityTable);

        // Column width hints
        activityTable.getColumnModel().getColumn(0).setPreferredWidth(45);   // #
        activityTable.getColumnModel().getColumn(1).setPreferredWidth(130);  // Admin
        activityTable.getColumnModel().getColumn(2).setPreferredWidth(160);  // Action
        activityTable.getColumnModel().getColumn(3).setPreferredWidth(320);  // Description
        activityTable.getColumnModel().getColumn(4).setPreferredWidth(160);  // Date & Time

        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    /** Builds the bottom status bar showing the record count. */
    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusBar.setBackground(UITheme.COLOR_BG);

        statusLabel = new JLabel("Loading activity records…");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        statusBar.add(statusLabel);

        return statusBar;
    }

    // -------------------------------------------------------------------------
    // Data Loading
    // -------------------------------------------------------------------------

    /**
     * Loads activity log data from the database using a SwingWorker so that
     * the Event Dispatch Thread is never blocked by I/O.
     */
    private void loadData() {
        refreshButton.setEnabled(false);
        statusLabel.setText("Loading activity records…");
        tableModel.setRowCount(0);

        SwingWorker<List<ActivityLog>, Void> worker = new SwingWorker<List<ActivityLog>, Void>() {

            private int totalCount = 0;

            @Override
            protected List<ActivityLog> doInBackground() throws Exception {
                ActivityLogDAO dao = new ActivityLogDAO();
                totalCount = dao.countTotal();
                return dao.findRecent(100);
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                try {
                    List<ActivityLog> logs = get();

                    if (logs.isEmpty()) {
                        // Show a friendly "empty state" message in the table area
                        tableModel.setRowCount(0);
                        showEmptyState();
                        statusLabel.setText("No activity recorded yet.");
                    } else {
                        populateTable(logs);
                        statusLabel.setText("Showing " + logs.size() +
                                " activity record" + (logs.size() == 1 ? "" : "s") +
                                " (total: " + totalCount + ").");
                    }

                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(
                            AdminActivityFrame.this,
                            "Failed to load activity logs:\n" + cause.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    statusLabel.setText("Error loading records.");
                }
            }
        };

        worker.execute();
    }

    /** Fills the table model with the fetched {@link ActivityLog} list. */
    private void populateTable(List<ActivityLog> logs) {
        tableModel.setRowCount(0);
        for (ActivityLog log : logs) {
            tableModel.addRow(new Object[]{
                    log.getLogId(),
                    log.getAdminName() != null ? log.getAdminName() : "Unknown",
                    log.getActionType(),
                    log.getTargetDescription() != null ? log.getTargetDescription() : "—",
                    log.getPerformedAt() != null ? log.getPerformedAt().toString() : "—"
            });
        }
    }

    /**
     * When there are no records, overlay a centred "empty state" label
     * inside the scroll pane's viewport instead of a bare empty table.
     */
    private void showEmptyState() {
        JLabel emptyLabel = new JLabel("No activity recorded yet.", SwingConstants.CENTER);
        emptyLabel.setFont(UITheme.FONT_REGULAR);
        emptyLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        // Temporarily replace the scroll pane's view with the label
        Container parent = activityTable.getParent();
        if (parent instanceof JViewport) {
            ((JViewport) parent).setView(emptyLabel);
        }
    }
}
