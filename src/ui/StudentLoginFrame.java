package ui;

import model.User;
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
 * Student Portal Sign-In frame with portal-specific authentication and role verification.
 */
public class StudentLoginFrame extends JFrame {

    private final AuthService authService;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheck;
    private JButton loginButton;
    private JLabel statusLabel;

    public StudentLoginFrame() {
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Student Portal - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 600);
        setMinimumSize(new Dimension(840, 540));
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // ------------------------------------------------
        // LEFT BRANDING PANEL
        // ------------------------------------------------
        JPanel brandPanel = new JPanel();
        brandPanel.setBackground(UITheme.COLOR_PRIMARY);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(50, 45, 50, 45));

        JLabel collegeLabel = new JLabel("CAMPUS SERVICES");
        collegeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        collegeLabel.setForeground(UITheme.COLOR_ACCENT);

        JLabel portalBadge = new JLabel("[ 🎓  STUDENT PORTAL ]");
        portalBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        portalBadge.setForeground(UITheme.COLOR_ACCENT);

        JLabel appTitle = new JLabel("<html>Smart<br>Lost &amp; Found<br>System</html>");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        appTitle.setForeground(Color.WHITE);

        JLabel appSubtitle = new JLabel("<html>A centralized academic desktop platform to report, track, match, and claim campus belongings securely.</html>");
        appSubtitle.setFont(UITheme.FONT_REGULAR);
        appSubtitle.setForeground(new Color(203, 213, 225));

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        badgePanel.setOpaque(false);
        JLabel badge = new JLabel("• Report Items  • Track Claims  • Manage Profile");
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
                new EmptyBorder(30, 38, 30, 38)
        ));
        formCard.setPreferredSize(new Dimension(390, 510));

        JLabel signInLabel = new JLabel("Sign In as Student");
        signInLabel.setFont(UITheme.FONT_HEADER);
        signInLabel.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel signInSub = new JLabel("Access your student lost-and-found dashboard");
        signInSub.setFont(UITheme.FONT_REGULAR);
        signInSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        JLabel emailLbl = new JLabel("Student Campus Email");
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

        loginButton = UITheme.createPrimaryButton("Sign In as Student");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.addActionListener(e -> performLogin());

        JButton registerButton = UITheme.createSecondaryButton("Register New Student Account");
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        registerButton.addActionListener(e -> {
            new RegistrationFrame(this).setVisible(true);
            setVisible(false);
        });

        JButton btnBackHome = UITheme.createSecondaryButton("Back to Home");
        btnBackHome.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBackHome.addActionListener(e -> {
            dispose();
            new HomeFrame().setVisible(true);
        });

        JLabel switchAdmin = new JLabel("<html><u>Switch to Admin Portal</u></html>");
        switchAdmin.setFont(UITheme.FONT_SMALL);
        switchAdmin.setForeground(UITheme.COLOR_ACCENT);
        switchAdmin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        switchAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);
        switchAdmin.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                new AdminLoginFrame().setVisible(true);
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
        formCard.add(Box.createVerticalStrut(16));
        formCard.add(emailLbl);
        formCard.add(Box.createVerticalStrut(5));
        formCard.add(emailField);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(passLbl);
        formCard.add(Box.createVerticalStrut(5));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(5));
        formCard.add(showPasswordCheck);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(statusLabel);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(loginButton);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(registerButton);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(btnBackHome);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(switchAdmin);

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
                loginButton.setText("Sign In as Student");
                try {
                    User user = get();
                    if (user != null) {
                        if (user.isAdmin()) {
                            statusLabel.setText("This account belongs to the Admin Portal. Please select Admin Portal.");
                            return;
                        }
                        dispose();
                        new StudentDashboard(user).setVisible(true);
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
