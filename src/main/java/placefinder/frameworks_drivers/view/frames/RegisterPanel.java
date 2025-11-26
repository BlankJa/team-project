package placefinder.frameworks_drivers.view.frames;

import placefinder.frameworks_drivers.view.components.swing.Button;
import placefinder.frameworks_drivers.view.components.swing.MyPasswordField;
import placefinder.frameworks_drivers.view.components.swing.MyTextField;
import placefinder.frameworks_drivers.view.components.swing.PanelRound;
import placefinder.interface_adapters.controllers.RegisterController;
import placefinder.interface_adapters.controllers.VerifyEmailController;
import placefinder.interface_adapters.viewmodels.RegisterViewModel;
import placefinder.interface_adapters.viewmodels.VerifyEmailViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Registration (sign up) panel.
 * After a successful registration, it immediately opens the VerifyEmailDialog.
 */
public class RegisterPanel extends JPanel {

    private final AppFrame appFrame;
    private final RegisterController registerController;
    private final RegisterViewModel registerVM;

    private final VerifyEmailController verifyEmailController;
    private final VerifyEmailViewModel verifyVM;

    private MyTextField nameField;
    private MyTextField emailField;
    private MyPasswordField passwordField;
    private MyTextField homeCityField;
    private JLabel messageLabel;

    public RegisterPanel(AppFrame appFrame,
                         RegisterController registerController,
                         RegisterViewModel registerVM,
                         VerifyEmailController verifyEmailController,
                         VerifyEmailViewModel verifyVM) {
        this.appFrame = appFrame;
        this.registerController = registerController;
        this.registerVM = registerVM;
        this.verifyEmailController = verifyEmailController;
        this.verifyVM = verifyVM;
        initUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Same gradient style as LoginPanel
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
        card.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("sansserif", Font.BOLD, 30));
        title.setForeground(new Color(7, 164, 121));

        JLabel subtitle = new JLabel("Join PlaceFinder", SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("sansserif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(120, 120, 120));

        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(30));

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
        homeCityField.setHint("Home city (optional)");
        card.add(homeCityField);
        card.add(Box.createVerticalStrut(15));

        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(15));

        Button signUpButton = new Button();
        signUpButton.setText("SIGN UP");
        signUpButton.setBackground(new Color(7, 164, 121));
        signUpButton.setForeground(new Color(250, 250, 250));
        signUpButton.setFont(new Font("sansserif", Font.BOLD, 14));
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpButton.setPreferredSize(new Dimension(180, 40));
        signUpButton.addActionListener(e -> doRegister());
        card.add(signUpButton);
        card.add(Box.createVerticalStrut(20));

        // "Already have an account? Sign in"
        JPanel bottomText = new JPanel();
        bottomText.setOpaque(false);
        bottomText.setLayout(new BoxLayout(bottomText, BoxLayout.X_AXIS));
        JLabel alreadyLabel = new JLabel("Already have an account? ");
        alreadyLabel.setForeground(new Color(100, 100, 100));
        JButton loginLink = new JButton("Sign in");
        loginLink.setContentAreaFilled(false);
        loginLink.setBorderPainted(false);
        loginLink.setFocusPainted(false);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.setForeground(new Color(7, 164, 121));
        loginLink.setFont(new Font("sansserif", Font.BOLD, 12));
        loginLink.addActionListener(e -> appFrame.showLogin());
        bottomText.add(alreadyLabel);
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

        String msg = registerVM.getMessage();
        messageLabel.setText(msg != null ? msg : " ");

        if (registerVM.isSuccess()) {
            // Immediately open verification dialog with the EXACT email used
            openVerifyDialog(email);
        }
    }

    private void openVerifyDialog(String email) {
        VerifyEmailDialog dialog = new VerifyEmailDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                verifyEmailController,
                verifyVM,
                email
        );
        dialog.setVisible(true);
    }
}
