package app;

import ui.HomeFrame;
import util.DBConnection;

import javax.swing.*;

/**
 * Main application launcher for Smart Lost-and-Found Management System.
 * Initializes desktop look-and-feel, checks database connectivity,
 * and presents the Main Home Page (HomeFrame) for portal selection.
 */
public class Main {
    public static void main(String[] args) {
        // Set modern desktop look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignoredToo) {
            }
        }

        // Check database connectivity in background
        boolean connected = DBConnection.testConnection();
        if (!connected) {
            System.err.println("[INFO] Could not connect to MySQL database at startup.");
            System.err.println("[INFO] Please verify MySQL is running and check 'config/db.properties'.");
        } else {
            System.out.println("[SUCCESS] Successfully connected to MySQL database: " + DBConnection.getDatabaseName());
        }

        // Launch Home Page on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            HomeFrame homeFrame = new HomeFrame();
            homeFrame.setVisible(true);

            if (!connected) {
                JOptionPane.showMessageDialog(
                        homeFrame,
                        "Note: Database connection to 'smart_lost_found' could not be established.\n\n" +
                        "1. Ensure MySQL Server 8.4 is running on localhost:3306.\n" +
                        "2. Create the schema by executing 'database/schema.sql'.\n" +
                        "3. Update your credentials in 'config/db.properties'.",
                        "Database Setup Notice",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });
    }
}
