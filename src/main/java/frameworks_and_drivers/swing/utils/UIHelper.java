package frameworks_and_drivers.swing.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIHelper {

    /**
     * Create a styled button with consistent appearance
     */
    public static JButton createStyledButton(String text, Color backgroundColor, Color foregroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFont(new Font("Arial", Font.BOLD, 14));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });

        return button;
    }

    /**
     * Create a feature button for the main dashboard
     */
    public static JButton createFeatureButton(String title, String emoji, String description, Color color) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(0, 5));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);

        // Emoji label
        JLabel emojiLabel = new JLabel(emoji, JLabel.CENTER);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        emojiLabel.setPreferredSize(new Dimension(50, 50));
        emojiLabel.setVerticalAlignment(SwingConstants.CENTER);
        emojiLabel.setHorizontalAlignment(SwingConstants.CENTER);
        button.add(emojiLabel, BorderLayout.NORTH);

        // Title label
        JLabel titleLabel = new JLabel("<html><center>" + title + "</center></html>", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        button.add(titleLabel, BorderLayout.CENTER);

        // Description label
        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>", JLabel.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(Color.DARK_GRAY);
        descLabel.setVerticalAlignment(SwingConstants.CENTER);
        button.add(descLabel, BorderLayout.SOUTH);

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    /**
     * Create a standardized text area for displaying information
     */
    public static JTextArea createInfoTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(new Color(255, 255, 240));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    /**
     * Create a standardized panel with consistent styling
     */
    public static JPanel createStyledPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    /**
     * Create a titled panel with consistent styling
     */
    public static JPanel createTitledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    /**
     * Center a component on screen
     */
    public static void centerOnScreen(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = window.getSize();
        int x = (screenSize.width - windowSize.width) / 2;
        int y = (screenSize.height - windowSize.height) / 2;
        window.setLocation(x, y);
    }

    /**
     * Show a loading dialog
     */
    public static JDialog createLoadingDialog(Component parent, String message) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Loading", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 120);
        dialog.setResizable(false);
        centerOnScreen(dialog);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);

        JLabel messageLabel = new JLabel(message, JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        contentPanel.add(messageLabel, BorderLayout.CENTER);
        contentPanel.add(progressBar, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        return dialog;
    }

    /**
     * Create a standardized combo box
     */
    public static JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setBackground(Color.WHITE);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        comboBox.setPreferredSize(new Dimension(200, 25));
        return comboBox;
    }

    /**
     * Create a standardized text field
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setBackground(Color.WHITE);
        return textField;
    }

    /**
     * Create a standardized spinner
     */
    public static JSpinner createStyledSpinner(SpinnerModel model) {
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Arial", Font.PLAIN, 12));
        spinner.setPreferredSize(new Dimension(100, 25));
        return spinner;
    }

    /**
     * Apply consistent frame styling
     */
    public static void styleFrame(JFrame frame) {
        frame.setIconImage(null); // You can set an icon here if available
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }
    }

    /**
     * Create a color scheme for the application
     */
    public static class Colors {
        public static final Color PRIMARY = new Color(70, 130, 180);
        public static final Color SECONDARY = new Color(100, 149, 237);
        public static final Color SUCCESS = new Color(34, 139, 34);
        public static final Color WARNING = new Color(255, 165, 0);
        public static final Color ERROR = new Color(220, 80, 80);
        public static final Color BACKGROUND = new Color(240, 248, 255);
        public static final Color CARD_BACKGROUND = Color.WHITE;
    }
}