package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.PlanCreationViewModel;
import placefinder.usecases.buildplan.BuildPlanOutputBoundary;
import placefinder.usecases.buildplan.BuildPlanOutputData;

public class BuildPlanPresenter implements BuildPlanOutputBoundary {

    private final PlanCreationViewModel viewModel;

    public BuildPlanPresenter(PlanCreationViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(BuildPlanOutputData outputData) {
        if (outputData.getErrorMessage() != null) {
            viewModel.setPlanPreview(null);
            viewModel.setErrorMessage(outputData.getErrorMessage());
            return;
        }
        viewModel.setPlanPreview(outputData.getPlan());
    }
}
