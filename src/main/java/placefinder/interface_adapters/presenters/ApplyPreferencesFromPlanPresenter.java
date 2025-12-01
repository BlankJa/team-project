package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.usecases.plans.ApplyPreferencesFromPlanOutputBoundary;
import placefinder.usecases.plans.ApplyPreferencesFromPlanOutputData;

public class ApplyPreferencesFromPlanPresenter implements ApplyPreferencesFromPlanOutputBoundary {

    private final DashboardViewModel dashboardViewModel;

    public ApplyPreferencesFromPlanPresenter(DashboardViewModel dashboardViewModel) {
        this.dashboardViewModel = dashboardViewModel;
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
}
