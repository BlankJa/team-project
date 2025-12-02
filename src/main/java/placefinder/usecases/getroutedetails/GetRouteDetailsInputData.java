package placefinder.usecases.getroutedetails;

/**
 * Data structure used to pass input parameters to the Get Route Details use case.
 *
 * <p>Since Clean Architecture advocates using plain data structures for
 * communication between layers, this class simply wraps the plan ID for
 * which navigation details are requested.  It is immutable and threadsafe.</p>
 */
public class GetRouteDetailsInputData {
    private final int planId;

    /**
     * Construct a new request for route details for the specified plan.
     *
     * @param planId the identifier of the plan whose directions are sought
     */
    public GetRouteDetailsInputData(int planId) {
        this.planId = planId;
    }

    /**
     * The ID of the plan whose route details should be loaded.
     *
     * @return the plan identifier
     */
    public int getPlanId() {
        return planId;
    }
}