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
            viewModel.setPlanTruncated(false);
            viewModel.setErrorMessage(outputData.getErrorMessage());
            return;
        }

        viewModel.setPlanPreview(outputData.getPlan());
        viewModel.setPlanTruncated(outputData.isTruncated());

        if (outputData.isTruncated()) {
            viewModel.setInfoMessage("Plan exceeds available time; some places were not included.");
        } else {
            viewModel.setInfoMessage(null);
        }
    }
}
