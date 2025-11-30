package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.LoginViewModel;
import placefinder.usecases.login.LoginOutputBoundary;
import placefinder.usecases.login.LoginOutputData;

public class LoginPresenter implements LoginOutputBoundary{

    private final LoginViewModel viewModel;

public LoginPresenter(LoginViewModel viewModel) {
    this.viewModel = viewModel;
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
}
