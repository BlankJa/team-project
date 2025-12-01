package placefinder;

import javax.swing.SwingUtilities;

import placefinder.frameworks_drivers.database.Database;
import placefinder.frameworks_drivers.database.SqliteUserGatewayImpl;
import placefinder.frameworks_drivers.database.SqlitePreferenceGatewayImpl;
import placefinder.frameworks_drivers.database.SqlitePlanGatewayImpl;
import placefinder.frameworks_drivers.database.SmtpEmailGateway;

import placefinder.frameworks_drivers.api.OpenCageGeocodingGateway;
import placefinder.frameworks_drivers.api.GeoApifyPlacesGatewayImpl;
import placefinder.frameworks_drivers.api.OpenMeteoWeatherGatewayImpl;

import placefinder.usecases.ports.UserGateway;
import placefinder.usecases.ports.PreferenceGateway;
import placefinder.usecases.ports.PlanGateway;
import placefinder.usecases.ports.GeocodingGateway;
import placefinder.usecases.ports.PlacesGateway;
import placefinder.usecases.ports.WeatherGateway;
import placefinder.usecases.ports.EmailGateway;

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

public class TravelSchedulerApp {

    public static void main(String[] args) {

        // Ensure database is initialized (triggers static init)
        try {
            Database.getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ========== GATEWAYS (Frameworks & Drivers) ==========
        UserGateway userGateway = new SqliteUserGatewayImpl();
        PreferenceGateway preferenceGateway = new SqlitePreferenceGatewayImpl();
        PlanGateway planGateway = new SqlitePlanGatewayImpl();
        GeocodingGateway geocodingGateway = new OpenCageGeocodingGateway();
        PlacesGateway placesGateway = new GeoApifyPlacesGatewayImpl();
        WeatherGateway weatherGateway = new OpenMeteoWeatherGatewayImpl();

        // Email gateway for registration / verification codes
        EmailGateway emailGateway = new SmtpEmailGateway(
                "subhan.akbar908@gmail.com",    // your Gmail
                "eqrsbydralnvylzm"              // your app password
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
                new LoginInteractor(userGateway, loginPresenter);
        LoginController loginController =
                new LoginController(loginInteractor, loginVM);

        // ---- Register (now uses EmailGateway) ----
        RegisterPresenter registerPresenter = new RegisterPresenter(registerVM);
        RegisterInputBoundary registerInteractor =
                new RegisterInteractor(userGateway, registerPresenter, emailGateway);
        RegisterController registerController =
                new RegisterController(registerInteractor, registerVM);

        // ---- Verify Email ----
        VerifyEmailPresenter verifyPresenter = new VerifyEmailPresenter(verifyVM);
        VerifyEmailInputBoundary verifyInteractor =
                new VerifyEmailInteractor(userGateway, verifyPresenter);
        VerifyEmailController verifyController =
                new VerifyEmailController(verifyInteractor, verifyVM);

        // ---- Preferences (Get / Update / Add / Delete Favorite) ----
        PreferencesPresenter preferencesPresenter = new PreferencesPresenter(preferencesVM);

        GetPreferencesInputBoundary getPrefsInteractor =
                new GetPreferencesInteractor(preferenceGateway, preferencesPresenter);
        UpdatePreferencesInputBoundary updatePrefsInteractor =
                new UpdatePreferencesInteractor(preferenceGateway, preferencesPresenter);
        AddFavoriteInputBoundary addFavoriteInteractor =
                new AddFavoriteInteractor(preferenceGateway, geocodingGateway, preferencesPresenter);
        DeleteFavoriteInputBoundary deleteFavoriteInteractor =
                new DeleteFavoriteInteractor(preferenceGateway, preferencesPresenter);

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
                        preferenceGateway,
                        geocodingGateway,
                        placesGateway,
                        weatherGateway,
                        planCreationPresenter
                );

        BuildPlanInputBoundary buildPlanInteractor =
                new BuildPlanInteractor(preferenceGateway, geocodingGateway, planCreationPresenter);

        SavePlanInputBoundary savePlanInteractor =
                new SavePlanInteractor(planGateway, planCreationPresenter);

        PlanCreationController planCreationController = new PlanCreationController(
                searchPlacesInteractor,
                buildPlanInteractor,
                savePlanInteractor,
                planCreationVM
        );

        // ---- Plans Dashboard / Details / Delete / Apply Prefs ----
        DashboardPresenter dashboardPresenter = new DashboardPresenter(dashboardVM, planDetailsVM);

        ListPlansInputBoundary listPlansInteractor =
                new ListPlansInteractor(planGateway, dashboardPresenter);
        DeletePlanInputBoundary deletePlanInteractor =
                new DeletePlanInteractor(planGateway, dashboardPresenter);
        ApplyPreferencesFromPlanInputBoundary applyPrefsFromPlanInteractor =
                new ApplyPreferencesFromPlanInteractor(
                        planGateway,
                        preferenceGateway,
                        dashboardPresenter
                );
        GetPlanDetailsInputBoundary getPlanDetailsInteractor =
                new GetPlanDetailsInteractor(planGateway, dashboardPresenter);

        DashboardController dashboardController = new DashboardController(
                listPlansInteractor,
                deletePlanInteractor,
                applyPrefsFromPlanInteractor,
                getPlanDetailsInteractor,
                dashboardVM,
                planDetailsVM
        );

        // ---- Weather Advice ----
        WeatherAdvicePresenter weatherAdvicePresenter = new WeatherAdvicePresenter(weatherAdviceVM);

        WeatherAdviceInputBoundary weatherAdviceInteractor =
                new WeatherAdviceInteractor(geocodingGateway, weatherGateway, weatherAdvicePresenter);
        WeatherAdviceController weatherAdviceController =
                new WeatherAdviceController(weatherAdviceInteractor, weatherAdviceVM);

        // ========== START UI ==========
        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame(
                    loginController,
                    registerController,
                    verifyController,
                    preferencesController,
                    planCreationController,
                    dashboardController,
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
            frame.setVisible(true);
        });
    }
}
