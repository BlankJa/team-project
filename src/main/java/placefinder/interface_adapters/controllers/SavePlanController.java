package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.PlanCreationViewModel;
import placefinder.usecases.saveplan.SavePlanInputBoundary;
import placefinder.usecases.saveplan.SavePlanInputData;

public class SavePlanController {

    private final SavePlanInputBoundary savePlanInteractor;
    private final PlanCreationViewModel viewModel;

    public SavePlanController(SavePlanInputBoundary savePlanInteractor,
                              PlanCreationViewModel viewModel) {
        this.savePlanInteractor = savePlanInteractor;
        this.viewModel = viewModel;
    }

    public void saveCurrentPlan(String name) {
        viewModel.setErrorMessage(null);
        viewModel.setInfoMessage(null);

        if (viewModel.getPlanPreview() == null) {
            viewModel.setErrorMessage("No plan to save. Please generate a plan first.");
            return;
        }

        savePlanInteractor.execute(new SavePlanInputData(viewModel.getPlanPreview(), name));
    }
}
