package placefinder.usecases.buildplan;

import placefinder.entities.Plan;

/**
 * Output data containing plan built.
 */
public class BuildPlanOutputData {
    private final Plan plan;
    private final String errorMessage;

    public BuildPlanOutputData(Plan plan, String errorMessage) {
        this.plan = plan;
        this.errorMessage = errorMessage;
    }

    public Plan getPlan() { return plan; }
    public String getErrorMessage() { return errorMessage; }
}
