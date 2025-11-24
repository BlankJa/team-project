package placefinder.frameworks_drivers.view.frames;

import placefinder.frameworks_drivers.view.components.swing.Button;
import placefinder.frameworks_drivers.view.components.swing.MyPasswordField;
import placefinder.frameworks_drivers.view.components.swing.MyTextField;
import placefinder.frameworks_drivers.view.components.swing.PanelRound;
import placefinder.interface_adapters.controllers.RegisterController;
import placefinder.interface_adapters.viewmodels.RegisterViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Registration panel UI component.
 * Displays the registration form with name, email, password, and optional home city fields.
 */
public class RegisterPanel extends JPanel {

    private final AppFrame appFrame;
    private final RegisterController registerController;
    private final RegisterViewModel registerVM;

    private MyTextField nameField;
    private MyTextField emailField;
    private MyPasswordField passwordField;
    private MyTextField homeCityField;
    private JLabel messageLabel;

    public RegisterPanel(AppFrame appFrame,
                         RegisterController registerController,
                         RegisterViewModel registerVM) {
        this.appFrame = appFrame;
        this.registerController = registerController;
        this.registerVM = registerVM;
        initUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Same gradient background as login
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

        PanelRound card = new PanelRound();
        card.setOpaque(false);
        card.setBackground(new Color(255, 255, 255));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(35, 60, 35, 60));

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("sansserif", Font.BOLD, 30));
        title.setForeground(new Color(7, 164, 121));

        JLabel subtitle = new JLabel("Sign up to start planning your trips", SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("sansserif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(120, 120, 120));

        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(25));

        nameField = new MyTextField();
        nameField.setHint("Name");
        card.add(nameField);
        card.add(Box.createVerticalStrut(15));

        emailField = new MyTextField();
        emailField.setHint("Email");
        card.add(emailField);
        card.add(Box.createVerticalStrut(15));

        passwordField = new MyPasswordField();
        passwordField.setHint("Password");
        card.add(passwordField);
        card.add(Box.createVerticalStrut(15));

        homeCityField = new MyTextField();
        homeCityField.setHint("Home City (optional)");
        card.add(homeCityField);
        card.add(Box.createVerticalStrut(10));

        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        messageLabel.setForeground(Color.RED);
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(15));

        Button registerButton = new Button();
        registerButton.setText("SIGN UP");
        registerButton.setBackground(new Color(7, 164, 121));
        registerButton.setForeground(new Color(250, 250, 250));
        registerButton.setFont(new Font("sansserif", Font.BOLD, 14));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setPreferredSize(new Dimension(200, 40));
        registerButton.addActionListener(e -> doRegister());
        card.add(registerButton);
        card.add(Box.createVerticalStrut(20));

        JPanel bottomText = new JPanel();
        bottomText.setOpaque(false);
        bottomText.setLayout(new BoxLayout(bottomText, BoxLayout.X_AXIS));
        JLabel haveAccountLabel = new JLabel("Already have an account? ");
        haveAccountLabel.setForeground(new Color(100, 100, 100));
        JButton loginLink = new JButton("Sign in");
        loginLink.setContentAreaFilled(false);
        loginLink.setBorderPainted(false);
        loginLink.setFocusPainted(false);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.setForeground(new Color(7, 164, 121));
        loginLink.setFont(new Font("sansserif", Font.BOLD, 12));
        loginLink.addActionListener(e -> appFrame.showLogin());
        bottomText.add(haveAccountLabel);
        bottomText.add(loginLink);
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

    private void doRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String homeCity = homeCityField.getText().trim();

        registerController.register(name, email, password, homeCity);

        messageLabel.setText(registerVM.getMessage() != null ? registerVM.getMessage() : "");

        if (registerVM.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                    "Registration successful. You can now log in.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            appFrame.showLogin();
        }
    }
}
