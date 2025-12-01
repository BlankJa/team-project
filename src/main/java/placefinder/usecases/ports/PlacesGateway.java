package placefinder.usecases.ports;

import placefinder.entities.Place;

import java.util.List;
import java.util.Map;

public interface PlacesGateway {
    List<Place> searchPlaces(double lat, double lon, double radiusKm, Map<String, List<String>> selectedCategories) throws Exception;
}
