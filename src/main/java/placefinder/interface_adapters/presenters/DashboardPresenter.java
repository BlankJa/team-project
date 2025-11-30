package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.interface_adapters.viewmodels.PlanDetailsViewModel;
import placefinder.usecases.plans.ApplyPreferencesFromPlanOutputBoundary;
import placefinder.usecases.plans.ApplyPreferencesFromPlanOutputData;

public class DashboardPresenter implements ListPlansOutputBoundary,
        DeletePlanOutputBoundary,
        ApplyPreferencesFromPlanOutputBoundary,
        GetPlanDetailsOutputBoundary{

    private final DashboardViewModel dashboardViewModel;
    private final PlanDetailsViewModel planDetailsViewModel;

public DashboardPresenter(DashboardViewModel dashboardViewModel, PlanDetailsViewModel planDetailsViewModel) {
    this.dashboardViewModel = dashboardViewModel;
    this.planDetailsViewModel = planDetailsViewModel;
}

@Override
    public void present(ListPlansOutputData outputData) {
        if (outputData.getErrorMessage() != null) {
            dashboardViewModel.setPlans(java.util.List.of());
            dashboardViewModel.setErrorMessage(outputData.getErrorMessage());
        } else {
            dashboardViewModel.setPlans(outputData.getPlans());
            dashboardViewModel.setErrorMessage(null);
        }
    }

@Override
    public void present(DeletePlanOutputData outputData) {
        if (outputData.isSuccess()) {
            dashboardViewModel.setMessage(outputData.getMessage());
            dashboardViewModel.setErrorMessage(null);
            // actual removal from list should be triggered by caller with specific planId,
            // or re-load the list; simplest is to re-call loadPlans() after delete.
        } else {
            dashboardViewModel.setErrorMessage(outputData.getMessage());
        }
    }

@Override
    public void present(ApplyPreferencesFromPlanOutputData outputData) {
        if (outputData.isSuccess()) {
            dashboardViewModel.setMessage(outputData.getMessage());
            dashboardViewModel.setErrorMessage(null);
        } else {
            dashboardViewModel.setErrorMessage(outputData.getMessage());
        }
    }

@Override
    public void present(GetPlanDetailsOutputData outputData) {
        if (outputData.getErrorMessage() != null) {
            planDetailsViewModel.setPlan(null);
            planDetailsViewModel.setErrorMessage(outputData.getErrorMessage());
        } else {
            planDetailsViewModel.setPlan(outputData.getPlan());
            planDetailsViewModel.setErrorMessage(null);
        }
    }
}
