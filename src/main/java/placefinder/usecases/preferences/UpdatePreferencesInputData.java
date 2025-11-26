package placefinder.usecases.preferences;

import java.util.Map;
import java.util.List;

/**
 * Input data for updating user preferences.
 */
public class UpdatePreferencesInputData {
    private final int userId;
    private final double radiusKm;
    private final Map<String, List<String>> selectedCategories;

    public UpdatePreferencesInputData(int userId, double radiusKm,
                                      Map<String, List<String>> selectedCategories) {
        this.userId = userId;
        this.radiusKm = radiusKm;
        this.selectedCategories = selectedCategories;
    }

    public int getUserId() { return userId; }
    public double getRadiusKm() { return radiusKm; }
    public Map<String, List<String>> getSelectedCategories() { return selectedCategories; }
}

