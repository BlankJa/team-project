package placefinder.usecases.getroutedetails;

/**
 * Output boundary for the Get Route Details use case.
 *
 * <p>Implementations of this interface receive the result produced by the
 * interactor and adapt it for the view layer (e.g., update a view model or
 * trigger UI events).  The method is called exactly once per execution of
 * the use case.</p>
 */
public interface GetRouteDetailsOutputBoundary {

    /**
     * Present the outcome of the get-route-details use case.
     *
     * @param outputData the result containing legs or an error message
     */
    void present(GetRouteDetailsOutputData outputData);
}