package ui;

import model.User;
import service.ClaimService;
import util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Screen to submit a Claim Request for an item with ownership proof.
 */
public class ClaimFrame extends JFrame {
    private final User currentUser;
    private final Integer lostItemId;
    private final Integer foundItemId;
    private final String itemTitle;
    private final Runnable onSuccessCallback;
    private final ClaimService claimService;

    private JTextArea claimDescArea;
    private JButton submitButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    public ClaimFrame(User currentUser, Integer lostItemId, Integer foundItemId, String itemTitle, Runnable onSuccessCallback) {
        this.currentUser = currentUser;
        this.lostItemId = lostItemId;
        this.foundItemId = foundItemId;
        this.itemTitle = itemTitle;
        this.onSuccessCallback = onSuccessCallback;
        this.claimService = new ClaimService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Submit Ownership Claim - Smart Lost-and-Found Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(580, 560);
        setMinimumSize(new Dimension(500, 500));
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(UITheme.COLOR_BG);
        contentPane.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.COLOR_BORDER, 1),
                new EmptyBorder(30, 35, 30, 35)
        ));

        JLabel titleLbl = new JLabel("Claim Ownership");
        titleLbl.setFont(UITheme.FONT_HEADER);
        titleLbl.setForeground(UITheme.COLOR_PRIMARY);

        String itemRef = (foundItemId != null ? "Found Item #" + foundItemId : "Lost Item #" + lostItemId) + ": " + itemTitle;
        JLabel itemLbl = new JLabel("Target: " + itemRef);
        itemLbl.setFont(UITheme.FONT_SUBHEADER);
        itemLbl.setForeground(UITheme.COLOR_ACCENT);

        JLabel infoLbl = new JLabel("<html><b>Ownership Verification Instructions:</b><br>" +
                "Please describe specific, non-public details to prove ownership (e.g., contents inside, secret markings, serial number, wallpaper, or purchase proof).</html>");
        infoLbl.setFont(UITheme.FONT_REGULAR);
        infoLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        claimDescArea = UITheme.createTextArea(6, 25);
        JScrollPane descScroll = new JScrollPane(claimDescArea);
        descScroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.COLOR_DANGER);

        submitButton = UITheme.createPrimaryButton("Submit Claim for Administrator Review");
        submitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        submitButton.addActionListener(e -> submitClaim());

        cancelButton = UITheme.createSecondaryButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cancelButton.addActionListener(e -> dispose());

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(itemLbl);
        card.add(Box.createVerticalStrut(15));
        card.add(infoLbl);
        card.add(Box.createVerticalStrut(15));

        JLabel explLbl = new JLabel("Detailed Ownership Explanation *");
        explLbl.setFont(UITheme.FONT_BOLD);
        explLbl.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        card.add(explLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(descScroll);
        card.add(Box.createVerticalStrut(10));

        card.add(statusLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(submitButton);
        card.add(Box.createVerticalStrut(10));
        card.add(cancelButton);

        contentPane.add(card, BorderLayout.CENTER);
        setContentPane(contentPane);
    }

    private void submitClaim() {
        String description = claimDescArea.getText().trim();
        statusLabel.setText(" ");

        if (description.isEmpty()) {
            statusLabel.setText("Please enter an explanation of ownership.");
            return;
        }

        submitButton.setEnabled(false);
        submitButton.setText("Submitting Claim...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return claimService.submitClaim(currentUser.getUserId(), lostItemId, foundItemId, description);
            }

            @Override
            protected void done() {
                submitButton.setEnabled(true);
                submitButton.setText("Submit Claim for Administrator Review");
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(ClaimFrame.this,
                                "Claim request submitted successfully!\nCampus administration will verify your details.",
                                "Claim Submitted",
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

