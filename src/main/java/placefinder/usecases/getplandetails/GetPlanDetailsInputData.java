package placefinder.usecases.getplandetails;

public class GetPlanDetailsInputData {
    private final int planId;

    public GetPlanDetailsInputData(int planId) {
        this.planId = planId;
    }

    public int getPlanId() { return planId; }
}
