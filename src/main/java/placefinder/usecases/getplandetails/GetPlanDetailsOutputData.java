package placefinder.usecases.getplandetails;

import placefinder.entities.Plan;

/**
 * Output data for the Get Plan Details use case.
 */
public class GetPlanDetailsOutputData {

    private final Plan plan;
    private final String errorMessage;

    /**
     * Creates output data for plan details.
     *
     * @param plan         the retrieved plan, or {@code null} if not found
     * @param errorMessage an error message if retrieval failed, otherwise {@code null}
     */
    public GetPlanDetailsOutputData(Plan plan, String errorMessage) {
        this.plan = plan;
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the plan included in this output.
     *
     * @return the plan, or {@code null} if it was not found
     */
    public Plan getPlan() {
        return plan;
    }

    /**
     * Returns any error message associated with this output.
     *
     * @return the error message, or {@code null} if there was no error
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
