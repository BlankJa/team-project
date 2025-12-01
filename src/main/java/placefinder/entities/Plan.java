package placefinder.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a user's generated plan.
 */
public class Plan {
    private Integer id;
    private int userId;
    private String name;
    private LocalDate date;
    private LocalTime startTime;
    private String originAddress;
    private RouteOld route;
    private double snapshotRadiusKm;
    private Map<String, List<String>> snapshotCategories = new HashMap<>();

    public Plan(Integer id, int userId, String name,
                LocalDate date, LocalTime startTime,
                String originAddress, RouteOld route,
                double snapshotRadiusKm, Map<String, List<String>> snapshotCategories) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.date = date;
        this.startTime = startTime;
        this.originAddress = originAddress;
        this.route = route;
        this.snapshotRadiusKm = snapshotRadiusKm;
        if (snapshotCategories != null) {
            this.snapshotCategories = new HashMap<>(snapshotCategories);
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public String getOriginAddress() { return originAddress; }
    public void setOriginAddress(String originAddress) { this.originAddress = originAddress; }

    public RouteOld getRoute() { return route; }
    public void setRoute(RouteOld route) { this.route = route; }

    public double getSnapshotRadiusKm() { return snapshotRadiusKm; }
    public void setSnapshotRadiusKm(double snapshotRadiusKm) { this.snapshotRadiusKm = snapshotRadiusKm; }

    public Map<String, List<String>> getSnapshotCategories() {
        return new HashMap<>(snapshotCategories);
    }

    public void setSnapshotCategories(Map<String, List<String>> snapshotCategories) {
        this.snapshotCategories = snapshotCategories != null
                ? new HashMap<>(snapshotCategories) : new HashMap<>();
    }
}
