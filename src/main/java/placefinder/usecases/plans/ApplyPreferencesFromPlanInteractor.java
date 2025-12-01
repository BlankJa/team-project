package placefinder.usecases.plans;

import placefinder.entities.Plan;
import placefinder.entities.PreferenceProfile;
import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;

/**
 * Interactor for applying user preferences to a plan from another plan.
 */
public class ApplyPreferencesFromPlanInteractor implements ApplyPreferencesFromPlanInputBoundary {

    private final PlanDataAccessInterface planDataAccessInterface;
    private final PreferenceDataAccessInterface preferenceDataAccessInterface;
    private final ApplyPreferencesFromPlanOutputBoundary presenter;

    public ApplyPreferencesFromPlanInteractor(PlanDataAccessInterface planDataAccessInterface,
                                              PreferenceDataAccessInterface preferenceDataAccessInterface,
                                              ApplyPreferencesFromPlanOutputBoundary presenter) {
        this.planDataAccessInterface = planDataAccessInterface;
        this.preferenceDataAccessInterface = preferenceDataAccessInterface;
        this.presenter = presenter;
    }

    @Override
    public void execute(ApplyPreferencesFromPlanInputData inputData) {
        try {
            Plan plan = planDataAccessInterface.findPlanWithStops(inputData.getPlanId());
            if (plan == null) {
                presenter.present(new ApplyPreferencesFromPlanOutputData(false, "Plan not found."));
                return;
            }
            PreferenceProfile profile = preferenceDataAccessInterface.loadForUser(inputData.getUserId());
            profile.setRadiusKm(plan.getSnapshotRadiusKm());
            profile.setSelectedCategories(plan.getSnapshotCategories());
            preferenceDataAccessInterface.saveForUser(profile);
            presenter.present(new ApplyPreferencesFromPlanOutputData(true,
                    "Preferences updated from plan."));
        } catch (Exception e) {
            presenter.present(new ApplyPreferencesFromPlanOutputData(false, e.getMessage()));
        }
    }
}
