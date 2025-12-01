package placefinder.usecases.getplandetails;

import placefinder.entities.Plan;

public class GetPlanDetailsOutputData {
    private final Plan plan;
    private final String errorMessage;

    public GetPlanDetailsOutputData(Plan plan, String errorMessage) {
        this.plan = plan;
        this.errorMessage = errorMessage;
    }

    public Plan getPlan() { return plan; }
    public String getErrorMessage() { return errorMessage; }
}
