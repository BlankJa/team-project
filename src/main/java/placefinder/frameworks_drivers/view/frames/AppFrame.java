package placefinder.frameworks_drivers.view.frames;

import placefinder.entities.Plan;

// Controllers
import placefinder.interface_adapters.controllers.LoginController;
import placefinder.interface_adapters.controllers.RegisterController;
import placefinder.interface_adapters.controllers.VerifyEmailController;
import placefinder.interface_adapters.controllers.PreferencesController;
import placefinder.interface_adapters.controllers.PlanCreationController;
import placefinder.interface_adapters.controllers.DashboardController;
import placefinder.interface_adapters.controllers.WeatherAdviceController;

// ViewModels
import placefinder.interface_adapters.viewmodels.LoginViewModel;
import placefinder.interface_adapters.viewmodels.RegisterViewModel;
import placefinder.interface_adapters.viewmodels.VerifyEmailViewModel;
import placefinder.interface_adapters.viewmodels.PreferencesViewModel;
import placefinder.interface_adapters.viewmodels.PlanCreationViewModel;
import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.interface_adapters.viewmodels.PlanDetailsViewModel;
import placefinder.interface_adapters.viewmodels.WeatherAdviceViewModel;

import javax.swing.*;
import java.awt.*;

/**
 * Main UI frame of PlaceFinder / TravelScheduler.
 *
 * Responsibilities:
 * - Holds and switches between UI screens using CardLayout.
 * - Receives controllers + view models from main application (TravelSchedulerApp).
 * - Controls navigation (Login → Register → Dashboard → Preferences → Plan → Weather → Plan Details).
 * - Stores active session state (current user).
 */
public class AppFrame extends JFrame {

    // ==== Controllers ====
    private final LoginController loginController;
    private final RegisterController registerController;
    private final VerifyEmailController verifyEmailController;
    private final PreferencesController preferencesController;
    private final PlanCreationController planCreationController;
    private final DashboardController dashboardController;
    private final WeatherAdviceController weatherAdviceController;

    // ==== ViewModels ====
    private final LoginViewModel loginVM;
    private final RegisterViewModel registerVM;
    private final VerifyEmailViewModel verifyVM;
    private final PreferencesViewModel preferencesVM;
    private final PlanCreationViewModel planCreationVM;
    private final DashboardViewModel dashboardVM;
    private final PlanDetailsViewModel planDetailsVM;
    private final WeatherAdviceViewModel weatherAdviceVM;

    // ==== Session state ====
    private Integer currentUserId = null;
    private String currentUserName = null;

    // ==== Layout ====
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // ==== Screen names ====
    public static final String CARD_LOGIN        = "login";
    public static final String CARD_REGISTER     = "register";
    public static final String CARD_DASHBOARD    = "dashboard";
    public static final String CARD_PREFERENCES  = "preferences";
    public static final String CARD_PLAN         = "plan";
    public static final String CARD_WEATHER      = "weather";
    public static final String CARD_PLAN_DETAILS = "planDetails";

    // ==== Panels ====
    private LoginPanel        loginPanel;
    private RegisterPanel     registerPanel;
    private DashboardPanel    dashboardPanel;
    private PreferencesPanel  preferencesPanel;
    private PlanBuilderPanel  planBuilderPanel;
    private WeatherAdvicePanel weatherAdvicePanel;
    private PlanDetailsPanel  planDetailsPanel;

    /**
     * Full constructor — receives all dependencies from TravelSchedulerApp.
     */
    public AppFrame(
            LoginController loginController,
            RegisterController registerController,
            VerifyEmailController verifyEmailController,
            PreferencesController preferencesController,
            PlanCreationController planCreationController,
            DashboardController dashboardController,
            WeatherAdviceController weatherAdviceController,
            LoginViewModel loginVM,
            RegisterViewModel registerVM,
            VerifyEmailViewModel verifyVM,
            PreferencesViewModel preferencesVM,
            PlanCreationViewModel planCreationVM,
            DashboardViewModel dashboardVM,
            PlanDetailsViewModel planDetailsVM,
            WeatherAdviceViewModel weatherAdviceVM
    ) {
        super("PlaceFinder");

        // Controllers
        this.loginController        = loginController;
        this.registerController     = registerController;
        this.verifyEmailController  = verifyEmailController;
        this.preferencesController  = preferencesController;
        this.planCreationController = planCreationController;
        this.dashboardController    = dashboardController;
        this.weatherAdviceController = weatherAdviceController;

        // ViewModels
        this.loginVM        = loginVM;
        this.registerVM     = registerVM;
        this.verifyVM       = verifyVM;
        this.preferencesVM  = preferencesVM;
        this.planCreationVM = planCreationVM;
        this.dashboardVM    = dashboardVM;
        this.planDetailsVM  = planDetailsVM;
        this.weatherAdviceVM = weatherAdviceVM;

        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);

        // ===== Instantiate screens with proper dependencies =====

        // Login screen
        loginPanel = new LoginPanel(this, loginController, loginVM);

        // Register screen – includes verify email support
        registerPanel = new RegisterPanel(
                this,
                registerController,
                registerVM,
                verifyEmailController,
                verifyVM
        );

        // Dashboard
        dashboardPanel = new DashboardPanel(
                this,
                dashboardController,
                dashboardVM,
                planDetailsVM
        );

        // Preferences
        preferencesPanel = new PreferencesPanel(
                this,
                preferencesController,
                preferencesVM
        );

        // Plan builder (with weather-aware recommendations)
        planBuilderPanel = new PlanBuilderPanel(
                this,
                planCreationController,
                planCreationVM,
                weatherAdviceController,
                weatherAdviceVM
        );

        // Standalone Weather Advice screen
        weatherAdvicePanel = new WeatherAdvicePanel(
                this,
                weatherAdviceController,
                weatherAdviceVM
        );

        // Plan details
        planDetailsPanel = new PlanDetailsPanel(
                dashboardController,
                dashboardVM,
                planDetailsVM,
                this
        );

        // ===== Register screens into CardLayout =====
        mainPanel.add(loginPanel,        CARD_LOGIN);
        mainPanel.add(registerPanel,     CARD_REGISTER);
        mainPanel.add(dashboardPanel,    CARD_DASHBOARD);
        mainPanel.add(preferencesPanel,  CARD_PREFERENCES);
        mainPanel.add(planBuilderPanel,  CARD_PLAN);
        mainPanel.add(weatherAdvicePanel,CARD_WEATHER);
        mainPanel.add(planDetailsPanel,  CARD_PLAN_DETAILS);

        setContentPane(mainPanel);
        showLogin();
    }

    // ===== Card switching helper =====
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

    public void showDashboard() {
        if (currentUserId != null) {
            dashboardPanel.refreshPlans();
        }
        showCard(CARD_DASHBOARD);
    }

    public void showPreferences() {
        preferencesPanel.loadForCurrentUser();
        showCard(CARD_PREFERENCES);
    }

    public void showNewPlan() {
        planBuilderPanel.setupForNewPlan();
        showCard(CARD_PLAN);
    }

    public void showWeatherAdvice() {
        weatherAdvicePanel.resetFields();
        showCard(CARD_WEATHER);
    }

    public void showPlanDetails() {
        planDetailsPanel.showFromViewModel();
        showCard(CARD_PLAN_DETAILS);
    }

    public void openPlanEditorWithPlan(Plan plan) {
        planBuilderPanel.editExistingPlan(plan);
        showCard(CARD_PLAN);
    }

    // ===== Session management =====

    public void onLoginSuccess() {
        if (loginVM.getLoggedInUser() != null) {
            currentUserId   = loginVM.getLoggedInUser().getId();
            currentUserName = loginVM.getLoggedInUser().getName();
            // After successful login, go to dashboard (as before)
            showDashboard();
        }
    }

    public void logout() {
        currentUserId   = null;
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
