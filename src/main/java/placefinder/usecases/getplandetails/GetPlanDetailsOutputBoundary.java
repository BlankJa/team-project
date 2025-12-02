package placefinder.usecases.getplandetails;

/**
 * Output boundary for the Get Plan Details use case.
 * <p>
 * Implementations of this interface receive the result produced by the
 * interactor and format it for the view layer (e.g., updating a view model).
 */
public interface GetPlanDetailsOutputBoundary {

    /**
     * Presents the result of the Get Plan Details use case.
     *
     * @param outputData the data containing the plan or an error message
     */
    void present(GetPlanDetailsOutputData outputData);
}
