package placefinder.usecases.computeroute;

import placefinder.entities.GeocodeResult;
import placefinder.entities.PlanStop;

import java.time.LocalTime;
import java.util.List;

public class ComputeRouteInputData {
    private final GeocodeResult origin;
    private final LocalTime startTime;
    private final List<PlanStop> stops;

    public ComputeRouteInputData(GeocodeResult origin, LocalTime startTime, List<PlanStop> stops) {
        this.origin = origin;
        this.startTime = startTime;
        this.stops = stops;
    }

    public GeocodeResult getOrigin() {
        return origin;
    }

    public List<PlanStop> getStops() {
        return stops;
    }

    public LocalTime getStartTime() {
        return startTime;
    }
}