package placefinder.usecases.dataacessinterfaces;

import placefinder.entities.GeocodeResult;
import placefinder.entities.PlanStop;
import placefinder.entities.Route;

import java.time.LocalTime;
import java.util.List;

public interface RouteDataAccessInterface {

    /**
     * Compute the optimal walking route between plan stops.
     *
     * @param origin The location data of the start/end location
     * @param startTime The starting time for the route
     * @param stops List of the planned stops locations
     * @return The optimal route as determined by the API
     * @throws Exception if the API call fails
     */
    Route computeRoute(GeocodeResult origin, LocalTime startTime, List<PlanStop> stops) throws Exception;
}