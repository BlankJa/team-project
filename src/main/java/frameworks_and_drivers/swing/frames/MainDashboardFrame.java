package frameworks_and_drivers.swing.frames;

import entities.User;
import interface_adapters.presenters.MainDashboardPresenter;
import interface_adapters.views.MainDashboardView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainDashboardFrame extends JFrame implements MainDashboardView {
    private User currentUser;
    private MainDashboardPresenter presenter;

    public MainDashboardFrame(User user) {
        this.currentUser = user;
        this.presenter = new MainDashboardPresenter(this);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Travel Scheduler - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Create main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 248, 255));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUserName() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> presenter.onLogoutClicked());
        headerPanel.add(logoutButton, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content panel - Simple centered layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(240, 248, 255));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);

        // Create preferences button
        JButton preferencesButton = createFeatureButton("Set Preferences", "🛠️",
                "Set your travel preferences and interests", new Color(100, 149, 237));

        contentPanel.add(preferencesButton, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Add action listener
        preferencesButton.addActionListener(e -> presenter.onPreferencesClicked());

        add(mainPanel);
    }

    private JButton createFeatureButton(String title, String emoji, String description, Color color) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(0, 5));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);

        JLabel emojiLabel = new JLabel(emoji, JLabel.CENTER);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        emojiLabel.setPreferredSize(new Dimension(50, 50));
        emojiLabel.setVerticalAlignment(SwingConstants.CENTER);
        emojiLabel.setHorizontalAlignment(SwingConstants.CENTER);
        button.add(emojiLabel, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel("<html><center>" + title + "</center></html>", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setVerticalAlignment(SwingConstants.CENTER);
        button.add(titleLabel, BorderLayout.CENTER);

        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>", JLabel.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        descLabel.setForeground(Color.DARK_GRAY);
        descLabel.setVerticalAlignment(SwingConstants.CENTER);
        button.add(descLabel, BorderLayout.SOUTH);

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

    @Override
    public void showWelcomeMessage(String username) {
    }

    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void navigateToLogin() {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(null); // Presenter will be injected via DI
            loginFrame.setVisible(true);
        });
        dispose();
    }

    @Override
    public void openPreferenceFrame() {
        SwingUtilities.invokeLater(() -> {
            PreferenceFrame preferenceFrame = new PreferenceFrame(currentUser);
            preferenceFrame.setVisible(true);
        });
    }
}