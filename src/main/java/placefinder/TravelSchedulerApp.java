package placefinder;

import javax.swing.SwingUtilities;

import placefinder.frameworks_drivers.api.GoogleMapsRouteGatewayImpl;
import placefinder.frameworks_drivers.database.Database;
import placefinder.frameworks_drivers.dataaccess.SqliteUserDataAccess;
import placefinder.frameworks_drivers.dataaccess.SqlitePreferenceDataAccess;
import placefinder.frameworks_drivers.dataaccess.SqlitePlanDataAccess;
import placefinder.frameworks_drivers.dataaccess.SmtpEmailDataAccess;
import placefinder.frameworks_drivers.dataaccess.EmailConfig;

import placefinder.frameworks_drivers.api.OpenCageGeocodingGateway;
import placefinder.frameworks_drivers.api.GeoApifyGatewayImpl;
import placefinder.frameworks_drivers.api.OpenMeteoWeatherGatewayImpl;

import placefinder.usecases.dataacessinterfaces.*;
import placefinder.usecases.favouritelocation.AddFavoriteInputBoundary;
import placefinder.usecases.favouritelocation.AddFavoriteInteractor;
import placefinder.usecases.favouritelocation.DeleteFavoriteInputBoundary;
import placefinder.usecases.favouritelocation.DeleteFavoriteInteractor;

// login & register
import placefinder.usecases.login.*;
import placefinder.usecases.register.*;

// preferences
import placefinder.usecases.preferences.*;

// search places + build/save plan
import placefinder.usecases.searchplaces.*;
import placefinder.usecases.buildplan.*;
import placefinder.usecases.saveplan.*;

// plans
import placefinder.usecases.plans.*;
import placefinder.usecases.getplandetails.*;
import placefinder.usecases.deleteplan.*;
import placefinder.usecases.listplans.*;

// weather advice
import placefinder.usecases.weatheradvice.*;

// verify email
import placefinder.usecases.verify.*;

// logging
import placefinder.usecases.logging.SwitchablePlacesLogger;

// interface adapters
import placefinder.interface_adapters.controllers.*;
import placefinder.interface_adapters.viewmodels.*;
import placefinder.interface_adapters.presenters.*;

import placefinder.frameworks_drivers.view.frames.AppFrame;
import placefinder.frameworks_drivers.view.frames.SplashScreen;

public class TravelSchedulerApp {

    public static void main(String[] args) {
        System.out.println("Java version = " + System.getProperty("java.version"));

        // Ensure database is initialized (triggers static init)
        try {
            Database.getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ========== GATEWAYS (Frameworks & Drivers) ==========
        UserDataAccessInterface userDataAccessInterface = new SqliteUserDataAccess();
        PreferenceDataAccessInterface preferenceDataAccessInterface = new SqlitePreferenceDataAccess();
        PlanDataAccessInterface planDataAccessInterface = new SqlitePlanDataAccess();
        GeocodingDataAccessInterface geocodingDataAccessInterface = new OpenCageGeocodingGateway();

        // Create switchable logger - starts with console logging enabled
        SwitchablePlacesLogger placesLogger = new SwitchablePlacesLogger(true);
        //  PlacesApiLogger placesLogger = new ConsolePlacesLogger();
        // PlacesApiLogger placesLogger = new InactivePlacesLogger();

        PlacesDataAccessInterface placesDataAccessInterface = new GeoApifyGatewayImpl(placesLogger);
        RouteDataAccessInterface routeDataAccessInterface = new GoogleMapsRouteGatewayImpl();
        WeatherDataAccessInterface weatherDataAccessInterface = new OpenMeteoWeatherGatewayImpl();

        EmailConfig emailConfig = new EmailConfig();
        EmailDataAccessInterface emailDataAccessInterface = new SmtpEmailDataAccess(
                emailConfig.getUsername(),
                emailConfig.getPassword()
        );

        // ========== VIEW MODELS ==========
        LoginViewModel loginVM = new LoginViewModel();
        RegisterViewModel registerVM = new RegisterViewModel();
        VerifyEmailViewModel verifyVM = new VerifyEmailViewModel();
        PreferencesViewModel preferencesVM = new PreferencesViewModel();
        PlanCreationViewModel planCreationVM = new PlanCreationViewModel();
        DashboardViewModel dashboardVM = new DashboardViewModel();
        PlanDetailsViewModel planDetailsVM = new PlanDetailsViewModel();
        WeatherAdviceViewModel weatherAdviceVM = new WeatherAdviceViewModel();

        // ========== PRESENTERS & INTERACTORS & CONTROLLERS ==========

        // ---- Login ----
        LoginPresenter loginPresenter = new LoginPresenter(loginVM);
        LoginInputBoundary loginInteractor =
                new LoginInteractor(userDataAccessInterface, loginPresenter);
        LoginController loginController =
                new LoginController(loginInteractor, loginVM);

        // ---- Register (uses EmailGateway) ----
        RegisterPresenter registerPresenter = new RegisterPresenter(registerVM);
        RegisterInputBoundary registerInteractor =
                new RegisterInteractor(userDataAccessInterface, registerPresenter, emailDataAccessInterface);
        RegisterController registerController =
                new RegisterController(registerInteractor, registerVM);

        // ---- Verify Email ----
        VerifyEmailPresenter verifyPresenter = new VerifyEmailPresenter(verifyVM);
        VerifyEmailInputBoundary verifyInteractor =
                new VerifyEmailInteractor(userDataAccessInterface, verifyPresenter);
        VerifyEmailController verifyController =
                new VerifyEmailController(verifyInteractor, verifyVM);

        // ---- Preferences (Get / Update / Add / Delete Favorite) ----
        PreferencesPresenter preferencesPresenter = new PreferencesPresenter(preferencesVM);

        GetPreferencesInputBoundary getPrefsInteractor =
                new GetPreferencesInteractor(preferenceDataAccessInterface, preferencesPresenter);
        UpdatePreferencesInputBoundary updatePrefsInteractor =
                new UpdatePreferencesInteractor(preferenceDataAccessInterface, preferencesPresenter);
        AddFavoriteInputBoundary addFavoriteInteractor =
                new AddFavoriteInteractor(preferenceDataAccessInterface, geocodingDataAccessInterface, preferencesPresenter);
        DeleteFavoriteInputBoundary deleteFavoriteInteractor =
                new DeleteFavoriteInteractor(preferenceDataAccessInterface, preferencesPresenter);

        PreferencesController preferencesController = new PreferencesController(
                getPrefsInteractor,
                updatePrefsInteractor,
                addFavoriteInteractor,
                deleteFavoriteInteractor,
                preferencesVM
        );

        // ---- Search Places / Build Plan / Save Plan ----
        SearchPlacesPresenter searchPlacesPresenter = new SearchPlacesPresenter(planCreationVM);
        BuildPlanPresenter buildPlanPresenter = new BuildPlanPresenter(planCreationVM);
        SavePlanPresenter savePlanPresenter = new SavePlanPresenter(planCreationVM);

        SearchPlacesInputBoundary searchPlacesInteractor =
                new SearchPlacesInteractor(
                        preferenceDataAccessInterface,
                        geocodingDataAccessInterface,
                        placesDataAccessInterface,
                        weatherDataAccessInterface,
                        searchPlacesPresenter
                );

        BuildPlanInputBoundary buildPlanInteractor =
                new BuildPlanInteractor(
                        preferenceDataAccessInterface,
                        geocodingDataAccessInterface,
                        routeDataAccessInterface,
                        buildPlanPresenter
                );

        SavePlanInputBoundary savePlanInteractor =
                new SavePlanInteractor(
                        planDataAccessInterface,
                        savePlanPresenter
                );

        SearchPlacesController searchPlacesController =
                new SearchPlacesController(searchPlacesInteractor, planCreationVM);
        BuildPlanController buildPlanController =
                new BuildPlanController(buildPlanInteractor, planCreationVM);
        SavePlanController savePlanController =
                new SavePlanController(savePlanInteractor, planCreationVM);

        PlanCreationController planCreationController =
                new PlanCreationController(
                        searchPlacesController,
                        buildPlanController,
                        savePlanController
                );

        // ---- Plans Dashboard / Details / Delete / Apply Prefs ----
        ListPlansPresenter listPlansPresenter = new ListPlansPresenter(dashboardVM);
        DeletePlanPresenter deletePlanPresenter = new DeletePlanPresenter(dashboardVM);
        ApplyPreferencesFromPlanPresenter applyPreferencesFromPlanPresenter =
                new ApplyPreferencesFromPlanPresenter(dashboardVM);
        GetPlanDetailsPresenter getPlanDetailsPresenter =
                new GetPlanDetailsPresenter(planDetailsVM);

        ListPlansInputBoundary listPlansInteractor =
                new ListPlansInteractor(planDataAccessInterface, listPlansPresenter);
        DeletePlanInputBoundary deletePlanInteractor =
                new DeletePlanInteractor(planDataAccessInterface, deletePlanPresenter);
        ApplyPreferencesFromPlanInputBoundary applyPrefsFromPlanInteractor =
                new ApplyPreferencesFromPlanInteractor(
                        planDataAccessInterface,
                        preferenceDataAccessInterface,
                        applyPreferencesFromPlanPresenter
                );
        GetPlanDetailsInputBoundary getPlanDetailsInteractor =
                new GetPlanDetailsInteractor(planDataAccessInterface, getPlanDetailsPresenter);

        ListPlansController listPlansController = new ListPlansController(
                listPlansInteractor,
                dashboardVM
        );
        DeletePlanController deletePlanController = new DeletePlanController(
                deletePlanInteractor,
                dashboardVM
        );
        ApplyPreferencesFromPlanController applyPreferencesFromPlanController =
                new ApplyPreferencesFromPlanController(
                        applyPrefsFromPlanInteractor,
                        dashboardVM
                );
        GetPlanDetailsController getPlanDetailsController =
                new GetPlanDetailsController(
                        getPlanDetailsInteractor,
                        planDetailsVM
                );

        // ---- Weather Advice ----
        WeatherAdvicePresenter weatherAdvicePresenter = new WeatherAdvicePresenter(weatherAdviceVM);

        WeatherAdviceInputBoundary weatherAdviceInteractor =
                new WeatherAdviceInteractor(geocodingDataAccessInterface, weatherDataAccessInterface, weatherAdvicePresenter);
        WeatherAdviceController weatherAdviceController =
                new WeatherAdviceController(weatherAdviceInteractor, weatherAdviceVM);

        // ========== START UI ==========
        SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen();

            AppFrame frame = new AppFrame(
                    loginController,
                    registerController,
                    verifyController,
                    preferencesController,
                    planCreationController,
                    listPlansController,
                    deletePlanController,
                    applyPreferencesFromPlanController,
                    getPlanDetailsController,
                    weatherAdviceController,
                    loginVM,
                    registerVM,
                    verifyVM,
                    preferencesVM,
                    planCreationVM,
                    dashboardVM,
                    planDetailsVM,
                    weatherAdviceVM,
                    placesLogger
            );

            splash.showSplash(() -> frame.setVisible(true));
        });
    }
}
