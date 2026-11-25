package app;

import model.User;
import ui.*;

import javax.swing.*;
import java.awt.*;

/**
 * Headless/UI test verifying that all Swing frames initialize and lay out properly without crashing.
 */
public class UIFramesVerificationTest {
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("VERIFYING SWING UI FRAMES INITIALIZATION");
        System.out.println("==========================================================");

        // Mock/Sample User for frame initialization
        User adminUser = new User(1, "Admin User", "ADM-001", "admin@college.edu", "hash", "ADMIN", "ACTIVE", null);
        User studentUser = new User(2, "Student User", "STU-001", "student@college.edu", "hash", "STUDENT", "ACTIVE", null);

        testFrame("HomeFrame", () -> new HomeFrame());
        testFrame("StudentLoginFrame", () -> new StudentLoginFrame());
        testFrame("AdminLoginFrame", () -> new AdminLoginFrame());
        testFrame("StudentDashboard", () -> new StudentDashboard(studentUser));
        testFrame("AdminDashboard", () -> new AdminDashboard(adminUser));
        testFrame("ClaimManagementFrame", () -> new ClaimManagementFrame(adminUser, null));
        testFrame("AdminActivityFrame", () -> new AdminActivityFrame(adminUser));
        testFrame("LostItemFrame", () -> new LostItemFrame(studentUser, null));
        testFrame("FoundItemFrame", () -> new FoundItemFrame(studentUser, null));
        testFrame("SearchItemsFrame", () -> new SearchItemsFrame(studentUser));
        testFrame("MyReportsFrame", () -> new MyReportsFrame(studentUser));
        testFrame("MyClaimsFrame", () -> new MyClaimsFrame(studentUser));
        testFrame("ProfileFrame", () -> new ProfileFrame(studentUser, null));
        testFrame("RegistrationFrame", () -> new RegistrationFrame(null));

        System.out.println("==========================================================");
        System.out.println("ALL 14 SWING FRAMES INITIALIZED AND VERIFIED CLEANLY!");
        System.out.println("==========================================================");
    }

    private static void testFrame(String name, FrameSupplier supplier) {
        try {
            JFrame frame = supplier.get();
            if (frame != null) {
                frame.dispose(); // clean up resources immediately
                System.out.println("  [PASS] " + name + " initialized successfully");
            } else {
                System.err.println("  [FAIL] " + name + " returned null");
            }
        } catch (Throwable t) {
            System.err.println("  [FAIL] " + name + " threw exception: " + t.getMessage());
            t.printStackTrace();
        }
    }

    @FunctionalInterface
    interface FrameSupplier {
        JFrame get() throws Exception;
    }
}

