package placefinder.usecases.saveplan;

import placefinder.entities.Plan;

/**
 * Output data for save plan.
 */
public class SavePlanOutputData {
    private final boolean success;
    private final String message;
    private final Plan plan;

    public SavePlanOutputData(boolean success, String message, Plan plan) {
        this.success = success;
        this.message = message;
        this.plan = plan;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Plan getPlan() { return plan; }
}
