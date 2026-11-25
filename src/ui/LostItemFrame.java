package ui;

import model.User;
import service.ItemService;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;

/**
 * Screen to report a Lost Item.
 */
public class LostItemFrame extends JFrame {
    private final User currentUser;
    private final ItemService itemService;
    private final Runnable onSuccessCallback;

    private JTextField itemNameField;
    private JComboBox<String> categoryCombo;
    private JTextArea descriptionArea;
    private JTextField locationField;
    private JTextField dateField;
    private JButton submitButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    public static final String[] CATEGORIES = {
            "Select Category", "ID Card", "Wallet", "Mobile Phone", "Keys",
            "Books", "Electronics", "Bags", "Accessories", "Other"
    };

    public LostItemFrame(User currentUser, Runnable onSuccessCallback) {
        this.currentUser = currentUser;
        this.onSuccessCallback = onSuccessCallback;
        this.itemService = new ItemService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Report Lost Item - Smart Lost-and-Found Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(580, 680);
        setMinimumSize(new Dimension(520, 600));
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(UITheme.COLOR_BG);
        contentPane.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(30, 35, 30, 35)
        ));

        JLabel titleLbl = new JLabel("Report a Lost Item");
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.COLOR_PRIMARY);

        JLabel subLbl = new JLabel("Provide accurate details to assist campus security and fellow students.");
        subLbl.setFont(UITheme.FONT_REGULAR);
        subLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        itemNameField = UITheme.createTextField();
        categoryCombo = UITheme.createComboBox(CATEGORIES);
        descriptionArea = UITheme.createTextArea(4, 25);
        locationField = UITheme.createTextField();
        dateField = UITheme.createTextField();
        dateField.setText(LocalDate.now().toString()); // Default to today: YYYY-MM-DD

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.COLOR_DANGER);

        submitButton = UITheme.createPrimaryButton("Submit Lost Item Report");
        submitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        submitButton.addActionListener(e -> submitReport());

        cancelButton = UITheme.createSecondaryButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cancelButton.addActionListener(e -> dispose());

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(subLbl);
        card.add(Box.createVerticalStrut(20));

        addLabeledField(card, "Item Name * (e.g., Blue Fastrack Backpack)", itemNameField);
        addLabeledField(card, "Category *", categoryCombo);

        JLabel descLbl = new JLabel("Description & Identifying Marks (e.g., brand, color, scratches)");
        descLbl.setFont(UITheme.FONT_BOLD);
        descLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        card.add(descLbl);
        card.add(Box.createVerticalStrut(4));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.add(descScroll);
        card.add(Box.createVerticalStrut(10));

        addLabeledField(card, "Lost Location * (e.g., Main Cafeteria Table 5)", locationField);
        addLabeledField(card, "Date Lost * (YYYY-MM-DD)", dateField);

        card.add(statusLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(submitButton);
        card.add(Box.createVerticalStrut(10));
        card.add(cancelButton);

        JScrollPane mainScroll = new JScrollPane(card);
        mainScroll.setBorder(null);
        mainScroll.setBackground(UITheme.COLOR_BG);
        mainScroll.getViewport().setBackground(UITheme.COLOR_BG);

        contentPane.add(mainScroll, BorderLayout.CENTER);
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

    private void submitReport() {
        String name = itemNameField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        String desc = descriptionArea.getText().trim();
        String loc = locationField.getText().trim();
        String dateStr = dateField.getText().trim();

        statusLabel.setText(" ");

        submitButton.setEnabled(false);
        submitButton.setText("Submitting...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return itemService.reportLostItem(currentUser.getUserId(), name, category, desc, loc, dateStr);
            }

            @Override
            protected void done() {
                submitButton.setEnabled(true);
                submitButton.setText("Submit Lost Item Report");
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(LostItemFrame.this,
                                "Lost item reported successfully!\nIt is now listed in the campus lost-and-found registry.",
                                "Report Submitted",
                                JOptionPane.INFORMATION_MESSAGE);
                        if (onSuccessCallback != null) {
                            onSuccessCallback.run();
                        }
                        dispose();
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

