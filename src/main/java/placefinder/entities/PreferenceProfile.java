package placefinder.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a user's preference profile including search radius and selected categories.
 */
public class PreferenceProfile {
    private int userId;
    private double radiusKm;
    private Map<String, List<String>> selectedCategories = new HashMap<>();

    public PreferenceProfile(int userId, double radiusKm) {
        this.userId = userId;
        this.radiusKm = radiusKm;
    }

    public PreferenceProfile(int userId, double radiusKm, 
                             Map<String, List<String>> selectedCategories) {
        this.userId = userId;
        this.radiusKm = radiusKm;
        if (selectedCategories != null) {
            this.selectedCategories = new HashMap<>(selectedCategories);
        }
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getRadiusKm() { return radiusKm; }
    public void setRadiusKm(double radiusKm) { this.radiusKm = radiusKm; }

    public Map<String, List<String>> getSelectedCategories() { 
        return new HashMap<>(selectedCategories);
    }
    
    public void setSelectedCategories(Map<String, List<String>> selectedCategories) {
        this.selectedCategories = selectedCategories != null 
            ? new HashMap<>(selectedCategories) 
            : new HashMap<>();
    }
}

