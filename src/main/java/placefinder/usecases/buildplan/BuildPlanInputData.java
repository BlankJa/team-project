package placefinder.usecases.buildplan;

import placefinder.entities.Place;
import java.util.List;

/**
 * Input data for building plan.
 */
public class BuildPlanInputData {
    private final int userId;
    private final String locationText;
    private final String date;
    private final String startTime;
    private final List<Place> selectedPlaces;
    private final Integer existingPlanId;

    public BuildPlanInputData(int userId, String locationText, String date,
                              String startTime, List<Place> selectedPlaces,
                              Integer existingPlanId) {
        this.userId = userId;
        this.locationText = locationText;
        this.date = date;
        this.startTime = startTime;
        this.selectedPlaces = selectedPlaces;
        this.existingPlanId = existingPlanId;
    }

    public int getUserId() { return userId; }
    public String getLocationText() { return locationText; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public List<Place> getSelectedPlaces() { return selectedPlaces; }
    public Integer getExistingPlanId() { return existingPlanId; }
}
