package ui;

import model.User;
import service.DatabaseService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class LoginFrame extends JFrame {
    public static final String CONFIG_FILE = "travel_scheduler.properties";
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private DatabaseService databaseService;

    public LoginFrame() {
        databaseService = DatabaseService.getInstance();
        initializeUI();
        loadSavedEmail();
    }

    private void initializeUI() {
        setTitle("Travel Scheduler - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create main panel with background color
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title label
        JLabel titleLabel = new JLabel("Travel Scheduler", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 248, 255));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 8;

        // Email
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.15;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.85;
        emailField = new JTextField();
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.15;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.85;
        passwordField = new JPasswordField();
        formPanel.add(passwordField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(240, 248, 255));

        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(100, 30));

        registerButton = new JButton("Register");
        registerButton.setBackground(new Color(100, 149, 237));
        registerButton.setForeground(Color.BLACK);
        registerButton.setFocusPainted(false);
        registerButton.setPreferredSize(new Dimension(100, 30));

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Add action listeners
        loginButton.addActionListener(new LoginAction());
        registerButton.addActionListener(e -> openRegistrationFrame());

        // Enter key listener for login
        passwordField.addActionListener(new LoginAction());

        // Set focus to email field when frame is shown
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent e) {
                if (!emailField.getText().isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        passwordField.requestFocusInWindow();
                    });
                } else {
                    emailField.requestFocusInWindow();
                }
            }
        });
    }

    private void loadSavedEmail() {
        try {
            Properties props = new Properties();
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                props.load(new FileInputStream(configFile));
                String savedEmail = props.getProperty("lastEmail", "");
                if (!savedEmail.isEmpty()) {
                    emailField.setText(savedEmail);
                    passwordField.requestFocus();
                }
            }
        } catch (IOException e) {
        }
    }

    private void saveEmail(String email) {
        try {
            Properties props = new Properties();
            props.setProperty("lastEmail", email);
            props.store(new FileOutputStream(CONFIG_FILE), "Travel Scheduler Configuration");
        } catch (IOException e) {
            System.err.println("Failed to save email: " + e.getMessage());
        }
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Please enter both email and password",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User authenticatedUser = databaseService.authenticateUser(email, password);

            if (authenticatedUser != null) {
                saveEmail(email);
                openMainDashboard(authenticatedUser);
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Invalid email or password. Please try again or register a new account.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocus();
            }
        }
    }

    private void openRegistrationFrame() {
        RegistrationFrame registrationFrame = new RegistrationFrame();
        registrationFrame.setVisible(true);
        this.dispose();
    }

    private void openMainDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            MainDashboard dashboard = new MainDashboard(user);
            dashboard.setVisible(true);
        });
    }
}

