package placefinder.usecases.plans;

import placefinder.entities.Plan;
import placefinder.entities.PreferenceProfile;
import placefinder.usecases.ports.PlanGateway;
import placefinder.usecases.ports.PreferenceGateway;

/**
 * Interactor for applying user preferences to a plan from another plan.
 */
public class ApplyPreferencesFromPlanInteractor implements ApplyPreferencesFromPlanInputBoundary {

    private final PlanGateway planGateway;
    private final PreferenceGateway preferenceGateway;
    private final ApplyPreferencesFromPlanOutputBoundary presenter;

    public ApplyPreferencesFromPlanInteractor(PlanGateway planGateway,
                                              PreferenceGateway preferenceGateway,
                                              ApplyPreferencesFromPlanOutputBoundary presenter) {
        this.planGateway = planGateway;
        this.preferenceGateway = preferenceGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(ApplyPreferencesFromPlanInputData inputData) {
        try {
            Plan plan = planGateway.findPlanWithStops(inputData.getPlanId());
            if (plan == null) {
                presenter.present(new ApplyPreferencesFromPlanOutputData(false, "Plan not found."));
                return;
            }
            PreferenceProfile profile = preferenceGateway.loadForUser(inputData.getUserId());
            profile.setRadiusKm(plan.getSnapshotRadiusKm());
            profile.setSelectedCategories(plan.getSnapshotCategories());
            preferenceGateway.saveForUser(profile);
            presenter.present(new ApplyPreferencesFromPlanOutputData(true,
                    "Preferences updated from plan."));
        } catch (Exception e) {
            presenter.present(new ApplyPreferencesFromPlanOutputData(false, e.getMessage()));
        }
    }
}
