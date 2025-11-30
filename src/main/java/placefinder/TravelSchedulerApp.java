package placefinder;

import javax.swing.SwingUtilities;

import placefinder.frameworks_drivers.database.Database;
import placefinder.frameworks_drivers.database.SqliteUserGatewayImpl;
import placefinder.frameworks_drivers.database.SqlitePreferenceGatewayImpl;
import placefinder.frameworks_drivers.database.SmtpEmailGateway;

import placefinder.usecases.ports.UserGateway;
import placefinder.usecases.ports.PreferenceGateway;
import placefinder.usecases.ports.EmailGateway;
import placefinder.usecases.ports.PlacesGateway;

import placefinder.frameworks_drivers.api.GeoapifyPlacesGateway;
import placefinder.usecases.logging.PlacesApiLogger;
import placefinder.usecases.logging.ConsolePlacesLogger;
import placefinder.usecases.logging.InactivePlacesLogger;

// login & register
import placefinder.usecases.login.*;
import placefinder.usecases.register.*;

// preferences
import placefinder.usecases.preferences.*;

// verify email
import placefinder.usecases.verify.*;

// controllers + viewmodels + presenters
import placefinder.interface_adapters.controllers.*;
import placefinder.interface_adapters.viewmodels.*;
import placefinder.interface_adapters.presenters.*;

// UI
import placefinder.frameworks_drivers.view.frames.AppFrame;

/**
 * Main application class for PlaceFinder / TravelScheduler.
 */
public class TravelSchedulerApp {

    public static void main(String[] args) {

        // Ensure database is initialized (triggers static init)
        try {
            Database.getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ========== API DEBUG CONFIGURATION ==========
        // CHANGE THIS TO true TO SEE API LOGS, false TO HIDE THEM
        boolean debugApiCalls = true;  // <- Toggle logging here

        PlacesApiLogger apiLogger = debugApiCalls
                ? new ConsolePlacesLogger()   // Shows detailed API debug logs
                : new InactivePlacesLogger();  // No logging (production mode)

        System.out.println("API Debug Mode: " + (debugApiCalls ? "ON" : "OFF"));
        // =============================================

        // ========== GATEWAYS (Frameworks & Drivers) ==========
        UserGateway userGateway = new SqliteUserGatewayImpl();
        PreferenceGateway preferenceGateway = new SqlitePreferenceGatewayImpl();

        // Sender email account for verification codes
        EmailGateway emailGateway = new SmtpEmailGateway(
                "subhan.akbar908@gmail.com",    // your Gmail address
                "eqrsbydralnvylzm"              // your 16-char app password
        );

        // Places API with optional debugging (configured above)
        PlacesGateway placesGateway = new GeoapifyPlacesGateway(
                "YOUR_GEOAPIFY_API_KEY_HERE",   // Get free key at Geoapify
                apiLogger                       // Uses the logger configured above
        );
        // (placesGateway is ready to be used by future use cases)

        // ========== VIEW MODELS ==========
        LoginViewModel loginVM = new LoginViewModel();
        RegisterViewModel registerVM = new RegisterViewModel();
        VerifyEmailViewModel verifyVM = new VerifyEmailViewModel();
        PreferencesViewModel preferencesVM = new PreferencesViewModel();

        // ========== PRESENTERS, INTERACTORS, CONTROLLERS ==========

        // ---- Login ----
        LoginPresenter loginPresenter = new LoginPresenter(loginVM);
        LoginInputBoundary loginInteractor =
                new LoginInteractor(userGateway, loginPresenter);
        LoginController loginController =
                new LoginController(loginInteractor, loginVM);

        // ---- Register (with email gateway) ----
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

        // ---- Preferences (Get + Update via single presenter) ----
        PreferencesPresenter preferencesPresenter = new PreferencesPresenter(preferencesVM);

        GetPreferencesInputBoundary getPrefsInteractor =
                new GetPreferencesInteractor(preferenceGateway, preferencesPresenter);
        UpdatePreferencesInputBoundary updatePrefsInteractor =
                new UpdatePreferencesInteractor(preferenceGateway, preferencesPresenter);

        PreferencesController preferencesController = new PreferencesController(
                getPrefsInteractor,
                updatePrefsInteractor,
                preferencesVM
        );

        // ========== START UI ==========
        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame(
                    loginController,
                    registerController,
                    verifyController,
                    preferencesController,
                    loginVM,
                    registerVM,
                    verifyVM,
                    preferencesVM
            );
            frame.setVisible(true);
        });
    }
}
