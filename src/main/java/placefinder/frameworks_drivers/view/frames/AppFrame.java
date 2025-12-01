package placefinder.frameworks_drivers.view.frames;

import placefinder.interface_adapters.controllers.*;
import placefinder.interface_adapters.viewmodels.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main UI frame of PlaceFinder / TravelScheduler.
 *
 * Responsibilities:
 * - Holds and switches between UI screens using CardLayout.
 * - Receives controllers + view models from main application (TravelSchedulerApp).
 * - Controls navigation (Login → Register → Preferences → Create Plan → Plan Details).
 * - Stores active session state (current user).
 */
public class AppFrame extends JFrame {

    // ==== Controllers ====
    private final LoginController loginController;
    private final RegisterController registerController;
    private final VerifyEmailController verifyEmailController;
    private final PreferencesController preferencesController;
    private final PlanCreationController planCreationController;

    // ==== ViewModels ====
    private final LoginViewModel loginVM;
    private final RegisterViewModel registerVM;
    private final VerifyEmailViewModel verifyVM;
    private final PreferencesViewModel preferencesVM;
    private final PlanCreationViewModel planCreationVM;
    private final PlanDetailsViewModel planDetailsVM;

    // ==== Session state ====
    private Integer currentUserId = null;
    private String currentUserName = null;

    // ==== Layout ====
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // ==== Screen names ====
    public static final String CARD_LOGIN = "login";
    public static final String CARD_REGISTER = "register";
    public static final String CARD_PREFERENCES = "preferences";
    public static final String CARD_PLAN_CREATE = "planCreate";
    public static final String CARD_PLAN_DETAILS = "planDetails";

    // ==== Panels ====
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private PreferencesPanel preferencesPanel;
    private PlanBuilderPanel planBuilderPanel;
    private PlanDetailsPanel planDetailsPanel;

    /**
     * Full constructor — receives all dependencies from TravelSchedulerApp.
     * This keeps the UI framework-independent from business logic layers.
     */
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

        // Create screens with injected controllers + ViewModels
        loginPanel = new LoginPanel(this, loginController, loginVM);

        registerPanel = new RegisterPanel(
                this, registerController, registerVM, verifyEmailController, verifyVM
        );

        preferencesPanel = new PreferencesPanel(
                this, preferencesController, preferencesVM
        );

        planBuilderPanel = new PlanBuilderPanel(
                this, planCreationController, planCreationVM
        );

        planDetailsPanel = new PlanDetailsPanel(
                this, planDetailsVM
        );

        // Add screens into CardLayout container
        mainPanel.add(loginPanel, CARD_LOGIN);
        mainPanel.add(registerPanel, CARD_REGISTER);
        mainPanel.add(preferencesPanel, CARD_PREFERENCES);
        mainPanel.add(planBuilderPanel, CARD_PLAN_CREATE);
        mainPanel.add(planDetailsPanel, CARD_PLAN_DETAILS);

        setContentPane(mainPanel);
        showLogin();
    }

    // ===== Navigation =====

    public void showLogin() { showCard(CARD_LOGIN); }
    public void showRegister() { showCard(CARD_REGISTER); }

    public void showPreferences() {
        preferencesPanel.loadForCurrentUser(); // refresh preferences before showing
        showCard(CARD_PREFERENCES);
    }

    public void showPlanCreation() {
        planBuilderPanel.setupForNewPlan();
        showCard(CARD_PLAN_CREATE);
    }

    public void showPlanDetails() {
        planDetailsPanel.showFromViewModel();
        showCard(CARD_PLAN_DETAILS);
    }

    private void showCard(String name) {
        cardLayout.show(mainPanel, name);
    }

    // ===== Session Management =====

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
        showLogin();
    }

    public Integer getCurrentUserId() { return currentUserId; }
    public String getCurrentUserName() { return currentUserName; }
}