package placefinder.interface_adapters.presenters;

import placefinder.interface_adapters.viewmodels.DirectionsViewModel;
import placefinder.usecases.getroutedetails.GetRouteDetailsOutputBoundary;
import placefinder.usecases.getroutedetails.GetRouteDetailsOutputData;

public class GetRouteDetailsPresenter implements GetRouteDetailsOutputBoundary {

    private final DirectionsViewModel directionsViewModel;

    public GetRouteDetailsPresenter(DirectionsViewModel directionsViewModel) {
        this.directionsViewModel = directionsViewModel;
    }

    @Override
    public void present(GetRouteDetailsOutputData outputData) {
        if (outputData.getErrorMessage() != null) {
            // Clear any existing legs and show an error message
            directionsViewModel.clear();
            directionsViewModel.setErrorMessage(outputData.getErrorMessage());
        } else {
            // Populate the legs and clear any old error message
            directionsViewModel.setFromRoute(outputData.getLegs());
            directionsViewModel.setErrorMessage(null);
        }
    }
}
