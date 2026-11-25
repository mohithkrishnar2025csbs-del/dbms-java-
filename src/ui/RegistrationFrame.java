package ui;

import service.AuthService;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Registration Screen for new students with real-time validation and PBKDF2 hashing.
 */
public class RegistrationFrame extends JFrame {
    private final AuthService authService;
    private final JFrame parentLoginFrame;

    private JTextField nameField;
    private JTextField regNoField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backButton;
    private JLabel errorLabel;

    public RegistrationFrame(JFrame parentLoginFrame) {
        this.parentLoginFrame = parentLoginFrame;
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Student Registration - Smart Lost-and-Found Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 640);
        setMinimumSize(new Dimension(500, 580));
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(UITheme.COLOR_BG);
        contentPane.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Form Card
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(30, 35, 30, 35)
        ));

        JLabel titleLbl = new JLabel("Student Registration");
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.COLOR_PRIMARY);

        JLabel subLbl = new JLabel("Create a new account to report items and submit claims");
        subLbl.setFont(UITheme.FONT_REGULAR);
        subLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        // Inputs
        nameField = UITheme.createTextField();
        regNoField = UITheme.createTextField();
        emailField = UITheme.createTextField();
        passwordField = UITheme.createPasswordField();
        confirmPasswordField = UITheme.createPasswordField();

        errorLabel = new JLabel(" ");
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.COLOR_DANGER);

        registerButton = UITheme.createAccentButton("Complete Registration");
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        registerButton.addActionListener(e -> performRegistration());

        backButton = UITheme.createSecondaryButton("Back to Sign In");
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        backButton.addActionListener(e -> returnToLogin());

        // Assemble card
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(subLbl);
        card.add(Box.createVerticalStrut(20));

        addField(card, "Full Name (e.g., Alex Johnson)", nameField);
        addField(card, "Register Number / Student ID (e.g., 2024CS101)", regNoField);
        addField(card, "Campus Email Address (e.g., alex@college.edu)", emailField);
        addField(card, "Password (min. 6 characters)", passwordField);
        addField(card, "Confirm Password", confirmPasswordField);

        card.add(errorLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(registerButton);
        card.add(Box.createVerticalStrut(10));
        card.add(backButton);

        JScrollPane scrollPane = new JScrollPane(card);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UITheme.COLOR_BG);
        scrollPane.getViewport().setBackground(UITheme.COLOR_BG);

        contentPane.add(scrollPane, BorderLayout.CENTER);
        setContentPane(contentPane);

        // Window close handler to restore login frame
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                returnToLogin();
            }
        });
    }

    private void addField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        panel.add(Box.createVerticalStrut(10));
    }

    private void performRegistration() {
        String name = nameField.getText().trim();
        String regNo = regNoField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        errorLabel.setText(" ");

        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authService.registerStudent(name, regNo, email, pass, confirm);
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                registerButton.setText("Complete Registration");
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(RegistrationFrame.this,
                                "Account registered successfully!\nYou may now sign in with your campus email.",
                                "Registration Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        returnToLogin();
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    errorLabel.setText(cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void returnToLogin() {
        dispose();
        if (parentLoginFrame != null) {
            parentLoginFrame.setVisible(true);
        } else {
            new LoginFrame().setVisible(true);
        }
    }
}

