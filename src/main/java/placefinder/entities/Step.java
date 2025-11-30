package placefinder.entities;

/**
 * Represents an instruction or step along the route.
 */
public class Step {
    private final int distance;
    private final double duration;
    private final String navInstruction;

    public Step (int distance, double duration, String navInstruction) {
        this.distance = distance;
        this.duration = duration;
        this.navInstruction = navInstruction;
    }

    public int getDistance() {
        return distance;
    }

    public double getDuration() {
        return duration;
    }

    public String getNavInstruction() {
        return navInstruction;
    }
}