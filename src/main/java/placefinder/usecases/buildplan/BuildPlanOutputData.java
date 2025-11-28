package placefinder.usecases.buildplan;

import placefinder.entities.Plan;

/**
 * Output data containing plan built.
 */
public class BuildPlanOutputData {
    private final Plan plan;
    private final boolean truncated;
    private final String errorMessage;

    public BuildPlanOutputData(Plan plan, boolean truncated, String errorMessage) {
        this.plan = plan;
        this.truncated = truncated;
        this.errorMessage = errorMessage;
    }

    public Plan getPlan() { return plan; }
    public boolean isTruncated() { return truncated; }
    public String getErrorMessage() { return errorMessage; }
}
