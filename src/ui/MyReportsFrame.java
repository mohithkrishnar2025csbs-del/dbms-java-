package ui;

import model.FoundItem;
import model.LostItem;
import model.User;
import service.ItemService;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Screen displaying reports submitted by the logged-in student (Lost & Found).
 */
public class MyReportsFrame extends JFrame {
    private final User currentUser;
    private final ItemService itemService;

    private JTable lostTable;
    private DefaultTableModel lostModel;
    private JTable foundTable;
    private DefaultTableModel foundModel;

    public MyReportsFrame(User currentUser) {
        this.currentUser = currentUser;
        this.itemService = new ItemService();
        initComponents();
        loadReports();
    }

    private void initComponents() {
        setTitle("My Submitted Reports - Smart Lost-and-Found Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(920, 620);
        setMinimumSize(new Dimension(800, 520));
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBackground(UITheme.COLOR_BG);
        contentPane.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLbl = new JLabel("My Submitted Reports");
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.COLOR_PRIMARY);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Data");
        refreshBtn.addActionListener(e -> loadReports());

        header.add(titleLbl, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);

        // Tabbed Pane for Lost vs Found
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        // Tab 1: Lost Items
        String[] lostCols = {"ID", "Item Name", "Category", "Location", "Lost Date", "Status"};
        lostModel = new DefaultTableModel(lostCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        lostTable = new JTable(lostModel);
        UITheme.styleTable(lostTable);
        JScrollPane lostScroll = new JScrollPane(lostTable);
        lostScroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1));

        // Tab 2: Found Items
        String[] foundCols = {"ID", "Item Name", "Category", "Location", "Found Date", "Status"};
        foundModel = new DefaultTableModel(foundCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        foundTable = new JTable(foundModel);
        UITheme.styleTable(foundTable);
        JScrollPane foundScroll = new JScrollPane(foundTable);
        foundScroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1));

        tabbedPane.addTab("My Lost Items", lostScroll);
        tabbedPane.addTab("My Found Items", foundScroll);

        contentPane.add(header, BorderLayout.NORTH);
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(contentPane);
    }

    public void loadReports() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<LostItem> lostItems;
            private List<FoundItem> foundItems;

            @Override
            protected Void doInBackground() throws Exception {
                lostItems = itemService.getStudentLostReports(currentUser.getUserId());
                foundItems = itemService.getStudentFoundReports(currentUser.getUserId());
                return null;
            }

            @Override
            protected void done() {
                lostModel.setRowCount(0);
                if (lostItems != null) {
                    for (LostItem l : lostItems) {
                        lostModel.addRow(new Object[]{
                                l.getLostItemId(),
                                l.getItemName(),
                                l.getCategory(),
                                l.getLostLocation(),
                                l.getLostDate(),
                                l.getStatus()
                        });
                    }
                }

                foundModel.setRowCount(0);
                if (foundItems != null) {
                    for (FoundItem f : foundItems) {
                        foundModel.addRow(new Object[]{
                                f.getFoundItemId(),
                                f.getItemName(),
                                f.getCategory(),
                                f.getFoundLocation(),
                                f.getFoundDate(),
                                f.getStatus()
                        });
                    }
                }
            }
        };
        worker.execute();
    }
}

