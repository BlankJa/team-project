package placefinder.interface_adapters.viewmodels;

import placefinder.entities.FavoriteLocation;

import java.util.*;

/**
 * ViewModel for managing preference-related UI state and data.
 */
public class PreferencesViewModel {
    private double radiusKm = 2.0;
    private List<FavoriteLocation> favorites = new ArrayList<>();
    private Map<String, List<String>> selectedCategories = new HashMap<>();
    private String message;
    private String errorMessage;

    public double getRadiusKm() { return radiusKm; }
    public void setRadiusKm(double radiusKm) { this.radiusKm = radiusKm; }

    public List<FavoriteLocation> getFavorites() { return Collections.unmodifiableList(favorites); }
    public void setFavorites(List<FavoriteLocation> favorites) {
        this.favorites = favorites != null ? new ArrayList<>(favorites) : new ArrayList<>();
    }

    public Map<String, List<String>> getSelectedCategories() {
        return Collections.unmodifiableMap(selectedCategories);
    }
    
    public void setSelectedCategories(Map<String, List<String>> selectedCategories) {
        this.selectedCategories = selectedCategories != null 
            ? new HashMap<>(selectedCategories) 
            : new HashMap<>();
    }

    public void addFavorite(FavoriteLocation favorite) {
        if (favorite == null) return;
        this.favorites.add(favorite);
    }

    public void removeFavoriteById(int id) {
        this.favorites.removeIf(f -> f.getId() != null && f.getId() == id);
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}

