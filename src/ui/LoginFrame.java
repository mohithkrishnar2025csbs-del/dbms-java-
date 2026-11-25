package ui;

import model.User;
import service.AuthService;
import util.DBConnection;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern Login Screen with role-based routing (Student vs Administrator).
 */
public class LoginFrame extends JFrame {
    private final AuthService authService;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox showPasswordCheck;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;

    public LoginFrame() {
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Smart Lost-and-Found Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 580);
        setMinimumSize(new Dimension(840, 520));
        setLocationRelativeTo(null);

        // Main Layout: Split into Left Brand Panel and Right Login Panel
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // ----------------------------------------------------
        // Left Branding Panel
        // ----------------------------------------------------
        JPanel brandPanel = new JPanel();
        brandPanel.setBackground(UITheme.COLOR_PRIMARY);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(50, 45, 50, 45));

        JLabel collegeLabel = new JLabel("CAMPUS SERVICES");
        collegeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        collegeLabel.setForeground(UITheme.COLOR_ACCENT);

        JLabel appTitle = new JLabel("<html>Smart<br>Lost &amp; Found<br>System</html>");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        appTitle.setForeground(Color.WHITE);

        JLabel appSubtitle = new JLabel("<html>A centralized academic desktop platform to report, track, match, and claim campus belongings securely.</html>");
        appSubtitle.setFont(UITheme.FONT_REGULAR);
        appSubtitle.setForeground(new Color(203, 213, 225));

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        badgePanel.setOpaque(false);
        JLabel badge = new JLabel("• Role-Based Access  • Secure PBKDF2  • Verified Claims");
        badge.setFont(UITheme.FONT_SMALL);
        badge.setForeground(new Color(148, 163, 184));
        badgePanel.add(badge);

        brandPanel.add(collegeLabel);
        brandPanel.add(Box.createVerticalStrut(15));
        brandPanel.add(appTitle);
        brandPanel.add(Box.createVerticalStrut(20));
        brandPanel.add(appSubtitle);
        brandPanel.add(Box.createVerticalGlue());
        brandPanel.add(badgePanel);

        // ----------------------------------------------------
        // Right Login Form Panel
        // ----------------------------------------------------
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(UITheme.COLOR_BG);

        JPanel formCard = new JPanel();
        formCard.setBackground(Color.WHITE);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(35, 40, 35, 40)
        ));
        formCard.setPreferredSize(new Dimension(380, 460));

        JLabel welcomeLabel = new JLabel("Sign In");
        welcomeLabel.setFont(UITheme.FONT_HEADER);
        welcomeLabel.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel welcomeSub = new JLabel("Access your student or admin dashboard");
        welcomeSub.setFont(UITheme.FONT_REGULAR);
        welcomeSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        // Fields
        JLabel emailLbl = new JLabel("Email Address");
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

        loginButton = UITheme.createPrimaryButton("Sign In");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.addActionListener(e -> performLogin());

        registerButton = UITheme.createSecondaryButton("Register New Student Account");
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        registerButton.addActionListener(e -> openRegistration());

        // Enter key submission
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        emailField.addKeyListener(enterKey);
        passwordField.addKeyListener(enterKey);

        // Add components to card
        formCard.add(welcomeLabel);
        formCard.add(Box.createVerticalStrut(4));
        formCard.add(welcomeSub);
        formCard.add(Box.createVerticalStrut(20));

        formCard.add(emailLbl);
        formCard.add(Box.createVerticalStrut(5));
        formCard.add(emailField);
        formCard.add(Box.createVerticalStrut(12));

        formCard.add(passLbl);
        formCard.add(Box.createVerticalStrut(5));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(6));

        formCard.add(showPasswordCheck);
        formCard.add(Box.createVerticalStrut(10));
        formCard.add(statusLabel);
        formCard.add(Box.createVerticalStrut(10));

        formCard.add(loginButton);
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(registerButton);

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

        // Run authentication in background thread to keep UI responsive
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(email, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In");
                try {
                    User user = get();
                    if (user != null) {
                        dispose(); // Close login window
                        if (user.isAdmin()) {
                            new AdminDashboard(user).setVisible(true);
                        } else {
                            new StudentDashboard(user).setVisible(true);
                        }
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setText(cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void openRegistration() {
        new RegistrationFrame(this).setVisible(true);
        setVisible(false);
    }
}
