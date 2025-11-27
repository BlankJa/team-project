package placefinder.frameworks_drivers.view.frames;

import placefinder.interface_adapters.controllers.LoginController;
import placefinder.interface_adapters.controllers.RegisterController;
import placefinder.interface_adapters.controllers.VerifyEmailController;
import placefinder.interface_adapters.controllers.PreferencesController;
import placefinder.interface_adapters.viewmodels.LoginViewModel;
import placefinder.interface_adapters.viewmodels.RegisterViewModel;
import placefinder.interface_adapters.viewmodels.VerifyEmailViewModel;
import placefinder.interface_adapters.viewmodels.PreferencesViewModel;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame for PlaceFinder / TravelScheduler.
 * - Manages the card layout for Login, Register, and Preferences screens.
 * - Handles session management and navigation between screens.
 * - Supports email verification (used inside RegisterPanel) and preferences.
 */
public class AppFrame extends JFrame {

    // ===== Controllers =====
    private final LoginController loginController;
    private final RegisterController registerController;
    private final VerifyEmailController verifyEmailController;
    private final PreferencesController preferencesController;

    // ===== ViewModels =====
    private final LoginViewModel loginVM;
    private final RegisterViewModel registerVM;
    private final VerifyEmailViewModel verifyVM;
    private final PreferencesViewModel preferencesVM;

    // ===== Session state =====
    private Integer currentUserId = null;
    private String currentUserName = null;

    // ===== Layout =====
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
            VerifyEmailController verifyEmailController,
            PreferencesController preferencesController,
            LoginViewModel loginVM,
            RegisterViewModel registerVM,
            VerifyEmailViewModel verifyVM,
            PreferencesViewModel preferencesVM
    ) {
        super("PlaceFinder");

        this.loginController = loginController;
        this.registerController = registerController;
        this.verifyEmailController = verifyEmailController;
        this.preferencesController = preferencesController;

        this.loginVM = loginVM;
        this.registerVM = registerVM;
        this.verifyVM = verifyVM;
        this.preferencesVM = preferencesVM;

        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Login panel (same as before)
        loginPanel = new LoginPanel(this, loginController, loginVM);

        // Register panel – NOTE: uses verifyEmailController + verifyVM
        registerPanel = new RegisterPanel(
                this,
                registerController,
                registerVM,
                verifyEmailController,
                verifyVM
        );

        // Preferences panel – uses preferencesController + preferencesVM
        preferencesPanel = new PreferencesPanel(
                this,
                preferencesController,
                preferencesVM
        );

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
        // Let the preferences screen refresh based on the current user
        preferencesPanel.loadForCurrentUser();
        showCard(CARD_PREFERENCES);
    }

    // ===== Session management =====

    public void onLoginSuccess() {
        if (loginVM.getLoggedInUser() != null) {
            currentUserId = loginVM.getLoggedInUser().getId();
            currentUserName = loginVM.getLoggedInUser().getName();
            // After a successful login, go straight to preferences screen
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