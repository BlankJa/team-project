package placefinder.usecases.saveplan;

import placefinder.entities.Plan;

/**
 * Input data for saving plan.
 */
public class SavePlanInputData {
    private final Plan plan;
    private final String name;

    public SavePlanInputData(Plan plan, String name) {
        this.plan = plan;
        this.name = name;
    }

    public Plan getPlan() { return plan; }
    public String getName() { return name; }
}
