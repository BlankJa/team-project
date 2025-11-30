package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.RegisterViewModel;
import placefinder.usecases.register.RegisterOutputBoundary;
import placefinder.usecases.register.RegisterOutputData;

public class RegisterPresenter implements RegisterOutputBoundary{

    private final RegisterViewModel viewModel;

public RegisterPresenter(RegisterViewModel viewModel) {
    this.viewModel = viewModel;
}

@Override
    public void present(RegisterOutputData outputData) {
        viewModel.setSuccess(outputData.isSuccess());
        viewModel.setMessage(outputData.getMessage());
    }
}
