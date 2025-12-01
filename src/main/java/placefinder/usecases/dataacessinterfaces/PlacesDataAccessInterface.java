package placefinder.usecases.dataacessinterfaces;

import placefinder.entities.Place;

import java.util.List;
import java.util.Map;

public interface PlacesDataAccessInterface {
    List<Place> searchPlaces(double lat, double lon, double radiusKm, Map<String, List<String>> selectedCategories) throws Exception;
}
