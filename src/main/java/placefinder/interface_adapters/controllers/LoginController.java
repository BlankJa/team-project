package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.LoginViewModel;
import placefinder.usecases.login.*;

/**
 * Controller for the login use case.
 * Coordinates between the UI and the login interactor, and updates the view model.
 */
public class LoginController implements LoginOutputBoundary {

    private final LoginInputBoundary interactor;
    private final LoginViewModel viewModel;

    public LoginController(LoginInputBoundary interactor, LoginViewModel viewModel) {
        this.interactor = interactor;
        this.viewModel = viewModel;
    }

    public void login(String email, String password) {
        viewModel.setErrorMessage(null);
        viewModel.setLoggedInUser(null);
        interactor.execute(new LoginInputData(email, password));
    }

    @Override
    public void present(LoginOutputData outputData) {
        if (outputData.isSuccess()) {
            viewModel.setLoggedInUser(outputData.getUser());
            viewModel.setErrorMessage(null);
        } else {
            viewModel.setLoggedInUser(null);
            viewModel.setErrorMessage(outputData.getMessage());
        }
    }

    public LoginViewModel getViewModel() { return viewModel; }
}
