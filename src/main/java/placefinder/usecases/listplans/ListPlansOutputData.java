package placefinder.usecases.listplans;

import placefinder.entities.Plan;
import java.util.List;

public class ListPlansOutputData {
    private final List<Plan> plans;
    private final String errorMessage;

    public ListPlansOutputData(List<Plan> plans, String errorMessage) {
        this.plans = plans;
        this.errorMessage = errorMessage;
    }

    public List<Plan> getPlans() { return plans; }
    public String getErrorMessage() { return errorMessage; }
}
