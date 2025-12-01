package placefinder.usecases.saveplan;

import placefinder.entities.Plan;
import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;

/**
 * Interactor for saving plan.
 */
public class SavePlanInteractor implements SavePlanInputBoundary {

    private final PlanDataAccessInterface planDataAccessInterface;
    private final SavePlanOutputBoundary presenter;

    public SavePlanInteractor(PlanDataAccessInterface planDataAccessInterface,
                              SavePlanOutputBoundary presenter) {
        this.planDataAccessInterface = planDataAccessInterface;
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
            planDataAccessInterface.savePlan(plan);
            presenter.present(new SavePlanOutputData(true, "Plan saved.", plan));
        } catch (Exception e) {
            presenter.present(new SavePlanOutputData(false, e.getMessage(), null));
        }
    }
}
