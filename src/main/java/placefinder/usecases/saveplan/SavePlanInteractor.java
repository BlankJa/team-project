package placefinder.usecases.saveplan;

import placefinder.entities.Plan;
import placefinder.usecases.ports.PlanGateway;

/**
 * Interactor for saving plan.
 */
public class SavePlanInteractor implements SavePlanInputBoundary {

    private final PlanGateway planGateway;
    private final SavePlanOutputBoundary presenter;

    public SavePlanInteractor(PlanGateway planGateway,
                              SavePlanOutputBoundary presenter) {
        this.planGateway = planGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(SavePlanInputData inputData) {
        try {
            Plan plan = inputData.getPlan();
            String name = inputData.getName();
            if (plan == null || plan.getRoute() == null ||
                    plan.getRoute().getStops() == null ||
                    plan.getRoute().getStops().isEmpty()) {
                presenter.present(new SavePlanOutputData(false,
                        "No plan to save. Please generate a plan first.", null));
                return;
            }
            String finalName = name;
            if (finalName == null || finalName.trim().isEmpty()) {
                finalName = "Plan - " + plan.getDate() + " - " + plan.getOriginAddress();
            }
            plan.setName(finalName);
            planGateway.savePlan(plan);
            presenter.present(new SavePlanOutputData(true, "Plan saved.", plan));
        } catch (Exception e) {
            presenter.present(new SavePlanOutputData(false, e.getMessage(), null));
        }
    }
}
