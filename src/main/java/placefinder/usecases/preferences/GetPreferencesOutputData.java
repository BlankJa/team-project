package placefinder.usecases.preferences;

import placefinder.entities.FavoriteLocation;

import java.util.List;
import java.util.Map;

/**
 * Output data containing retrieved user preferences.
 */
public class GetPreferencesOutputData {
    private final double radiusKm;
    private final List<FavoriteLocation> favorites;
    private final Map<String, List<String>> selectedCategories;
    private final String errorMessage;

    public GetPreferencesOutputData(double radiusKm,
                                    List<FavoriteLocation> favorites,
                                    Map<String, List<String>> selectedCategories,
                                    String errorMessage) {
        this.radiusKm = radiusKm;
        this.favorites = favorites;
        this.selectedCategories = selectedCategories;
        this.errorMessage = errorMessage;
    }

    public double getRadiusKm() { return radiusKm; }
    public List<FavoriteLocation> getFavorites() { return favorites; }
    public Map<String, List<String>> getSelectedCategories() { return selectedCategories; }
    public String getErrorMessage() { return errorMessage; }
}

