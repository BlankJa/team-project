package placefinder.usecases.ports;

import placefinder.entities.Place;
import java.util.List;

public interface PlacesGateway {

    /**
     * Search for places near a given location.
     *
     * @param latitude The latitude of the search location
     * @param longitude The longitude of the search location
     * @param categories List of categories to filter by (e.g., "entertainment", "catering")
     * @param radiusMeters The search radius in meters
     * @return List of places found
     * @throws Exception if the API call fails
     */
    List<Place> searchPlaces(double latitude, double longitude,
                            List<String> categories, int radiusMeters) throws Exception;
}