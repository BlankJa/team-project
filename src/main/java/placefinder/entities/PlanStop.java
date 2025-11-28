package placefinder.entities;

import java.time.LocalTime;

/**
 * Represents a stop in the user's generated plan.
 */
public class PlanStop {
    private int sequenceNumber;
    private Place place;
    private LocalTime startTime;
    private LocalTime endTime;

    public PlanStop(int sequenceNumber, Place place,
                    LocalTime startTime, LocalTime endTime) {
        this.sequenceNumber = sequenceNumber;
        this.place = place;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public Place getPlace() { return place; }
    public void setPlace(Place place) { this.place = place; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}
