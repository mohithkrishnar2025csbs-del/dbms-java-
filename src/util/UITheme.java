package util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Centralized Modern UI Theme and Component Factory for Smart Lost-and-Found System.
 * Ensures consistent, clean, and elegant visual presentation across all frames.
 */
public class UITheme {

    // Color Palette
    public static final Color COLOR_PRIMARY = new Color(26, 54, 93);       // Deep Navy Blue
    public static final Color COLOR_PRIMARY_DARK = new Color(15, 32, 58);  // Darker Navy for hover/sidebar
    public static final Color COLOR_ACCENT = new Color(13, 148, 136);      // Teal Accent
    public static final Color COLOR_ACCENT_HOVER = new Color(15, 118, 110);
    public static final Color COLOR_SECONDARY = Color.WHITE;
    public static final Color COLOR_BG = new Color(245, 247, 250);         // Modern Light Gray
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240);     // Subtle slate border
    public static final Color COLOR_TEXT_PRIMARY = new Color(30, 41, 59);  // Slate Dark
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); // Slate Muted
    public static final Color COLOR_DANGER = new Color(225, 29, 72);       // Rose Red
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);     // Emerald Green
    public static final Color COLOR_WARNING = new Color(245, 158, 11);     // Amber Warning

    // Fonts
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_VALUE = new Font("Segoe UI", Font.BOLD, 28);

    /**
     * Creates a styled modern primary action button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(COLOR_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(COLOR_ACCENT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(COLOR_PRIMARY);
            }
        });
        return button;
    }

    /**
     * Creates a styled accent button (Teal).
     */
    public static JButton createAccentButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(COLOR_ACCENT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(COLOR_ACCENT_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(COLOR_ACCENT);
            }
        });
        return button;
    }

    /**
     * Creates a danger/action button (Red).
     */
    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(COLOR_DANGER);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(190, 18, 60));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(COLOR_DANGER);
            }
        });
        return button;
    }

    /**
     * Creates an outline/secondary button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setForeground(COLOR_TEXT_PRIMARY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(9, 17, 9, 17)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(241, 245, 249));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    /**
     * Creates a navigation sidebar button.
     */
    public static JButton createNavButton(String text, boolean active) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(210, 44));

        if (active) {
            button.setForeground(Color.WHITE);
            button.setBackground(COLOR_ACCENT);
            button.setBorder(new EmptyBorder(10, 20, 10, 15));
        } else {
            button.setForeground(new Color(203, 213, 225));
            button.setBackground(COLOR_PRIMARY);
            button.setBorder(new EmptyBorder(10, 20, 10, 15));

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(COLOR_PRIMARY_DARK);
                    button.setForeground(Color.WHITE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(COLOR_PRIMARY);
                    button.setForeground(new Color(203, 213, 225));
                }
            });
        }
        return button;
    }

    /**
     * Creates a standard styled text field with internal padding.
     */
    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_REGULAR);
        tf.setForeground(COLOR_TEXT_PRIMARY);
        tf.setBackground(Color.WHITE);
        tf.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(8, 10, 8, 10)));
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 36));
        return tf;
    }

    /**
     * Creates a standard styled password field.
     */
    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_REGULAR);
        pf.setForeground(COLOR_TEXT_PRIMARY);
        pf.setBackground(Color.WHITE);
        pf.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(8, 10, 8, 10)));
        pf.setPreferredSize(new Dimension(pf.getPreferredSize().width, 36));
        return pf;
    }

    /**
     * Creates a styled text area with word wrap and internal padding.
     */
    public static JTextArea createTextArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(FONT_REGULAR);
        ta.setForeground(COLOR_TEXT_PRIMARY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(8, 10, 8, 10));
        return ta;
    }

    /**
     * Creates a styled combo box.
     */
    public static <E> JComboBox<E> createComboBox(E[] items) {
        JComboBox<E> cb = new JComboBox<>(items);
        cb.setFont(FONT_REGULAR);
        cb.setBackground(Color.WHITE);
        cb.setForeground(COLOR_TEXT_PRIMARY);
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width, 36));
        return cb;
    }

    /**
     * Creates a statistic KPI card for dashboards.
     */
    public static JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Top Accent Stripe or Title
        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(COLOR_TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_STAT_VALUE);
        valueLabel.setForeground(accentColor != null ? accentColor : COLOR_PRIMARY);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    /**
     * Formats and styles a JTable for clean, modern desktop display.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(224, 242, 254)); // Soft light blue
        table.setSelectionForeground(COLOR_TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(COLOR_TEXT_PRIMARY);
        header.setBorder(new LineBorder(COLOR_BORDER, 1));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
}

