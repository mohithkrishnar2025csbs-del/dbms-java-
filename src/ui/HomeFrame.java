package ui;

import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern Main Home Page for Smart Lost-and-Found Management System.
 * Serves as the primary entry point allowing users to choose between
 * the Student Portal and the Administrator Portal.
 */
public class HomeFrame extends JFrame {

    public HomeFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Smart Lost-and-Found Management System - Welcome");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 680);
        setMinimumSize(new Dimension(920, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.COLOR_BG);

        // ------------------------------------------------
        // 1. TOP HEADER SECTION
        // ------------------------------------------------
        JPanel header = new JPanel();
        header.setBackground(UITheme.COLOR_PRIMARY);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(26, 35, 26, 35));

        JLabel appTitle = new JLabel("SMART LOST-AND-FOUND MANAGEMENT SYSTEM");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appTitle.setForeground(Color.WHITE);
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("A Smart Campus Solution for Lost and Found Items.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(203, 213, 225));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcome = new JLabel("Welcome! Choose Your Portal to Continue.");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 15));
        welcome.setForeground(UITheme.COLOR_ACCENT);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(appTitle);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(10));
        header.add(welcome);

        // ------------------------------------------------
        // 2. PORTAL SELECTION CARDS CONTAINER
        // ------------------------------------------------
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(UITheme.COLOR_BG);
        centerContainer.setBorder(new EmptyBorder(25, 35, 25, 35));

        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 28, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(880, 480));

        cardsPanel.add(buildStudentCard());
        cardsPanel.add(buildAdminCard());

        centerContainer.add(cardsPanel);

        // ------------------------------------------------
        // 3. BOTTOM FOOTER BAR
        // ------------------------------------------------
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(UITheme.COLOR_BG);
        footer.setBorder(new EmptyBorder(0, 0, 15, 0));
        JLabel footerNote = new JLabel("Smart Campus Lost-and-Found System  •  Desktop Java Swing Application  •  College Academic Project");
        footerNote.setFont(UITheme.FONT_SMALL);
        footerNote.setForeground(UITheme.COLOR_TEXT_MUTED);
        footer.add(footerNote);

        root.add(header, BorderLayout.NORTH);
        root.add(centerContainer, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // -------------------------------------------------------------------------
    // A. STUDENT PORTAL CARD
    // -------------------------------------------------------------------------
    private JPanel buildStudentCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(0, 0, 24, 0)
        ));

        // Header Strip (Blue Theme)
        JPanel headerStrip = new JPanel();
        headerStrip.setBackground(UITheme.COLOR_PRIMARY);
        headerStrip.setLayout(new BoxLayout(headerStrip, BoxLayout.Y_AXIS));
        headerStrip.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel icon = new JLabel("🎓  STUDENT PORTAL");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        icon.setForeground(Color.WHITE);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tag = new JLabel("Campus Student Services");
        tag.setFont(UITheme.FONT_SMALL);
        tag.setForeground(UITheme.COLOR_ACCENT);
        tag.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerStrip.add(icon);
        headerStrip.add(Box.createVerticalStrut(4));
        headerStrip.add(tag);

        // Body Content
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 24, 0, 24));

        JLabel desc = new JLabel("<html><div style='line-height:1.4; color:#334155; font-size:12px;'>"
                + "Access student services to report lost items, report found items, "
                + "search belongings and track claim requests."
                + "</div></html>");
        desc.setFont(UITheme.FONT_REGULAR);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel featHeader = new JLabel("Available Features:");
        featHeader.setFont(UITheme.FONT_BOLD);
        featHeader.setForeground(UITheme.COLOR_PRIMARY);
        featHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel features = new JLabel("<html><div style='color:#475569; font-size:12px; line-height:1.7;'>"
                + "✔  Report Lost Items<br>"
                + "✔  Report Found Items<br>"
                + "✔  Search Items<br>"
                + "✔  Submit Claims<br>"
                + "✔  Track Claim Status<br>"
                + "✔  Manage Profile"
                + "</div></html>");
        features.setFont(UITheme.FONT_REGULAR);
        features.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnStudent = UITheme.createPrimaryButton("CONTINUE AS STUDENT");
        btnStudent.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnStudent.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnStudent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnStudent.addActionListener(e -> {
            dispose();
            new StudentLoginFrame().setVisible(true);
        });

        body.add(desc);
        body.add(Box.createVerticalStrut(14));
        body.add(featHeader);
        body.add(Box.createVerticalStrut(6));
        body.add(features);
        body.add(Box.createVerticalGlue());
        body.add(Box.createVerticalStrut(20));
        body.add(btnStudent);

        card.add(headerStrip, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // -------------------------------------------------------------------------
    // B. ADMIN PORTAL CARD
    // -------------------------------------------------------------------------
    private JPanel buildAdminCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(0, 0, 24, 0)
        ));

        // Header Strip (Dark Slate / Amber Accent Theme)
        JPanel headerStrip = new JPanel();
        headerStrip.setBackground(UITheme.COLOR_PRIMARY_DARK);
        headerStrip.setLayout(new BoxLayout(headerStrip, BoxLayout.Y_AXIS));
        headerStrip.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel icon = new JLabel("🛡️  ADMIN PORTAL");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        icon.setForeground(Color.WHITE);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tag = new JLabel("Campus Security & Administration");
        tag.setFont(UITheme.FONT_SMALL);
        tag.setForeground(UITheme.COLOR_ACCENT);
        tag.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerStrip.add(icon);
        headerStrip.add(Box.createVerticalStrut(4));
        headerStrip.add(tag);

        // Body Content
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 24, 0, 24));

        JLabel desc = new JLabel("<html><div style='line-height:1.4; color:#334155; font-size:12px;'>"
                + "Access administrative services to manage students, "
                + "monitor item reports and review claim requests."
                + "</div></html>");
        desc.setFont(UITheme.FONT_REGULAR);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel featHeader = new JLabel("Administrative Features:");
        featHeader.setFont(UITheme.FONT_BOLD);
        featHeader.setForeground(UITheme.COLOR_PRIMARY_DARK);
        featHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel features = new JLabel("<html><div style='color:#475569; font-size:12px; line-height:1.7;'>"
                + "✔  Admin Dashboard<br>"
                + "✔  Student Management<br>"
                + "✔  Lost Item Management<br>"
                + "✔  Found Item Management<br>"
                + "✔  Claim Management<br>"
                + "✔  Activity History"
                + "</div></html>");
        features.setFont(UITheme.FONT_REGULAR);
        features.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAdmin = UITheme.createAccentButton("CONTINUE AS ADMIN");
        btnAdmin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAdmin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnAdmin.addActionListener(e -> {
            dispose();
            new AdminLoginFrame().setVisible(true);
        });

        body.add(desc);
        body.add(Box.createVerticalStrut(14));
        body.add(featHeader);
        body.add(Box.createVerticalStrut(6));
        body.add(features);
        body.add(Box.createVerticalGlue());
        body.add(Box.createVerticalStrut(20));
        body.add(btnAdmin);

        card.add(headerStrip, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }
}
