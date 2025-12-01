package placefinder.frameworks_drivers.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import placefinder.entities.DayTripExperienceCategories;
import placefinder.entities.IndoorOutdoorType;
import placefinder.entities.Place;
import placefinder.usecases.dataacessinterfaces.PlacesDataAccessInterface;

import java.util.*;
import java.util.stream.Collectors;

public class GeoApifyGatewayImpl implements PlacesDataAccessInterface {
    private final String apiKey;

    public GeoApifyGatewayImpl() {
        this.apiKey = "75be457789934a199ed4014ad24925ba";
    }

    public GeoApifyGatewayImpl(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<Place> searchPlaces(double lat, double lon, double radiusKm,
                                    Map<String, List<String>> selectedCategories) throws Exception {

        double radiusMeters = radiusKm * 1000.0;
        String categoriesParam = buildCategoriesParam(selectedCategories);

        String url = "https://api.geoapify.com/v2/places?categories=" + categoriesParam +
                "&filter=circle:" + lon + "," + lat + "," + (int) radiusMeters +
                "&bias=proximity:" + lon + "," + lat +
                "&limit=40&apiKey=" + apiKey;

        String json = HttpUtil.get(url);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray features = root.getAsJsonArray("features");
        List<Place> places = new ArrayList<>();
        if (features == null) return places;

        for (JsonElement featureEl : features) {
            JsonObject feature = featureEl.getAsJsonObject();
            JsonObject props = feature.getAsJsonObject("properties");
            if (props == null) continue;

            List<String> catStrings = new ArrayList<>();
            if (props.has("categories") && props.get("categories").isJsonArray()) {
                for (JsonElement c : props.getAsJsonArray("categories")) {
                    catStrings.add(c.getAsString());
                }
            } else if (props.has("category")) {
                catStrings.add(props.get("category").getAsString());
            }

            String id = props.has("place_id") ? props.get("place_id").getAsString() : null;
            String rawName = props.has("name") ? props.get("name").getAsString() : null;
            String name;
            if (rawName == null || rawName.trim().isEmpty()) {
                if (!catStrings.isEmpty())
                {
                    name = DayTripExperienceCategories.getDisplayName(catStrings.get(0));
                }
                else
                {
                    name = "(unknown)";
                }
            }
            else {
                name = rawName;
            }
            String address = props.has("formatted") ? props.get("formatted").getAsString() : "";
            double plat = props.has("lat") ? props.get("lat").getAsDouble()
                    : feature.getAsJsonObject("geometry")
                    .getAsJsonArray("coordinates").get(1).getAsDouble();
            double plon = props.has("lon") ? props.get("lon").getAsDouble()
                    : feature.getAsJsonObject("geometry")
                    .getAsJsonArray("coordinates").get(0).getAsDouble();
            double distanceKm = props.has("distance") ? props.get("distance").getAsDouble() / 1000.0 : 0.0;

            IndoorOutdoorType type = classifyIndoorOutdoor(catStrings);

            Place place = new Place();
            place.setId(id);
            place.setName(name);
            place.setAddress(address);
            place.setLat(plat);
            place.setLon(plon);
            place.setDistanceKm(distanceKm);
            place.setIndoorOutdoorType(type);
            place.setCategories(catStrings);
            places.add(place);
        }

        return places;
    }

    private String firstCategory(List<String> categories) {
        return categories.isEmpty() ? "(unknown)" : categories.get(0);
    }

    private String buildCategoriesParam(Map<String, List<String>> selectedCategories) {
        if (selectedCategories == null || selectedCategories.isEmpty()) {
            return "tourism.sights,entertainment,leisure.park,catering";
        }
        List<String> allCategories = selectedCategories.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        if (allCategories.isEmpty()) {
            return "tourism.sights,entertainment,leisure.park,catering";
        }

        return String.join(",", allCategories);
    }

    private IndoorOutdoorType classifyIndoorOutdoor(List<String> catStrings) {
        boolean hasPark = false;
        boolean hasIndoor = false;
        for (String c : catStrings) {
            if (c.startsWith("leisure.park") || c.startsWith("natural")) {
                hasPark = true;
            }
            if (c.startsWith("catering.") || c.startsWith("commercial.") || c.startsWith("entertainment.")) {
                hasIndoor = true;
            }
        }
        if (hasPark && !hasIndoor) return IndoorOutdoorType.OUTDOOR;
        if (hasIndoor && !hasPark) return IndoorOutdoorType.INDOOR;
        if (hasIndoor && hasPark) return IndoorOutdoorType.MIXED;
        return IndoorOutdoorType.MIXED;
    }
}