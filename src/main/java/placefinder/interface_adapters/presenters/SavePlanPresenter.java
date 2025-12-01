package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.PlanCreationViewModel;
import placefinder.usecases.saveplan.SavePlanOutputBoundary;
import placefinder.usecases.saveplan.SavePlanOutputData;

public class SavePlanPresenter implements SavePlanOutputBoundary {

    private final PlanCreationViewModel viewModel;

    public SavePlanPresenter(PlanCreationViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(SavePlanOutputData outputData) {
        if (outputData.isSuccess()) {
            viewModel.setLastSavedPlan(outputData.getPlan());
            viewModel.setInfoMessage(outputData.getMessage());
            viewModel.setErrorMessage(null);
        } else {
            viewModel.setErrorMessage(outputData.getMessage());
        }
    }
}
