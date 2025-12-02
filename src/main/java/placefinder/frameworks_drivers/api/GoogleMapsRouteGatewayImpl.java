package placefinder.frameworks_drivers.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import placefinder.entities.*;
import placefinder.usecases.dataacessinterfaces.RouteDataAccessInterface;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleMapsRouteGatewayImpl implements RouteDataAccessInterface {
    private final String apiKey;

    public GoogleMapsRouteGatewayImpl() {
        this.apiKey = "AIzaSyAh2raqcn9TfrwMyLpeTQtxlbGCqG8QqZ0";
    }

    public GoogleMapsRouteGatewayImpl(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Route computeRoute(GeocodeResult origin, LocalTime startTime, List<Place> places) throws Exception {
        PlanStop originStop = new PlanStop(0, new Place(), startTime, startTime);

        String fieldMask = "*";
        String url = "https://routes.googleapis.com/directions/v2:computeRoutes"
                + "?key=" + apiKey
                + "&fields=" + fieldMask;

        // Prepare extra headers: set the key and field mask again
        Map<String,String> headers = new HashMap<>();
        headers.put("X-Goog-Api-Key", apiKey);
        headers.put("X-Goog-FieldMask", fieldMask);

        // building input Json body
        JsonObject inputJson = new JsonObject();
        JsonObject start = geocodeToWaypoint(origin);
        inputJson.add("origin", start);
        inputJson.add("destination", start);

        JsonArray intermediates = new JsonArray();
        for  (Place place : places) {
            intermediates.add(placeToWaypoint(place));
        }
        inputJson.add("intermediates", intermediates);

        inputJson.addProperty("travelMode", "WALK");
        inputJson.addProperty("optimizeWaypointOrder", true);

        // calling API
        String json = HttpUtil.post(url, inputJson.toString(), headers);

        // getting output
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray routeList = root.getAsJsonArray("routes");
        if (routeList.isEmpty()) {
            return null;
        }
        JsonObject routeObj = routeList.get(0).getAsJsonObject();

        JsonArray orderJson = routeObj.getAsJsonArray("optimizedIntermediateWaypointIndex");

        // optimized order of stops
        List<Place> order = new ArrayList<>();
        for (JsonElement element : orderJson) {
            order.add(places.get(element.getAsInt()));
        }

        // creating list of legs for the route
        List<Leg> legs = new ArrayList<>();
        List<PlanStop> routeStops= new ArrayList<>();
        for (int legNum = 0; legNum < routeObj.getAsJsonArray("legs").size(); legNum++) {
            JsonObject legObj = routeObj.getAsJsonArray("legs").get(legNum).getAsJsonObject();

            // creating list of steps for leg
            List<Step> steps = new ArrayList<>();
            for (int stepNum = 0; stepNum < legObj.getAsJsonArray("steps").size(); stepNum++) {
                JsonObject stepObj = legObj.getAsJsonArray("steps").get(stepNum).getAsJsonObject();
                JsonObject navInst = stepObj.getAsJsonObject("navigationInstruction");
                if (navInst == null) {
                    Step step = new Step (stepObj.getAsJsonPrimitive("distanceMeters").getAsInt(),
                            Double.parseDouble(stepObj.getAsJsonPrimitive("staticDuration")
                                    .getAsString().replace("s", "")),
                            "Instruction unavailable.");
                    steps.add(step);
                }
                else{
                    Step step = new Step (stepObj.getAsJsonPrimitive("distanceMeters").getAsInt(),
                            Double.parseDouble(stepObj.getAsJsonPrimitive("staticDuration")
                                    .getAsString().replace("s", "")),
                            navInst.getAsJsonPrimitive("instructions").getAsString());
                    steps.add(step);
                }

            }

            double legDuration = Double.parseDouble(legObj.getAsJsonPrimitive("duration").getAsString()
                    .replace("s", ""));
            // creating new Leg object and adding to list
            // first leg
            if (legNum == 0) {
                PlanStop firstStop = new PlanStop(legNum,
                        order.get(legNum),
                        startTime.plusSeconds((long) legDuration),
                        startTime.plusSeconds((long) legDuration).plusHours(1));
                Leg leg = createLeg(legObj, legDuration, originStop, firstStop, steps);
                routeStops.add(firstStop);
                legs.add(leg);
            }
            // last leg
            else if (legNum == routeObj.getAsJsonArray("legs").size() - 1) {
                Leg leg = createLeg(legObj, legDuration, legs.get(legNum - 1).getEndLocation(), originStop, steps);
                legs.add(leg);
            }
            else {
                PlanStop lastStop = legs.get(legNum - 1).getEndLocation();
                PlanStop nextStop = new PlanStop(legNum,
                        order.get(legNum),
                        lastStop.getEndTime().plusSeconds((long) legDuration),
                        lastStop.getEndTime().plusSeconds((long) legDuration).plusHours(1));
                Leg leg = createLeg(legObj, legDuration, lastStop, nextStop, steps);
                routeStops.add(nextStop);
                legs.add(leg);
            }
        }

        // creating route
        return new Route(routeStops,
                legs,
                routeObj.getAsJsonPrimitive("distanceMeters").getAsInt(),
                Double.parseDouble(routeObj.getAsJsonPrimitive("duration")
                        .getAsString().replace("s", "")),
                routeObj.getAsJsonObject("polyline")
                        .getAsJsonPrimitive("encodedPolyline").getAsString());
    }

    private static Leg createLeg(JsonObject legObj, double duration, PlanStop start, PlanStop end, List<Step> steps) {
        return new Leg(legObj.getAsJsonPrimitive("distanceMeters").getAsInt(),
                duration,
                legObj.getAsJsonObject("polyline").getAsJsonPrimitive("encodedPolyline")
                        .getAsString(),
                start,
                end,
                steps);
    }

    private JsonObject placeToWaypoint(Place place){
        JsonObject waypoint = new JsonObject();

        waypoint.addProperty("via", false);
        waypoint.addProperty("vehicleStopover", false);
        waypoint.addProperty("sideOfRoad", false);

        JsonObject location = new JsonObject();

        JsonObject latLng = new JsonObject();
        latLng.addProperty("latitude", place.getLat());
        latLng.addProperty("longitude", place.getLon());

        location.add("latLng", latLng);
        location.addProperty("heading", 0);

        waypoint.add("location", location);

        return waypoint;
    }

    private JsonObject geocodeToWaypoint(GeocodeResult geocodeResult){
        JsonObject waypoint = new JsonObject();

        waypoint.addProperty("via", false);
        waypoint.addProperty("vehicleStopover", false);
        waypoint.addProperty("sideOfRoad", false);

        JsonObject location = new JsonObject();

        JsonObject latLng = new JsonObject();
        latLng.addProperty("latitude", geocodeResult.getLat());
        latLng.addProperty("longitude", geocodeResult.getLon());

        location.add("latLng", latLng);
        location.addProperty("heading", 0);

        waypoint.add("location", location);

        return waypoint;
    }
}