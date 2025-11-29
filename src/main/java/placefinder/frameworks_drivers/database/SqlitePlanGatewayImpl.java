package placefinder.frameworks_drivers.database;

import placefinder.entities.*;
import placefinder.usecases.ports.PlanGateway;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SQLite implementation of PlanGateway for saving plans.
 */
public class SqlitePlanGatewayImpl implements PlanGateway {

    @Override
    public void savePlan(Plan plan) throws Exception {
        if (plan.getId() == null) {
            insertPlan(plan);
        } else {
            updatePlan(plan);
        }
        // stops
        deleteStopsForPlan(plan.getId());
        insertStops(plan);
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

                List<PlanStop> stops = loadStopsForPlan(conn, id);
                Route route = new Route(stops);

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
