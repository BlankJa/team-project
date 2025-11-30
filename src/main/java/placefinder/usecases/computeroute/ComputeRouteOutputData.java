package placefinder.usecases.computeroute;

import placefinder.entities.Route;

public class ComputeRouteOutputData {
    private final Route route;
    private final String errorMessage;

    public ComputeRouteOutputData(Route route, String errorMessage) {
        this.route = route;
        this.errorMessage = errorMessage;
    }

    public Route getRoute() {
        return route;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}