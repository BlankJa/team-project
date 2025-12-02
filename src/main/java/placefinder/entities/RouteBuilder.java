// src/main/java/placefinder/entities/RouteBuilder.java
package placefinder.entities;

import java.util.List;

/**
 * A standalone builder for constructing {@link Route} instances.
 */
public class RouteBuilder {
    private List<PlanStop> stops;
    private List<Leg> legs;
    private int distance;
    private double duration;
    private String encodedPolyline;

    public RouteBuilder withStops(List<PlanStop> stops) {
        this.stops = stops;
        return this;
    }

    public RouteBuilder withLegs(List<Leg> legs) {
        this.legs = legs;
        return this;
    }

    public RouteBuilder withDistance(int distance) {
        this.distance = distance;
        return this;
    }

    public RouteBuilder withDuration(double duration) {
        this.duration = duration;
        return this;
    }

    public RouteBuilder withEncodedPolyline(String encodedPolyline) {
        this.encodedPolyline = encodedPolyline;
        return this;
    }

    public Route build() {
        return new Route(stops, legs, distance, duration, encodedPolyline);
    }
}
