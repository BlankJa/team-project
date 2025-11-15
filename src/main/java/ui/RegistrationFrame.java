package ui;

import model.User;
import service.DatabaseService;
import utils.ValidationUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrationFrame extends JFrame {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;
    private DatabaseService databaseService;

    public RegistrationFrame() {
        databaseService = DatabaseService.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Travel Scheduler - Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Create New Account", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(70, 130, 180));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(240, 248, 255));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipady = 8;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        usernameField = new JTextField();
        formPanel.add(usernameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        emailField = new JTextField();
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.7;
        passwordField = new JPasswordField();
        formPanel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Confirm:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.7;
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(240, 248, 255));

        registerButton = new JButton("Register");
        registerButton.setBackground(new Color(70, 130, 180));
        registerButton.setForeground(Color.BLACK);
        registerButton.setFocusPainted(false);
        registerButton.setPreferredSize(new Dimension(100, 30));

        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(100, 149, 237));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new Dimension(100, 30));

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        registerButton.addActionListener(new RegisterAction());
        cancelButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private class RegisterAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Validation
            if (!ValidationUtils.isValidUsername(username)) {
                JOptionPane.showMessageDialog(RegistrationFrame.this,
                        "Username must be at least 3 characters long.",
                        "Invalid Username", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtils.isValidEmail(email)) {
                JOptionPane.showMessageDialog(RegistrationFrame.this,
                        "Please enter a valid email address.",
                        "Invalid Email", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtils.isValidPassword(password)) {
                JOptionPane.showMessageDialog(RegistrationFrame.this,
                        "Password must be at least 6 characters long.",
                        "Invalid Password", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(RegistrationFrame.this,
                        "Passwords do not match.",
                        "Password Mismatch", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (databaseService.userExists(email)) {
                JOptionPane.showMessageDialog(RegistrationFrame.this,
                        "An account with this email already exists.",
                        "Email Exists", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create user
            User newUser = new User(username, email, password);
            boolean success = databaseService.createUser(newUser);

            if (success) {
                JOptionPane.showMessageDialog(RegistrationFrame.this,
                        "Registration successful! Please login.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                new LoginFrame().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(RegistrationFrame.this,
                        "Registration failed. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

