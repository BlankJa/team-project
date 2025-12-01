package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.PlanDetailsViewModel;
import placefinder.usecases.getplandetails.GetPlanDetailsInputBoundary;
import placefinder.usecases.getplandetails.GetPlanDetailsInputData;

public class GetPlanDetailsController {

    private final GetPlanDetailsInputBoundary getPlanDetailsInteractor;
    private final PlanDetailsViewModel planDetailsViewModel;

    public GetPlanDetailsController(GetPlanDetailsInputBoundary getPlanDetailsInteractor,
                                    PlanDetailsViewModel planDetailsViewModel) {
        this.getPlanDetailsInteractor = getPlanDetailsInteractor;
        this.planDetailsViewModel = planDetailsViewModel;
    }

    public void loadPlanDetails(int planId) {
        planDetailsViewModel.setErrorMessage(null);
        getPlanDetailsInteractor.execute(new GetPlanDetailsInputData(planId));
    }
}
