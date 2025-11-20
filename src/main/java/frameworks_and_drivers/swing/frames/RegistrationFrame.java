package frameworks_and_drivers.swing.frames;

import entities.User;
import frameworks_and_drivers.di.DependencyContainer;
import interface_adapters.presenters.RegistrationPresenter;
import interface_adapters.views.RegistrationView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class RegistrationFrame extends JFrame implements RegistrationView {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton backButton;
    private RegistrationPresenter presenter;
    public static final String CONFIG_FILE = "travel_scheduler.properties";

    public RegistrationFrame() {
        this.presenter = DependencyContainer.getInstance().provideRegistrationPresenter(this);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Travel Scheduler - Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Create Account", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
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
        gbc.weightx = 0.1;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.9;
        usernameField = new JTextField();
        formPanel.add(usernameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.9;
        emailField = new JTextField();
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.9;
        passwordField = new JPasswordField();
        formPanel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.1;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.9;
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField, gbc);

        formPanel.add(new JLabel("")); // Empty cell for alignment
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(240, 248, 255));

        registerButton = new JButton("Register");
        registerButton.setBackground(new Color(70, 130, 180));
        registerButton.setForeground(Color.BLACK);
        registerButton.setFocusPainted(false);
        registerButton.setPreferredSize(new Dimension(100, 30));

        backButton = new JButton("Back to Login");
        backButton.setBackground(new Color(100, 149, 237));
        backButton.setForeground(Color.BLACK);
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(120, 30));

        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        registerButton.addActionListener(new RegisterAction());
        backButton.addActionListener(e -> presenter.onBackToLoginClicked());

        // Set focus to username field when frame is shown
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent e) {
                usernameField.requestFocus();
            }
        });
    }

    private class RegisterAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            presenter.onRegisterClicked(username, email, password, confirmPassword);
        }
    }

    // RegistrationView interface implementation
    @Override
    public void showLoading() {
        registerButton.setEnabled(false);
        registerButton.setText("Registering...");
    }

    @Override
    public void hideLoading() {
        registerButton.setEnabled(true);
        registerButton.setText("Register");
    }

    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(RegistrationFrame.this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(RegistrationFrame.this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
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

    @Override
    public void navigateToLogin(User user) {
        if (user != null) {
            saveEmail(user.getEmail());
        }
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(null);
            loginFrame.setVisible(true);
        });
        dispose();
    }

    @Override
    public void clearForm() {
        passwordField.setText("");
        confirmPasswordField.setText("");
    }
}