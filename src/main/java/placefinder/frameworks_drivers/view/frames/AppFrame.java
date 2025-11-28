package placefinder.frameworks_drivers.view.frames;

import placefinder.interface_adapters.controllers.*;
import placefinder.interface_adapters.viewmodels.*;

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
    private final PlanCreationController planCreationController;

    // ===== ViewModels =====
    private final LoginViewModel loginVM;
    private final RegisterViewModel registerVM;
    private final VerifyEmailViewModel verifyVM;
    private final PreferencesViewModel preferencesVM;
    private final PlanCreationViewModel planCreationVM;
    private final PlanDetailsViewModel planDetailsVM;

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
    public static final String CARD_PLAN = "plan";
    public static final String CARD_PLAN_DETAILS = "planDetails";

    // Panels
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private PreferencesPanel preferencesPanel;
    private PlanBuilderPanel planBuilderPanel;
    private PlanDetailsPanel planDetailsPanel;

    public AppFrame(
            LoginController loginController,
            RegisterController registerController,
            VerifyEmailController verifyEmailController,
            PreferencesController preferencesController,
            PlanCreationController planCreationController,
            LoginViewModel loginVM,
            RegisterViewModel registerVM,
            VerifyEmailViewModel verifyVM,
            PreferencesViewModel preferencesVM,
            PlanCreationViewModel planCreationVM,
            PlanDetailsViewModel planDetailsVM
    ) {
        super("PlaceFinder");

        this.loginController = loginController;
        this.registerController = registerController;
        this.verifyEmailController = verifyEmailController;
        this.preferencesController = preferencesController;
        this.planCreationController = planCreationController;

        this.loginVM = loginVM;
        this.registerVM = registerVM;
        this.verifyVM = verifyVM;
        this.preferencesVM = preferencesVM;
        this.planCreationVM = planCreationVM;
        this.planDetailsVM = planDetailsVM;

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

        // Plan Builder panel – uses planCreationController + planCreationVM
        planBuilderPanel = new PlanBuilderPanel(
                this,
                planCreationController,
                planCreationVM
        );

        mainPanel.add(loginPanel, CARD_LOGIN);
        mainPanel.add(registerPanel, CARD_REGISTER);
        mainPanel.add(preferencesPanel, CARD_PREFERENCES);
        mainPanel.add(planBuilderPanel, CARD_PLAN);
        mainPanel.add(planDetailsPanel, CARD_PLAN_DETAILS);

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

    public void showNewPlan() {
        planBuilderPanel.setupForNewPlan();
        showCard(CARD_PLAN);
    }

    public void showPlanDetails() {
        planDetailsPanel.showFromViewModel();
        showCard(CARD_PLAN_DETAILS);
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