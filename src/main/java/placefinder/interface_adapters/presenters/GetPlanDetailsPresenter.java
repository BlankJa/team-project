package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.PlanDetailsViewModel;
import placefinder.usecases.getplandetails.GetPlanDetailsOutputBoundary;
import placefinder.usecases.getplandetails.GetPlanDetailsOutputData;

public class GetPlanDetailsPresenter implements GetPlanDetailsOutputBoundary {

    private final PlanDetailsViewModel planDetailsViewModel;

    public GetPlanDetailsPresenter(PlanDetailsViewModel planDetailsViewModel) {
        this.planDetailsViewModel = planDetailsViewModel;
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
