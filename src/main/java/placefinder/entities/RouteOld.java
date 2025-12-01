package placefinder.entities;

import java.util.ArrayList;
import java.util.List;

public class RouteOld {
    private List<PlanStop> stops = new ArrayList<>();

    public RouteOld(List<PlanStop> stops) {
        if (stops != null) {
            this.stops = new ArrayList<>(stops);
        }
    }

    public List<PlanStop> getStops() { return stops; }
    public void setStops(List<PlanStop> stops) {
        this.stops = stops != null ? new ArrayList<>(stops) : new ArrayList<>();
    }
}
