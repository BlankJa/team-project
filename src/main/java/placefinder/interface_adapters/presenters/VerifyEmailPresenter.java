package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.VerifyEmailViewModel;
import placefinder.usecases.verify.VerifyEmailOutputBoundary;
import placefinder.usecases.verify.VerifyEmailOutputData;

public class VerifyEmailPresenter implements VerifyEmailOutputBoundary {

    private final VerifyEmailViewModel viewModel;

    public VerifyEmailPresenter(VerifyEmailViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(VerifyEmailOutputData outputData) {
        viewModel.setSuccess(outputData.isSuccess());
        viewModel.setMessage(outputData.getMessage());
    }
}
