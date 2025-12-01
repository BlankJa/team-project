package placefinder;

import javax.swing.SwingUtilities;

// NOTE: JavaFX is still used in your UI layer (e.g., SplashScreen via JFXPanel).
// We deliberately do NOT call Platform.startup(...) here to avoid thread issues.
// JavaFX should be initialized inside components using JFXPanel + Platform.runLater
// on the JavaFX Application Thread.

import placefinder.frameworks_drivers.database.Database;
import placefinder.frameworks_drivers.dataaccess.SqliteUserDataAccess;
import placefinder.frameworks_drivers.dataaccess.SqlitePreferenceDataAccess;
import placefinder.frameworks_drivers.dataaccess.SqlitePlanDataAccess;
import placefinder.frameworks_drivers.dataaccess.SmtpEmailDataAccess;

import placefinder.frameworks_drivers.api.OpenCageGeocodingGateway;
import placefinder.frameworks_drivers.api.GeoApifyGatewayImpl;
import placefinder.frameworks_drivers.api.OpenMeteoWeatherGatewayImpl;

import placefinder.usecases.favouritelocation.AddFavoriteInputBoundary;
import placefinder.usecases.favouritelocation.AddFavoriteInteractor;
import placefinder.usecases.favouritelocation.DeleteFavoriteInputBoundary;
import placefinder.usecases.favouritelocation.DeleteFavoriteInteractor;
import placefinder.usecases.dataacessinterfaces.UserDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.GeocodingDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PlacesDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.WeatherDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.EmailDataAccessInterface;

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

// interface adapters
import placefinder.interface_adapters.controllers.*;
import placefinder.interface_adapters.viewmodels.*;
import placefinder.interface_adapters.presenters.*;

// UI
import placefinder.frameworks_drivers.view.frames.AppFrame;
import placefinder.frameworks_drivers.view.frames.SplashScreen;

public class TravelSchedulerApp {

    public static void main(String[] args) {

        // OPTIONAL: print Java version so you can confirm you're on 21
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
        PlacesDataAccessInterface placesDataAccessInterface = new GeoApifyGatewayImpl();
        WeatherDataAccessInterface weatherDataAccessInterface = new OpenMeteoWeatherGatewayImpl();

        EmailDataAccessInterface emailDataAccessInterface = new SmtpEmailDataAccess(
                "subhanakbar908@gmail.com","eqrsbydralnvylzm"
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
        PlanCreationPresenter planCreationPresenter = new PlanCreationPresenter(planCreationVM);

        SearchPlacesInputBoundary searchPlacesInteractor =
                new SearchPlacesInteractor(
                        preferenceDataAccessInterface,
                        geocodingDataAccessInterface,
                        placesDataAccessInterface,
                        weatherDataAccessInterface,
                        planCreationPresenter
                );

        BuildPlanInputBoundary buildPlanInteractor =
                new BuildPlanInteractor(preferenceDataAccessInterface, geocodingDataAccessInterface, planCreationPresenter);

        SavePlanInputBoundary savePlanInteractor =
                new SavePlanInteractor(planDataAccessInterface, planCreationPresenter);

        PlanCreationController planCreationController = new PlanCreationController(
                searchPlacesInteractor,
                buildPlanInteractor,
                savePlanInteractor,
                planCreationVM
        );

        // ---- Plans Dashboard / Details / Delete / Apply Prefs ----
        ListPlansPresenter listPlansPresenter = new ListPlansPresenter(dashboardVM);
        DeletePlanPresenter deletePlanPresenter = new DeletePlanPresenter(dashboardVM);
        ApplyPreferencesFromPlanPresenter applyPreferencesFromPlanPresenter =
                new ApplyPreferencesFromPlanPresenter(dashboardVM);
        GetPlanDetailsPresenter getPlanDetailsPresenter =
                new GetPlanDetailsPresenter(planDetailsVM);

        ListPlansInputBoundary listPlansInteractor =
                new ListPlansInteractor(planGateway, listPlansPresenter);
        DeletePlanInputBoundary deletePlanInteractor =
                new DeletePlanInteractor(planGateway, deletePlanPresenter);
        ApplyPreferencesFromPlanInputBoundary applyPrefsFromPlanInteractor =
                new ApplyPreferencesFromPlanInteractor(
                        planGateway,
                        preferenceGateway,
                        applyPreferencesFromPlanPresenter
                );
        GetPlanDetailsInputBoundary getPlanDetailsInteractor =
                new GetPlanDetailsInteractor(planGateway, getPlanDetailsPresenter);

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

        // ========== START UI (Swing + JavaFX) ==========
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
                    weatherAdviceVM
            );

            // Show splash screen, then show main window after it closes
            splash.showSplash(() -> frame.setVisible(true));
        });
    }
}