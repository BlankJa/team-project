package placefinder.usecases.computeroute;

import placefinder.entities.Route;
import placefinder.usecases.ports.RouteGateway;

public class ComputeRouteInteractor implements ComputeRouteInputBoundary {

    private final RouteGateway routeGateway;
    private final ComputeRouteOutputBoundary presenter;

    public ComputeRouteInteractor(RouteGateway routeGateway, ComputeRouteOutputBoundary presenter) {
        this.routeGateway = routeGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(ComputeRouteInputData inputData) {
        try {
            Route route = routeGateway.computeRoute(inputData.getOrigin(), inputData.getStartTime(), inputData.getStops());
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