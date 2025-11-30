package placefinder.entities;

import java.util.List;

/**
 * Represents the leg between two planned stops in the route.
 */
public class Leg {
    private final int distance;
    private final double duration;
    private final String encodedPolyline;
    private final PlanStop startLocation;
    private final PlanStop endLocation;
    private final List<Step> steps;

    public Leg (int distance, double duration, String polyline, PlanStop startLocation, PlanStop endLocation, List<Step> steps) {
        this.distance = distance;
        this.duration = duration;
        this.encodedPolyline = polyline;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.steps = steps;
    }

    public int getDistance() {
        return distance;
    }

    public double getDuration() {
        return duration;
    }

    public String getEncodedPolyline() {
        return encodedPolyline;
    }

    public PlanStop getStartLocation() {
        return startLocation;
    }

    public PlanStop getEndLocation() {
        return endLocation;
    }

    public List<Step> getSteps() {
        return steps;
    }
}