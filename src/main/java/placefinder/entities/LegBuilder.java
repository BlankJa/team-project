package placefinder.entities;

import java.util.List;

/**
 * A builder class for constructing {@link Leg} instances.
 */
public class LegBuilder {
    private int distance;
    private double duration;
    private String encodedPolyline;
    private PlanStop startLocation;
    private PlanStop endLocation;
    private List<Step> steps;

    public LegBuilder withDistance(int distance) {
        this.distance = distance;
        return this;
    }

    public LegBuilder withDuration(double duration) {
        this.duration = duration;
        return this;
    }

    public LegBuilder withEncodedPolyline(String encodedPolyline) {
        this.encodedPolyline = encodedPolyline;
        return this;
    }

    public LegBuilder withStartLocation(PlanStop startLocation) {
        this.startLocation = startLocation;
        return this;
    }

    public LegBuilder withEndLocation(PlanStop endLocation) {
        this.endLocation = endLocation;
        return this;
    }

    public LegBuilder withSteps(List<Step> steps) {
        this.steps = steps;
        return this;
    }

    public Leg build() {
        return new Leg(distance, duration, encodedPolyline, startLocation, endLocation, steps);
    }
}
