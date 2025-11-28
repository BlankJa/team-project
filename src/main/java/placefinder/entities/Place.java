package placefinder.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a place selected.
 */
public class Place {
    private String id;
    private String name;
    private String address;
    private double lat;
    private double lon;
    private double distanceKm;
    private IndoorOutdoorType indoorOutdoorType;
    private List<String> categories = new ArrayList<>();

    public Place() {
    }

    public Place(String id, String name, String address,
                 double lat, double lon, double distanceKm,
                 IndoorOutdoorType indoorOutdoorType, List<String> categories) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
        this.distanceKm = distanceKm;
        this.indoorOutdoorType = indoorOutdoorType;
        if (categories != null) {
            this.categories = new ArrayList<>(categories);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public IndoorOutdoorType getIndoorOutdoorType() { return indoorOutdoorType; }
    public void setIndoorOutdoorType(IndoorOutdoorType indoorOutdoorType) {
        this.indoorOutdoorType = indoorOutdoorType;
    }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) {
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
    }

    @Override
    public String toString() {
        return name != null ? name : super.toString();
    }
}
