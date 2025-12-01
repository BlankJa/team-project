package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.usecases.listplans.ListPlansOutputBoundary;
import placefinder.usecases.listplans.ListPlansOutputData;

public class ListPlansPresenter implements ListPlansOutputBoundary {

    private final DashboardViewModel dashboardViewModel;

    public ListPlansPresenter(DashboardViewModel dashboardViewModel) {
        this.dashboardViewModel = dashboardViewModel;
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
}
