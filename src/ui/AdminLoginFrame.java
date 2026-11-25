package ui;

import model.User;
import service.AdminService;
import service.AuthService;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Admin Portal Sign-In frame with administrator-only authentication and role verification.
 */
public class AdminLoginFrame extends JFrame {

    private final AuthService authService;
    private final AdminService adminService;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheck;
    private JButton loginButton;
    private JLabel statusLabel;

    public AdminLoginFrame() {
        this.authService = new AuthService();
        this.adminService = new AdminService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Admin Portal - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 600);
        setMinimumSize(new Dimension(840, 540));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // ------------------------------------------------
        // LEFT BRANDING PANEL
        // ------------------------------------------------
        JPanel brandPanel = new JPanel();
        brandPanel.setBackground(UITheme.COLOR_PRIMARY_DARK);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(50, 45, 50, 45));

        JLabel collegeLabel = new JLabel("CAMPUS SERVICES");
        collegeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        collegeLabel.setForeground(UITheme.COLOR_ACCENT);

        JLabel portalBadge = new JLabel("[ 🛡️  ADMIN PORTAL ]");
        portalBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        portalBadge.setForeground(UITheme.COLOR_ACCENT);

        JLabel appTitle = new JLabel("<html>Smart<br>Lost &amp; Found<br>System</html>");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        appTitle.setForeground(Color.WHITE);

        JLabel appSubtitle = new JLabel("<html>Secure administrative access to manage registered students, monitor campus item reports, and review claim verification requests.</html>");
        appSubtitle.setFont(UITheme.FONT_REGULAR);
        appSubtitle.setForeground(new Color(203, 213, 225));

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        badgePanel.setOpaque(false);
        JLabel badge = new JLabel("• Role-Based Access  • Secure PBKDF2  • Admin Only");
        badge.setFont(UITheme.FONT_SMALL);
        badge.setForeground(new Color(148, 163, 184));
        badgePanel.add(badge);

        brandPanel.add(collegeLabel);
        brandPanel.add(Box.createVerticalStrut(8));
        brandPanel.add(portalBadge);
        brandPanel.add(Box.createVerticalStrut(12));
        brandPanel.add(appTitle);
        brandPanel.add(Box.createVerticalStrut(18));
        brandPanel.add(appSubtitle);
        brandPanel.add(Box.createVerticalGlue());
        brandPanel.add(badgePanel);

        // ------------------------------------------------
        // RIGHT FORM PANEL
        // ------------------------------------------------
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(UITheme.COLOR_BG);

        JPanel formCard = new JPanel();
        formCard.setBackground(Color.WHITE);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(32, 38, 32, 38)
        ));
        formCard.setPreferredSize(new Dimension(390, 480));

        JLabel signInLabel = new JLabel("Admin Sign In");
        signInLabel.setFont(UITheme.FONT_HEADER);
        signInLabel.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel signInSub = new JLabel("Restricted to authorized campus administrators");
        signInSub.setFont(UITheme.FONT_REGULAR);
        signInSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        JLabel emailLbl = new JLabel("Admin Email Address");
        emailLbl.setFont(UITheme.FONT_BOLD);
        emailLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        emailField = UITheme.createTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(UITheme.FONT_BOLD);
        passLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        passwordField = UITheme.createPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        showPasswordCheck = new JCheckBox("Show Password");
        showPasswordCheck.setFont(UITheme.FONT_SMALL);
        showPasswordCheck.setForeground(UITheme.COLOR_TEXT_MUTED);
        showPasswordCheck.setBackground(Color.WHITE);
        showPasswordCheck.setFocusPainted(false);
        showPasswordCheck.addActionListener(e -> {
            if (showPasswordCheck.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('*');
            }
        });

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.COLOR_DANGER);

        loginButton = UITheme.createAccentButton("Sign In as Admin");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.addActionListener(e -> performLogin());

        JButton btnBackHome = UITheme.createSecondaryButton("Back to Home");
        btnBackHome.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBackHome.addActionListener(e -> {
            dispose();
            new HomeFrame().setVisible(true);
        });

        JLabel switchStudent = new JLabel("<html><u>Switch to Student Portal</u></html>");
        switchStudent.setFont(UITheme.FONT_SMALL);
        switchStudent.setForeground(UITheme.COLOR_PRIMARY);
        switchStudent.setCursor(new Cursor(Cursor.HAND_CURSOR));
        switchStudent.setAlignmentX(Component.CENTER_ALIGNMENT);
        switchStudent.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                new StudentLoginFrame().setVisible(true);
            }
        });

        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        };
        emailField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        formCard.add(signInLabel);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(signInSub);
        formCard.add(Box.createVerticalStrut(18));
        formCard.add(emailLbl);
        formCard.add(Box.createVerticalStrut(5));
        formCard.add(emailField);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(passLbl);
        formCard.add(Box.createVerticalStrut(5));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(showPasswordCheck);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(statusLabel);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(loginButton);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(btnBackHome);
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(switchStudent);

        formContainer.add(formCard);

        mainPanel.add(brandPanel);
        mainPanel.add(formContainer);

        setContentPane(mainPanel);
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        statusLabel.setText(" ");

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both email and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(email, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In as Admin");
                try {
                    User user = get();
                    if (user != null) {
                        if (!user.isAdmin()) {
                            statusLabel.setText("Access Denied: This account does not have administrator privileges.");
                            return;
                        }
                        // Log admin login activity
                        adminService.logActivity(user.getUserId(), "ADMIN_LOGIN",
                                "Admin logged in: " + user.getName(),
                                "Authenticated from Admin Portal");

                        dispose();
                        new AdminDashboard(user).setVisible(true);
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setText(cause.getMessage());
                }
            }
        };
        worker.execute();
    }
}
