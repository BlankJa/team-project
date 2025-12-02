package placefinder.entities;

/**
 * A builder class for constructing {@link Step} instances.
 */
public class StepBuilder {
    private int distance;
    private double duration;
    private String instruction;

    public StepBuilder withDistance(int distance) {
        this.distance = distance;
        return this;
    }

    public StepBuilder withDuration(double duration) {
        this.duration = duration;
        return this;
    }

    public StepBuilder withInstruction(String instruction) {
        this.instruction = instruction;
        return this;
    }

    public Step build() {
        return new Step(distance, duration, instruction);
    }
}
