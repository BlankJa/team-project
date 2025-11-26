package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.DashboardViewModel;
import placefinder.interface_adapters.viewmodels.PlanDetailsViewModel;
import placefinder.usecases.plans.*;

public class DashboardController implements
        ListPlansOutputBoundary,
        DeletePlanOutputBoundary,
        ApplyPreferencesFromPlanOutputBoundary,
        GetPlanDetailsOutputBoundary {

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

    public DashboardViewModel getDashboardViewModel() { return dashboardViewModel; }
    public PlanDetailsViewModel getPlanDetailsViewModel() { return planDetailsViewModel; }
}
