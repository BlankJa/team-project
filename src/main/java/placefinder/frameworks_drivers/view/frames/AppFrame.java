package placefinder.frameworks_drivers.view.frames;

import placefinder.interface_adapters.controllers.*;
import placefinder.interface_adapters.viewmodels.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame for PlaceFinder.
 * Manages the card layout for switching between login and registration panels.
 * Handles user session management and navigation between screens.
 */
public class AppFrame extends JFrame {

    // Controllers
    private final LoginController loginController;
    private final RegisterController registerController;
    private final PreferencesController preferencesController;

    // ViewModels
    private final LoginViewModel loginVM;
    private final RegisterViewModel registerVM;
    private final PreferencesViewModel preferencesVM;

    // Current user
    private Integer currentUserId = null;
    private String currentUserName = null;

    // Card layout for screens
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Screen names
    public static final String CARD_LOGIN = "login";
    public static final String CARD_REGISTER = "register";
    public static final String CARD_PREFERENCES = "preferences";

    // Panels
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private PreferencesPanel preferencesPanel;

    public AppFrame(
            LoginController loginController,
            RegisterController registerController,
            PreferencesController preferencesController,
            LoginViewModel loginVM,
            RegisterViewModel registerVM,
            PreferencesViewModel preferencesVM
    ) {
        super("PlaceFinder");

        this.loginController = loginController;
        this.registerController = registerController;
        this.preferencesController = preferencesController;

        this.loginVM = loginVM;
        this.registerVM = registerVM;
        this.preferencesVM = preferencesVM;

        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this, loginController, loginVM);
        registerPanel = new RegisterPanel(this, registerController, registerVM);
        preferencesPanel = new PreferencesPanel(this, preferencesController, preferencesVM);

        mainPanel.add(loginPanel, CARD_LOGIN);
        mainPanel.add(registerPanel, CARD_REGISTER);
        mainPanel.add(preferencesPanel, CARD_PREFERENCES);

        setContentPane(mainPanel);
        showLogin();
    }

    void showCard(String card) {
        cardLayout.show(mainPanel, card);
    }

    // ===== Navigation helpers =====

    public void showLogin() {
        showCard(CARD_LOGIN);
    }

    public void showRegister() {
        showCard(CARD_REGISTER);
    }

    public void showPreferences() {
        preferencesPanel.loadForCurrentUser();
        showCard(CARD_PREFERENCES);
    }

    // ===== Session management =====

    public void onLoginSuccess() {
        if (loginVM.getLoggedInUser() != null) {
            currentUserId = loginVM.getLoggedInUser().getId();
            currentUserName = loginVM.getLoggedInUser().getName();
            showPreferences();
        }
    }

    public void logout() {
        currentUserId = null;
        currentUserName = null;
        loginVM.setLoggedInUser(null);
        loginVM.setErrorMessage(null);
        showLogin();
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }
}
