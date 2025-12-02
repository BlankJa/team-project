package placefinder.frameworks_drivers.dataaccess;

import placefinder.entities.*;
import placefinder.frameworks_drivers.database.Database;
import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SQLite implementation of PlanGateway for saving plans.
 */
public class SqlitePlanDataAccess implements PlanDataAccessInterface {

    @Override
    public void savePlan(Plan plan) throws Exception {
        if (plan.getId() == null) {
            insertPlan(plan);
        } else {
            updatePlan(plan);
        }
        deleteStopsForPlan(plan.getId());
        insertStops(plan);

        // remove and re‑insert route data
        deleteRouteForPlan(plan.getId());
        insertRoute(plan);
    }



    private void insertPlan(Plan plan) throws Exception {
        String sql = "INSERT INTO plans(user_id, name, date, start_time, origin_address, " +
                "snapshot_radius_km, snapshot_categories) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, plan.getUserId());
            ps.setString(2, plan.getName());
            ps.setString(3, plan.getDate().toString());
            ps.setString(4, plan.getStartTime().toString());
            ps.setString(5, plan.getOriginAddress());
            ps.setDouble(6, plan.getSnapshotRadiusKm());
            ps.setString(7, serializeCategories(plan.getSnapshotCategories()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    plan.setId(rs.getInt(1));
                }
            }
        }
    }

    private void deleteRouteForPlan(Integer planId) throws Exception {
        if (planId == null) return;
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM plan_steps WHERE plan_id = ?")) {
                ps.setInt(1, planId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM plan_legs WHERE plan_id = ?")) {
                ps.setInt(1, planId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM plan_routes WHERE plan_id = ?")) {
                ps.setInt(1, planId);
                ps.executeUpdate();
            }
        }
    }

    private void insertRoute(Plan plan) throws Exception {
        Route route = plan.getRoute();
        if (route == null || route.getStops() == null) return;
        try (Connection conn = Database.getConnection()) {
            // insert plan_routes row
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO plan_routes(plan_id,distance,duration,encoded_polyline) VALUES(?,?,?,?)")) {
                ps.setInt(1, plan.getId());
                ps.setInt(2, route.getDistance());
                ps.setDouble(3, route.getDuration());
                ps.setString(4, route.getEncodedPolyline());
                ps.executeUpdate();
            }
            // insert plan_legs rows
            List<Leg> legs = route.getLegs();
            if (legs != null && !legs.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO plan_legs(plan_id,leg_index,distance,duration,encoded_polyline,start_seq,end_seq) VALUES(?,?,?,?,?,?,?)")) {
                    for (int i = 0; i < legs.size(); i++) {
                        Leg leg = legs.get(i);
                        ps.setInt(1, plan.getId());
                        ps.setInt(2, i);
                        ps.setInt(3, leg.getDistance());
                        ps.setDouble(4, leg.getDuration());
                        ps.setString(5, leg.getEncodedPolyline());
                        ps.setInt(6, leg.getStartLocation().getSequenceNumber());
                        ps.setInt(7, leg.getEndLocation().getSequenceNumber());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                // insert plan_steps rows
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO plan_steps(plan_id,leg_index,step_index,distance,duration,nav_instruction) VALUES(?,?,?,?,?,?)")) {
                    for (int i = 0; i < legs.size(); i++) {
                        Leg leg = legs.get(i);
                        List<Step> steps = leg.getSteps();
                        if (steps == null) continue;
                        for (int j = 0; j < steps.size(); j++) {
                            Step step = steps.get(j);
                            ps.setInt(1, plan.getId());
                            ps.setInt(2, i);
                            ps.setInt(3, j);
                            ps.setInt(4, step.getDistance());
                            ps.setDouble(5, step.getDuration());
                            ps.setString(6, step.getNavInstruction());
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    private Route loadRouteForPlan(Connection conn, int planId, List<PlanStop> stops) throws Exception {
        // read plan_routes; if none found, return null
        int routeDistance = 0;
        double routeDuration = 0.0;
        String routePolyline = null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT distance,duration,encoded_polyline FROM plan_routes WHERE plan_id = ?")) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    routeDistance = rs.getInt("distance");
                    routeDuration = rs.getDouble("duration");
                    routePolyline = rs.getString("encoded_polyline");
                } else {
                    return null;
                }
            }
        }
        // map sequence numbers to PlanStop objects
        Map<Integer, PlanStop> stopMap = new HashMap<>();
        for (PlanStop s : stops) stopMap.put(s.getSequenceNumber(), s);
        List<Leg> legs = new ArrayList<>();
        try (PreparedStatement psLeg = conn.prepareStatement(
                "SELECT leg_index,distance,duration,encoded_polyline,start_seq,end_seq FROM plan_legs WHERE plan_id = ? ORDER BY leg_index")) {
            psLeg.setInt(1, planId);
            try (ResultSet rsLeg = psLeg.executeQuery()) {
                while (rsLeg.next()) {
                    int legIndex = rsLeg.getInt("leg_index");
                    int legDist = rsLeg.getInt("distance");
                    double legDur = rsLeg.getDouble("duration");
                    String legPoly = rsLeg.getString("encoded_polyline");
                    int startSeq = rsLeg.getInt("start_seq");
                    int endSeq = rsLeg.getInt("end_seq");
                    PlanStop startStop = stopMap.get(startSeq);
                    PlanStop endStop = stopMap.get(endSeq);
                    List<Step> stepList = new ArrayList<>();
                    try (PreparedStatement psStep = conn.prepareStatement(
                            "SELECT distance,duration,nav_instruction FROM plan_steps WHERE plan_id = ? AND leg_index = ? ORDER BY step_index")) {
                        psStep.setInt(1, planId);
                        psStep.setInt(2, legIndex);
                        try (ResultSet rsStep = psStep.executeQuery()) {
                            while (rsStep.next()) {
                                stepList.add(new Step(rsStep.getInt("distance"),
                                        rsStep.getDouble("duration"),
                                        rsStep.getString("nav_instruction")));
                            }
                        }
                    }
                    legs.add(new Leg(legDist, legDur, legPoly, startStop, endStop, stepList));
                }
            }
        }
        return new Route(stops, legs, routeDistance, routeDuration, routePolyline);
    }



    private void updatePlan(Plan plan) throws Exception {
        String sql = "UPDATE plans SET name = ?, date = ?, start_time = ?, origin_address = ?, " +
                "snapshot_radius_km = ?, snapshot_categories = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plan.getName());
            ps.setString(2, plan.getDate().toString());
            ps.setString(3, plan.getStartTime().toString());
            ps.setString(4, plan.getOriginAddress());
            ps.setDouble(5, plan.getSnapshotRadiusKm());
            ps.setString(6, serializeCategories(plan.getSnapshotCategories()));
            ps.setInt(7, plan.getId());
            ps.setInt(8, plan.getUserId());
            ps.executeUpdate();
        }
    }

    private void deleteStopsForPlan(Integer planId) throws Exception {
        if (planId == null) return;
        String sql = "DELETE FROM plan_stops WHERE plan_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ps.executeUpdate();
        }
    }

    private void insertStops(Plan plan) throws Exception {
        if (plan.getRoute() == null || plan.getRoute().getStops() == null) return;
        String sql = "INSERT INTO plan_stops(plan_id, seq, place_id, place_name, place_address, " +
                "lat, lon, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (PlanStop stop : plan.getRoute().getStops()) {
                ps.setInt(1, plan.getId());
                ps.setInt(2, stop.getSequenceNumber());
                ps.setString(3, stop.getPlace() != null ? stop.getPlace().getId() : null);
                ps.setString(4, stop.getPlace() != null ? stop.getPlace().getName() : "");
                ps.setString(5, stop.getPlace() != null ? stop.getPlace().getAddress() : null);
                if (stop.getPlace() != null) {
                    ps.setDouble(6, stop.getPlace().getLat());
                    ps.setDouble(7, stop.getPlace().getLon());
                } else {
                    ps.setNull(6, Types.REAL);
                    ps.setNull(7, Types.REAL);
                }
                ps.setString(8, stop.getStartTime().toString());
                ps.setString(9, stop.getEndTime().toString());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public List<Plan> findPlansByUser(int userId) throws Exception {
        String sql = "SELECT id, user_id, name, date, start_time, origin_address, " +
                "snapshot_radius_km, snapshot_categories FROM plans WHERE user_id = ? ORDER BY date DESC, id DESC";
        List<Plan> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer id = rs.getInt("id");
                    LocalDate date = LocalDate.parse(rs.getString("date"));
                    LocalTime start = LocalTime.parse(rs.getString("start_time"));
                    double radius = rs.getDouble("snapshot_radius_km");
                    String categoriesStr = rs.getString("snapshot_categories");
                    Map<String, List<String>> categories = parseCategories(categoriesStr);
                    Plan plan = new Plan(
                            id,
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            date,
                            start,
                            rs.getString("origin_address"),
                            null,
                            radius,
                            categories
                    );
                    list.add(plan);
                }
            }
        }
        return list;
    }

    @Override
    public Plan findPlanWithStops(int planId) throws Exception {
        String sql = "SELECT id, user_id, name, date, start_time, origin_address, " +
                "snapshot_radius_km, snapshot_categories FROM plans WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Integer id = rs.getInt("id");
                int userId = rs.getInt("user_id");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                LocalTime start = LocalTime.parse(rs.getString("start_time"));
                String originAddress = rs.getString("origin_address");
                double radius = rs.getDouble("snapshot_radius_km");
                String categoriesStr = rs.getString("snapshot_categories");
                Map<String, List<String>> categories = parseCategories(categoriesStr);

                // Load all stops as before
                List<PlanStop> stops = loadStopsForPlan(conn, id);

                // Load the full route (distance, legs, steps) from the new tables.
                // This will return null if there is no route data for this plan.
                Route route = loadRouteForPlan(conn, id, stops);

                // Fallback for old plans with no route data which will construct a Route
                // containing only the stops and no legs.
                if (route == null) {
                    route = new Route(stops, new ArrayList<>(), 0, 0.0, null);
                }

                return new Plan(id, userId, rs.getString("name"),
                        date, start, originAddress, route, radius, categories);
            }
        }
    }


    private List<PlanStop> loadStopsForPlan(Connection conn, int planId) throws Exception {
        String sql = "SELECT seq, place_id, place_name, place_address, lat, lon, start_time, end_time " +
                "FROM plan_stops WHERE plan_id = ? ORDER BY seq";
        List<PlanStop> stops = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String placeId = rs.getString("place_id");
                    String name = rs.getString("place_name");
                    String address = rs.getString("place_address");
                    Double lat = rs.getObject("lat") != null ? rs.getDouble("lat") : null;
                    Double lon = rs.getObject("lon") != null ? rs.getDouble("lon") : null;

                    Place place = new Place();
                    place.setId(placeId);
                    place.setName(name);
                    place.setAddress(address);
                    if (lat != null) place.setLat(lat);
                    if (lon != null) place.setLon(lon);
                    place.setDistanceKm(0);
                    place.setIndoorOutdoorType(IndoorOutdoorType.MIXED);
                    place.setCategories(new ArrayList<>());

                    int seq = rs.getInt("seq");
                    LocalTime start = LocalTime.parse(rs.getString("start_time"));
                    LocalTime end = LocalTime.parse(rs.getString("end_time"));
                    stops.add(new PlanStop(seq, place, start, end));
                }
            }
        }
        return stops;
    }

    @Override
    public void deletePlan(int planId, int userId) throws Exception {
        String sql = "DELETE FROM plans WHERE id = ? AND user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Parse categories string
     * Format: mainCategory1:subCategory1,subCategory2|mainCategory2:subCategory1,subCategory2
     */
    private Map<String, List<String>> parseCategories(String categoriesStr) {
        Map<String, List<String>> result = new HashMap<>();
        if (categoriesStr == null || categoriesStr.isBlank()) {
            return result;
        }
        String[] mainCategories = categoriesStr.split("\\|");
        for (String mainCatEntry : mainCategories) {
            if (mainCatEntry.isEmpty()) continue;
            String[] parts = mainCatEntry.split(":", 2);
            if (parts.length == 2) {
                String mainCategory = parts[0].trim();
                String subCategoriesStr = parts[1].trim();
                if (!mainCategory.isEmpty() && !subCategoriesStr.isEmpty()) {
                    List<String> subCategories = Arrays.stream(subCategoriesStr.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                    if (!subCategories.isEmpty()) {
                        result.put(mainCategory, subCategories);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Serialize categories to string
     * Format: mainCategory1:subCategory1,subCategory2|mainCategory2:subCategory1,subCategory2
     */
    private String serializeCategories(Map<String, List<String>> categories) {
        if (categories == null || categories.isEmpty()) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
            String mainCategory = entry.getKey();
            List<String> subCategories = entry.getValue();
            if (mainCategory != null && !mainCategory.isEmpty() &&
                    subCategories != null && !subCategories.isEmpty()) {
                String subCategoriesStr = String.join(",", subCategories);
                entries.add(mainCategory + ":" + subCategoriesStr);
            }
        }
        return String.join("|", entries);
    }
}
