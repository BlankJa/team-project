package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.usecases.deleteplan.DeletePlanOutputBoundary;
import placefinder.usecases.deleteplan.DeletePlanOutputData;

public class DeletePlanPresenter implements DeletePlanOutputBoundary {

    private final DashboardViewModel dashboardViewModel;

    public DeletePlanPresenter(DashboardViewModel dashboardViewModel) {
        this.dashboardViewModel = dashboardViewModel;
    }

    @Override
    public void present(DeletePlanOutputData outputData) {
        if (outputData.isSuccess()) {
            dashboardViewModel.setMessage(outputData.getMessage());
            dashboardViewModel.setErrorMessage(null);
        } else {
            dashboardViewModel.setErrorMessage(outputData.getMessage());
        }
    }
}