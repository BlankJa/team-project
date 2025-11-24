package placefinder;

import javax.swing.SwingUtilities;

import placefinder.frameworks_drivers.database.Database;
import placefinder.frameworks_drivers.database.SqliteUserGatewayImpl;

import placefinder.usecases.ports.UserGateway;

// login & register
import placefinder.usecases.login.*;
import placefinder.usecases.register.*;

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

        // ========== GATEWAYS (Frameworks & Drivers) ==========
        UserGateway userGateway = new SqliteUserGatewayImpl();

        // ========== VIEW MODELS ==========
        LoginViewModel loginVM = new LoginViewModel();
        RegisterViewModel registerVM = new RegisterViewModel();

        // ========== PRESENTERS & INTERACTORS & CONTROLLERS ==========

        // ---- Login ----
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

        // ---- Register ----
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

        // ========== START UI ==========
        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame(
                    loginController,
                    registerController,
                    loginVM,
                    registerVM
            );
            frame.setVisible(true);
        });
    }
}
