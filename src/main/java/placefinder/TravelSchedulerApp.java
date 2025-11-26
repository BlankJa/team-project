package placefinder;

import javax.swing.SwingUtilities;

import placefinder.frameworks_drivers.database.Database;
import placefinder.frameworks_drivers.database.SqliteUserGatewayImpl;
import placefinder.frameworks_drivers.database.SqlitePreferenceGatewayImpl;

import placefinder.usecases.ports.UserGateway;
import placefinder.usecases.ports.PreferenceGateway;

// login & register
import placefinder.usecases.login.*;
import placefinder.usecases.register.*;

// preferences
import placefinder.usecases.preferences.*;

// interface adapters
import placefinder.interface_adapters.controllers.*;
import placefinder.interface_adapters.viewmodels.*;

// UI (you implement this in frameworks_drivers.ui)
import placefinder.frameworks_drivers.view.frames.AppFrame;

/**
 * Main application class for PlaceFinder.
 */
public class TravelSchedulerApp {

    public static void main(String[] args) {

        // Ensure database is initialized (triggers static init)
        try {
            Database.getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        UserGateway userGateway = new SqliteUserGatewayImpl();
        PreferenceGateway preferenceGateway = new SqlitePreferenceGatewayImpl();

        LoginViewModel loginVM = new LoginViewModel();
        RegisterViewModel registerVM = new RegisterViewModel();
        PreferencesViewModel preferencesVM = new PreferencesViewModel();


        LoginOutputBoundary loginPresenter = new LoginOutputBoundary() {
            @Override
            public void present(LoginOutputData outputData) {
                if (outputData.isSuccess()) {
                    loginVM.setLoggedInUser(outputData.getUser());
                    loginVM.setErrorMessage(null);
                } else {
                    loginVM.setLoggedInUser(null);
                    loginVM.setErrorMessage(outputData.getMessage());
                }
            }
        };
        LoginInputBoundary loginInteractor =
                new LoginInteractor(userGateway, loginPresenter);
        LoginController loginController =
                new LoginController(loginInteractor, loginVM);

        RegisterOutputBoundary registerPresenter = new RegisterOutputBoundary() {
            @Override
            public void present(RegisterOutputData outputData) {
                registerVM.setSuccess(outputData.isSuccess());
                registerVM.setMessage(outputData.getMessage());
            }
        };
        RegisterInputBoundary registerInteractor =
                new RegisterInteractor(userGateway, registerPresenter);
        RegisterController registerController =
                new RegisterController(registerInteractor, registerVM);

        GetPreferencesOutputBoundary getPrefsPresenter = new GetPreferencesOutputBoundary() {
            @Override
            public void present(GetPreferencesOutputData outputData) {
                if (outputData.getErrorMessage() != null) {
                    preferencesVM.setErrorMessage(outputData.getErrorMessage());
                    return;
                }
                preferencesVM.setRadiusKm(outputData.getRadiusKm());
                preferencesVM.setSelectedCategories(outputData.getSelectedCategories());
                preferencesVM.setFavorites(outputData.getFavorites());
            }
        };

        UpdatePreferencesOutputBoundary updatePrefsPresenter = new UpdatePreferencesOutputBoundary() {
            @Override
            public void present(UpdatePreferencesOutputData outputData) {
                if (outputData.isSuccess()) {
                    preferencesVM.setMessage(outputData.getMessage());
                    preferencesVM.setErrorMessage(null);
                } else {
                    preferencesVM.setErrorMessage(outputData.getMessage());
                }
            }
        };

        GetPreferencesInputBoundary getPrefsInteractor =
                new GetPreferencesInteractor(preferenceGateway, getPrefsPresenter);
        UpdatePreferencesInputBoundary updatePrefsInteractor =
                new UpdatePreferencesInteractor(preferenceGateway, updatePrefsPresenter);

        PreferencesController preferencesController = new PreferencesController(
                getPrefsInteractor,
                updatePrefsInteractor,
                preferencesVM
        );

        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame(
                    loginController,
                    registerController,
                    preferencesController,
                    loginVM,
                    registerVM,
                    preferencesVM
            );
            frame.setVisible(true);
        });
    }
}
