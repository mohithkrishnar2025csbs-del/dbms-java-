package ui;

import model.User;
import service.AuthService;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Screen allowing users to update their profile information and change passwords.
 */
public class ProfileFrame extends JFrame {
    private final User currentUser;
    private final AuthService authService;
    private final Runnable onProfileUpdated;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField regNoField;

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    private JButton saveProfileBtn;
    private JButton changePassBtn;
    private JLabel profileStatusLbl;
    private JLabel passStatusLbl;

    public ProfileFrame(User currentUser, Runnable onProfileUpdated) {
        this.currentUser = currentUser;
        this.onProfileUpdated = onProfileUpdated;
        this.authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setTitle("My Profile - Smart Lost-and-Found Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(620, 680);
        setMinimumSize(new Dimension(540, 600));
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBackground(UITheme.COLOR_BG);
        contentPane.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        // ----------------------------------------------------
        // Profile Card
        // ----------------------------------------------------
        JPanel profileCard = new JPanel();
        profileCard.setBackground(Color.WHITE);
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel titleLbl = new JLabel("User Profile Information");
        titleLbl.setFont(UITheme.FONT_SUBHEADER);
        titleLbl.setForeground(UITheme.COLOR_PRIMARY);

        regNoField = UITheme.createTextField();
        regNoField.setText(currentUser.getRegisterNumber());
        regNoField.setEditable(false);
        regNoField.setBackground(new Color(241, 245, 249));

        nameField = UITheme.createTextField();
        nameField.setText(currentUser.getName());

        emailField = UITheme.createTextField();
        emailField.setText(currentUser.getEmail());

        profileStatusLbl = new JLabel(" ");
        profileStatusLbl.setFont(UITheme.FONT_SMALL);
        profileStatusLbl.setForeground(UITheme.COLOR_SUCCESS);

        saveProfileBtn = UITheme.createPrimaryButton("Save Profile Changes");
        saveProfileBtn.addActionListener(e -> updateProfile());

        profileCard.add(titleLbl);
        profileCard.add(Box.createVerticalStrut(15));
        addLabeledField(profileCard, "Register Number (Non-Editable)", regNoField);
        addLabeledField(profileCard, "Full Name", nameField);
        addLabeledField(profileCard, "Campus Email", emailField);
        profileCard.add(profileStatusLbl);
        profileCard.add(Box.createVerticalStrut(8));
        profileCard.add(saveProfileBtn);

        // ----------------------------------------------------
        // Password Card
        // ----------------------------------------------------
        JPanel passCard = new JPanel();
        passCard.setBackground(Color.WHITE);
        passCard.setLayout(new BoxLayout(passCard, BoxLayout.Y_AXIS));
        passCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel passTitle = new JLabel("Change Security Password");
        passTitle.setFont(UITheme.FONT_SUBHEADER);
        passTitle.setForeground(UITheme.COLOR_PRIMARY);

        currentPasswordField = UITheme.createPasswordField();
        newPasswordField = UITheme.createPasswordField();
        confirmPasswordField = UITheme.createPasswordField();

        passStatusLbl = new JLabel(" ");
        passStatusLbl.setFont(UITheme.FONT_SMALL);
        passStatusLbl.setForeground(UITheme.COLOR_DANGER);

        changePassBtn = UITheme.createSecondaryButton("Update Password");
        changePassBtn.addActionListener(e -> changePassword());

        passCard.add(passTitle);
        passCard.add(Box.createVerticalStrut(15));
        addLabeledField(passCard, "Current Password", currentPasswordField);
        addLabeledField(passCard, "New Password (min. 6 characters)", newPasswordField);
        addLabeledField(passCard, "Confirm New Password", confirmPasswordField);
        passCard.add(passStatusLbl);
        passCard.add(Box.createVerticalStrut(8));
        passCard.add(changePassBtn);

        container.add(profileCard);
        container.add(Box.createVerticalStrut(15));
        container.add(passCard);

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.setBackground(UITheme.COLOR_BG);
        scrollPane.getViewport().setBackground(UITheme.COLOR_BG);

        contentPane.add(scrollPane, BorderLayout.CENTER);
        setContentPane(contentPane);
    }

    private void addLabeledField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        panel.add(Box.createVerticalStrut(10));
    }

    private void updateProfile() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        profileStatusLbl.setText(" ");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authService.updateProfile(currentUser.getUserId(), name, email);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        profileStatusLbl.setForeground(UITheme.COLOR_SUCCESS);
                        profileStatusLbl.setText("Profile updated successfully!");
                        if (onProfileUpdated != null) {
                            onProfileUpdated.run();
                        }
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    profileStatusLbl.setForeground(UITheme.COLOR_DANGER);
                    profileStatusLbl.setText(cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void changePassword() {
        String currentPass = new String(currentPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        passStatusLbl.setText(" ");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authService.changePassword(currentUser.getUserId(), currentPass, newPass, confirmPass);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        passStatusLbl.setForeground(UITheme.COLOR_SUCCESS);
                        passStatusLbl.setText("Password changed successfully!");
                        currentPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    passStatusLbl.setForeground(UITheme.COLOR_DANGER);
                    passStatusLbl.setText(cause.getMessage());
                }
            }
        };
        worker.execute();
    }
}

