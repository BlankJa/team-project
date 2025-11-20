package frameworks_and_drivers.data.models;

public class PreferenceEntity {
    private int id;
    private int userId;
    private String interests;
    private String locations;
    private String cities;
    private float radius;

    public PreferenceEntity() {
        this.interests = "";
        this.locations = "";
        this.cities = "";
        this.radius = 10.0f;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }

    public String getLocations() { return locations; }
    public void setLocations(String locations) { this.locations = locations; }

    public String getCities() { return cities; }
    public void setCities(String cities) { this.cities = cities; }

    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }
}