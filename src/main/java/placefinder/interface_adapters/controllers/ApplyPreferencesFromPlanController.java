package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.usecases.plans.ApplyPreferencesFromPlanInputBoundary;
import placefinder.usecases.plans.ApplyPreferencesFromPlanInputData;

public class ApplyPreferencesFromPlanController {

    private final ApplyPreferencesFromPlanInputBoundary applyPreferencesFromPlanInteractor;
    private final DashboardViewModel dashboardViewModel;

    public ApplyPreferencesFromPlanController(ApplyPreferencesFromPlanInputBoundary applyPreferencesFromPlanInteractor,
                                              DashboardViewModel dashboardViewModel) {
        this.applyPreferencesFromPlanInteractor = applyPreferencesFromPlanInteractor;
        this.dashboardViewModel = dashboardViewModel;
    }

    public void applyPreferencesFromPlan(int userId, int planId) {
        dashboardViewModel.setErrorMessage(null);
        // NOTE: your ApplyPreferencesFromPlanInputData constructor is (planId, userId)
        applyPreferencesFromPlanInteractor.execute(
                new ApplyPreferencesFromPlanInputData(planId, userId)
        );
    }
}