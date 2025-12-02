package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.DirectionsViewModel;
import placefinder.usecases.getroutedetails.GetRouteDetailsInputBoundary;
import placefinder.usecases.getroutedetails.GetRouteDetailsInputData;

/**
 * Controller for the Get Route Details use case.
 */
public class GetRouteDetailsController {

    private final GetRouteDetailsInputBoundary getRouteDetailsInteractor;
    private final DirectionsViewModel directionsViewModel;

    /**
     * Constructs a controller bound to a specific interactor and view model.
     *
     * @param getRouteDetailsInteractor: the use case interactor
     * @param directionsViewModel: the view model for displaying route details
     */
    public GetRouteDetailsController(GetRouteDetailsInputBoundary getRouteDetailsInteractor,
                                     DirectionsViewModel directionsViewModel) {
        this.getRouteDetailsInteractor = getRouteDetailsInteractor;
        this.directionsViewModel = directionsViewModel;
    }

    /**
     * Triggers the use case to load route details for the given plan.
     *
     * @param planId the identifier of the plan whose directions should be loaded
     */
    public void loadRouteDetails(int planId) {
        // Reset any previous error message in the view model
        directionsViewModel.setErrorMessage(null);
        // Invoke the interactor with a new input data object
        getRouteDetailsInteractor.execute(new GetRouteDetailsInputData(planId));
    }
}