package placefinder.usecases.getroutedetails;

/**
 * Input boundary for the Get Route Details use case.
 *
 * <p>This use case retrieves the navigation directions (legs and steps) for a
 * specific plan.  It is triggered by the controller when the user wishes
 * to view route instructions for a saved plan.  The input boundary
 * defines the method that the interactor must implement, accepting
 * immutable input data describing the request.</p>
 */
public interface GetRouteDetailsInputBoundary {

    /**
     * Execute the use case to obtain navigation details for the given plan.
     *
     * @param inputData immutable object containing the plan ID
     */
    void execute(GetRouteDetailsInputData inputData);
}