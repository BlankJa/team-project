package placefinder.usecases.computeroute;

import placefinder.entities.Route;
import placefinder.usecases.dataacessinterfaces.RouteDataAccessInterface;

public class ComputeRouteInteractor implements ComputeRouteInputBoundary {

    private final RouteDataAccessInterface routeDataAccessInterface;
    private final ComputeRouteOutputBoundary presenter;

    public ComputeRouteInteractor(RouteDataAccessInterface routeDataAccessInterface, ComputeRouteOutputBoundary presenter) {
        this.routeDataAccessInterface = routeDataAccessInterface;
        this.presenter = presenter;
    }

    @Override
    public void execute(ComputeRouteInputData inputData) {
        try {
            Route route = routeDataAccessInterface.computeRoute(inputData.getOrigin(), inputData.getStartTime(), inputData.getStops());
            if (route == null) {
                presenter.present(new ComputeRouteOutputData(null, "Route could not be found."));
            }
            presenter.present(new ComputeRouteOutputData(route, null));
        }
        catch (Exception e) {
            presenter.present(new ComputeRouteOutputData(null, e.getMessage()));
        }
    }
}