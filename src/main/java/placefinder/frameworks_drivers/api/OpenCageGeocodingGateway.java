package placefinder.frameworks_drivers.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import placefinder.entities.GeocodeResult;
import placefinder.usecases.ports.GeocodingGateway;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OpenCageGeocodingGateway implements GeocodingGateway {

    private final String apiKey;

    public OpenCageGeocodingGateway() {
        this.apiKey = "c2ab249430a240bbaeebb6c7ef8e01a8";
    }

    public OpenCageGeocodingGateway(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public GeocodeResult geocode(String query) throws Exception {
        if (query == null || query.isBlank()) {
            return null;
        }

        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.opencagedata.com/geocode/v1/json?q=" + encoded +
                "&key=" + apiKey + "&limit=1";

        String json = HttpUtil.get(url);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray results = root.getAsJsonArray("results");
        if (results == null || results.size() == 0) {
            return null;
        }
        JsonObject first = results.get(0).getAsJsonObject();
        JsonObject geometry = first.getAsJsonObject("geometry");
        double lat = geometry.get("lat").getAsDouble();
        double lon = geometry.get("lng").getAsDouble();
        String formatted = first.has("formatted")
                ? first.get("formatted").getAsString()
                : query;
        return new GeocodeResult(lat, lon, formatted);
    }
}
