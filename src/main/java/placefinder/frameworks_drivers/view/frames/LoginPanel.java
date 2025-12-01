package placefinder.frameworks_drivers.view.frames;

import placefinder.frameworks_drivers.view.components.swing.Button;
import placefinder.frameworks_drivers.view.components.swing.MyPasswordField;
import placefinder.frameworks_drivers.view.components.swing.MyTextField;
import placefinder.frameworks_drivers.view.components.swing.PanelRound;
import placefinder.frameworks_drivers.view.components.swing.LoadingOverlay;
import placefinder.interface_adapters.controllers.LoginController;
import placefinder.interface_adapters.viewmodels.LoginViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Login panel UI component.
 * Displays the login form with email and password fields, and handles user authentication.
 */
public class LoginPanel extends JPanel {

    private final AppFrame appFrame;
    private final LoginController loginController;
    private final LoginViewModel loginVM;

    private MyTextField emailField;
    private MyPasswordField passwordField;
    private JLabel errorLabel;
    private LoadingOverlay loadingOverlay;

    public LoginPanel(AppFrame appFrame,
                      LoginController loginController,
                      LoginViewModel loginVM) {
        this.appFrame = appFrame;
        this.loginController = loginController;
        this.loginVM = loginVM;
        initUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Soft green gradient background like the Raven UI
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color c1 = new Color(7, 164, 121);
        Color c2 = new Color(0, 92, 75);
        GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void initUI() {
        setLayout(new GridBagLayout());

        // Center card
        PanelRound card = new PanelRound();
        card.setOpaque(false);
        card.setBackground(new Color(255, 255, 255));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel title = new JLabel("Sign In", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("sansserif", Font.BOLD, 30));
        title.setForeground(new Color(7, 164, 121));

        JLabel subtitle = new JLabel("Welcome back to PlaceFinder", SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("sansserif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(120, 120, 120));

        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(30));

        emailField = new MyTextField();
        emailField.setHint("Email");
        card.add(emailField);
        card.add(Box.createVerticalStrut(15));

        passwordField = new MyPasswordField();
        passwordField.setHint("Password");
        card.add(passwordField);
        card.add(Box.createVerticalStrut(10));

        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(15));

        Button loginButton = new Button();
        loginButton.setText("SIGN IN");
        loginButton.setBackground(new Color(7, 164, 121));
        loginButton.setForeground(new Color(250, 250, 250));
        loginButton.setFont(new Font("sansserif", Font.BOLD, 14));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setPreferredSize(new Dimension(180, 40));
        loginButton.addActionListener(e -> doLogin());
        card.add(loginButton);
        card.add(Box.createVerticalStrut(20));

        // "Don't have an account? Sign up" link-like text
        JPanel bottomText = new JPanel();
        bottomText.setOpaque(false);
        bottomText.setLayout(new BoxLayout(bottomText, BoxLayout.X_AXIS));
        JLabel noAccountLabel = new JLabel("Don't have an account? ");
        noAccountLabel.setForeground(new Color(100, 100, 100));
        JButton registerLink = new JButton("Sign up");
        registerLink.setContentAreaFilled(false);
        registerLink.setBorderPainted(false);
        registerLink.setFocusPainted(false);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.setForeground(new Color(7, 164, 121));
        registerLink.setFont(new Font("sansserif", Font.BOLD, 12));
        registerLink.addActionListener(e -> appFrame.showRegister());
        bottomText.add(noAccountLabel);
        bottomText.add(registerLink);
        bottomText.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(bottomText);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(card, gbc);
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Set loading state and show loading animation
        loginVM.setLoading(true);
        showLoadingOverlay("Logging in...");

        // Execute login operation in background thread
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loginController.login(email, password);
                return null;
            }

            @Override
            protected void done() {
                // Set loading state and hide loading animation
                loginVM.setLoading(false);
                hideLoadingOverlay();

                // Process login result
                if (loginVM.getLoggedInUser() != null) {
                    errorLabel.setText(" ");
                    appFrame.onLoginSuccess();
                } else {
                    String msg = loginVM.getErrorMessage();
                    errorLabel.setText(msg != null ? msg : "Login failed.");
                }
            }
        };
        worker.execute();
    }

    private void showLoadingOverlay(String message) {
        SwingUtilities.invokeLater(() -> {
            if (loadingOverlay == null) {
                loadingOverlay = new LoadingOverlay(message);
            } else {
                loadingOverlay.setMessage(message);
            }
            loadingOverlay.showOverlay(this);
        });
    }

    private void hideLoadingOverlay() {
        SwingUtilities.invokeLater(() -> {
            if (loadingOverlay != null) {
                loadingOverlay.hideOverlay(this);
            }
        });
    }
}
