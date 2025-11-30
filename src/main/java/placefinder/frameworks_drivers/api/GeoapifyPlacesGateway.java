package placefinder.frameworks_drivers.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import placefinder.entities.Place;
import placefinder.usecases.logging.PlacesApiLogger;
import placefinder.usecases.ports.PlacesGateway;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of PlacesGateway using the Geoapify Places API with optional logging.
 *
 * SOLID Principles:
 * - Single Responsibility Principle (SRP): This class has one responsibility -
 *   fetching places from the Geoapify API and converting them to Place entities.
 * - Open/Closed Principle (OCP): This implementation can be replaced with another
 *   API implementation (e.g., Google Places) without changing the use case code.
 * - Dependency Inversion Principle (DIP): This class depends on the PlacesApiLogger
 *   abstraction, allowing logging to be toggled on/off without changing this code.
 *   Use cases also depend on PlacesGateway abstraction, not this concrete implementation.
 */
public class GeoapifyPlacesGateway implements PlacesGateway {

    private final String apiKey;
    private final PlacesApiLogger logger;

    /**
     * Constructor with logger for debugging.
     * DIP: Depends on PlacesApiLogger abstraction, not concrete implementation.
     */
    public GeoapifyPlacesGateway(String apiKey, PlacesApiLogger logger) {
        this.apiKey = apiKey;
        this.logger = logger;
    }

    @Override
    public List<Place> searchPlaces(double latitude, double longitude,
                                   List<String> categories, int radiusMeters) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            // Log the request if logger is available
            if (logger != null) {
                logger.logSearchRequest(
                    String.format("lat=%.6f, lon=%.6f, radius=%dm", latitude, longitude, radiusMeters),
                    categories != null ? String.join(", ", categories) : "default categories"
                );
            }

            // Build the API URL
            StringBuilder url = new StringBuilder("https://api.geoapify.com/v2/places?");
            url.append("categories=").append(buildCategoriesParam(categories));
            url.append("&filter=circle:").append(longitude).append(",").append(latitude)
               .append(",").append(radiusMeters);
            url.append("&limit=50");
            url.append("&apiKey=").append(apiKey);

            // Make the HTTP request
            String json = HttpUtil.get(url.toString());

            // Parse the JSON response
            List<Place> places = parsePlacesFromJson(json, latitude, longitude);

            // Log the response if logger is available
            if (logger != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                logger.logSearchResponse(places, responseTime);
            }

            return places;

        } catch (Exception e) {
            // Log the error if logger is available
            if (logger != null) {
                logger.logError(e.getMessage());
            }
            throw e;
        }
    }

    /**
     * Builds the categories parameter for the Geoapify API.
     * SRP: This method has one job - format categories for the API.
     */
    private String buildCategoriesParam(List<String> categories) throws Exception {
        if (categories == null || categories.isEmpty()) {
            return "tourism,entertainment,catering,leisure";
        }
        return URLEncoder.encode(String.join(",", categories), StandardCharsets.UTF_8);
    }

    /**
     * Parses places from the Geoapify JSON response.
     * SRP: This method has one job - convert JSON to Place entities.
     */
    private List<Place> parsePlacesFromJson(String json, double originLat, double originLon) {
        List<Place> places = new ArrayList<>();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray features = root.getAsJsonArray("features");

        if (features == null) {
            return places;
        }

        for (JsonElement element : features) {
            JsonObject feature = element.getAsJsonObject();
            JsonObject properties = feature.getAsJsonObject("properties");
            JsonObject geometry = feature.getAsJsonObject("geometry");

            if (properties == null || geometry == null) {
                continue;
            }

            // Extract place data
            String id = properties.has("place_id") ?
                       properties.get("place_id").getAsString() : null;
            String name = properties.has("name") ?
                         properties.get("name").getAsString() : "Unknown";
            String address = properties.has("formatted") ?
                            properties.get("formatted").getAsString() : "";

            // Extract coordinates
            JsonArray coordinates = geometry.getAsJsonArray("coordinates");
            double lon = coordinates.get(0).getAsDouble();
            double lat = coordinates.get(1).getAsDouble();

            // Calculate distance
            double distanceKm = calculateDistance(originLat, originLon, lat, lon);

            // Extract categories
            List<String> categoryList = new ArrayList<>();
            if (properties.has("categories")) {
                JsonArray cats = properties.getAsJsonArray("categories");
                for (JsonElement cat : cats) {
                    categoryList.add(cat.getAsString());
                }
            }

            // Create place with null for indoorOutdoorType (keeping it simple)
            places.add(new Place(id, name, address, lat, lon, distanceKm, null, categoryList));
        }

        return places;
    }

    /**
     * Calculates the distance between two coordinates using the Haversine formula.
     * SRP: This method has one job - calculate distance between coordinates.
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                  Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                  Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}