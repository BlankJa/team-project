package placefinder.entities;

import java.util.List;

public class Route {
    private final List<PlanStop> stops;
    private final List<Leg> legs;
    private final int distance;
    private final double duration;
    private final String encodedPolyline;

    public Route(List<PlanStop> stops, List<Leg> legs, int distance, double duration, String encodedPolyline) {
        this.stops = stops;
        this.legs = legs;
        this.distance = distance;
        this.duration = duration;
        this.encodedPolyline = encodedPolyline;
    }

    public List<PlanStop> getStops() { return stops; }

    public List<Leg> getLegs() { return legs; }

    public int getDistance() {return distance;}

    public double getDuration() {return duration;}

    public String getEncodedPolyline() {return encodedPolyline;}
}
