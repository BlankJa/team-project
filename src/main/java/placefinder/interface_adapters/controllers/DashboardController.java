package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.interface_adapters.viewmodels.PlanDetailsViewModel;
import placefinder.usecases.deleteplan.DeletePlanInputBoundary;
import placefinder.usecases.deleteplan.DeletePlanInputData;
import placefinder.usecases.getplandetails.GetPlanDetailsInputBoundary;
import placefinder.usecases.getplandetails.GetPlanDetailsInputData;
import placefinder.usecases.listplans.ListPlansInputBoundary;
import placefinder.usecases.listplans.ListPlansInputData;
import placefinder.usecases.plans.*;

public class DashboardController {

    private final ListPlansInputBoundary listPlansInteractor;
    private final DeletePlanInputBoundary deletePlanInteractor;
    private final ApplyPreferencesFromPlanInputBoundary applyPreferencesFromPlanInteractor;
    private final GetPlanDetailsInputBoundary getPlanDetailsInteractor;

    private final DashboardViewModel dashboardViewModel;
    private final PlanDetailsViewModel planDetailsViewModel;

    public DashboardController(ListPlansInputBoundary listPlansInteractor,
                               DeletePlanInputBoundary deletePlanInteractor,
                               ApplyPreferencesFromPlanInputBoundary applyPreferencesFromPlanInteractor,
                               GetPlanDetailsInputBoundary getPlanDetailsInteractor,
                               DashboardViewModel dashboardViewModel,
                               PlanDetailsViewModel planDetailsViewModel) {
        this.listPlansInteractor = listPlansInteractor;
        this.deletePlanInteractor = deletePlanInteractor;
        this.applyPreferencesFromPlanInteractor = applyPreferencesFromPlanInteractor;
        this.getPlanDetailsInteractor = getPlanDetailsInteractor;
        this.dashboardViewModel = dashboardViewModel;
        this.planDetailsViewModel = planDetailsViewModel;
    }

    public void loadPlans(int userId) {
        dashboardViewModel.setErrorMessage(null);
        dashboardViewModel.setMessage(null);
        listPlansInteractor.execute(new ListPlansInputData(userId));
    }

    public void deletePlan(int userId, int planId) {
        dashboardViewModel.setErrorMessage(null);
        deletePlanInteractor.execute(new DeletePlanInputData(planId, userId));
    }

    public void applyPreferencesFromPlan(int userId, int planId) {
        dashboardViewModel.setErrorMessage(null);
        applyPreferencesFromPlanInteractor.execute(new ApplyPreferencesFromPlanInputData(planId, userId));
    }

    public void loadPlanDetails(int planId) {
        planDetailsViewModel.setErrorMessage(null);
        getPlanDetailsInteractor.execute(new GetPlanDetailsInputData(planId));
    }


    public DashboardViewModel getDashboardViewModel() { return dashboardViewModel; }
    public PlanDetailsViewModel getPlanDetailsViewModel() { return planDetailsViewModel; }
}
