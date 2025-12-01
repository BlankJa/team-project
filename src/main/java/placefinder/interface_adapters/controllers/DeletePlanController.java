package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.usecases.deleteplan.DeletePlanInputBoundary;
import placefinder.usecases.deleteplan.DeletePlanInputData;

public class DeletePlanController {

    private final DeletePlanInputBoundary deletePlanInteractor;
    private final DashboardViewModel dashboardViewModel;

    public DeletePlanController(DeletePlanInputBoundary deletePlanInteractor,
                                DashboardViewModel dashboardViewModel) {
        this.deletePlanInteractor = deletePlanInteractor;
        this.dashboardViewModel = dashboardViewModel;
    }

    public void deletePlan(int userId, int planId) {
        // clear only error; success message will be set by presenter
        dashboardViewModel.setErrorMessage(null);
        // NOTE: DeletePlanInputData constructor is (planId, userId)
        deletePlanInteractor.execute(new DeletePlanInputData(planId, userId));
    }
}