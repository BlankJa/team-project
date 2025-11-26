package placefinder.entities;

/**
 * Represents a favorite location saved by a user.
 */
public class FavoriteLocation {
    private Integer id;
    private Integer userId;
    private String name;
    private String address;
    private double lat;
    private double lon;

    public FavoriteLocation(Integer id, Integer userId, String name,
                            String address, double lat, double lon) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }
}

