package entities;

import java.util.Arrays;

public class PreferenceProfile {
    private String[] interests;
    private String[] locations;
    private String[] cities;
    private float radius;

    public PreferenceProfile() {
        this.interests = new String[0];
        this.locations = new String[0];
        this.cities = new String[]{"Vancouver", "Toronto", "New York", "Ottawa"};
        this.radius = 1.0f;
    }

    public PreferenceProfile(String[] interests, String[] locations, float radius, String[] cities) {
        this.interests = interests != null ? interests : new String[0];
        this.locations = locations != null ? locations : new String[0];
        this.cities = cities != null && cities.length > 0 ? cities : new String[]{"Vancouver", "Toronto", "New York", "Ottawa"};
        this.radius = radius;
    }

    // Setters
    public void setInterests(String[] interests) {
        this.interests = interests != null ? interests : new String[0];
    }

    public void setLocations(String[] locations) {
        this.locations = locations != null ? locations : new String[0];
    }

    public void setCities(String[] cities) {
        this.cities = cities != null && cities.length > 0 ? cities : new String[]{"Vancouver", "Toronto", "New York", "Ottawa"};
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    // Getters
    public String[] getInterests() {
        return interests;
    }

    public String[] getLocations() {
        return locations;
    }

    public String[] getCities() {
        return cities;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public String toString() {
        return "PreferenceProfile{" +
                "interests=" + Arrays.toString(interests) +
                ", locations=" + Arrays.toString(locations) +
                ", cities=" + Arrays.toString(cities) +
                ", radius=" + radius +
                '}';
    }
}