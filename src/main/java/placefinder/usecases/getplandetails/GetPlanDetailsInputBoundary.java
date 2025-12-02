package placefinder.usecases.getplandetails;

/**
 * Input boundary for requesting details of a specific plan.
 */
public interface GetPlanDetailsInputBoundary {

    /**
     * Executes the use case to retrieve detailed information for the given plan.
     *
     * @param inputData the data containing the plan ID needed to perform the lookup
     */
    void execute(GetPlanDetailsInputData inputData);
}
