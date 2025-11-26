package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.VerifyEmailViewModel;
import placefinder.usecases.verify.VerifyEmailInputBoundary;
import placefinder.usecases.verify.VerifyEmailInputData;
import placefinder.usecases.verify.VerifyEmailOutputBoundary;
import placefinder.usecases.verify.VerifyEmailOutputData;

public class VerifyEmailController {

    private final VerifyEmailInputBoundary interactor;
    private final VerifyEmailViewModel viewModel;

    public VerifyEmailController(VerifyEmailInputBoundary interactor,
                                 VerifyEmailViewModel viewModel) {
        this.interactor = interactor;
        this.viewModel = viewModel;
    }

    public void verify(String email, String code) {
        VerifyEmailInputData inputData = new VerifyEmailInputData(email, code);
        interactor.execute(inputData);
    }

    public VerifyEmailViewModel getViewModel() {
        return viewModel;
    }

    // 🔥 This is the missing correct factory Presenter method
    public static VerifyEmailOutputBoundary createPresenter(VerifyEmailViewModel vm) {
        return new VerifyEmailOutputBoundary() {
            @Override
            public void present(VerifyEmailOutputData outputData) {
                vm.setSuccess(outputData.isSuccess());
                vm.setMessage(outputData.getMessage());
            }
        };
    }
}