package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.usecases.listplans.ListPlansInputBoundary;
import placefinder.usecases.listplans.ListPlansInputData;

public class ListPlansController {

    private final ListPlansInputBoundary listPlansInteractor;
    private final DashboardViewModel dashboardViewModel;

    public ListPlansController(ListPlansInputBoundary listPlansInteractor,
                               DashboardViewModel dashboardViewModel) {
        this.listPlansInteractor = listPlansInteractor;
        this.dashboardViewModel = dashboardViewModel;
    }

    public void loadPlans(int userId) {
        dashboardViewModel.setErrorMessage(null);
        dashboardViewModel.setMessage(null);
        listPlansInteractor.execute(new ListPlansInputData(userId));
    }
}
