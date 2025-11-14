package model;

import java.util.Arrays;

public class PreferenceProfile {
    private String[] interests;
    private String[] locations;
    private String[] cities;
    private float radius;

    public PreferenceProfile() {
        this.interests = new String[0];
        this.locations = new String[0];
        this.cities = new String[]{"New York", "Washington", "Vancouver", "Toronto", "Sydney", "London", "Paris", "Tokyo", "Toronto", "Berlin", "Rome", "Barcelona", "Amsterdam"};
        this.radius = 10.0f;
    }

    public PreferenceProfile(String[] interests, String[] locations, float radius, String[] cities) {
        this.interests = interests != null ? interests : new String[0];
        this.locations = locations != null ? locations : new String[0];
        this.cities = cities != null ? cities : new String[]{"New York", "Washington", "Vancouver", "Toronto", "Sydney", "London", "Paris", "Tokyo", "Toronto", "Berlin", "Rome", "Barcelona", "Amsterdam"};
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
        this.cities = cities != null ? cities : new String[]{"New York", "Washington", "Vancouver", "Toronto", "Sydney", "London", "Paris", "Tokyo", "Toronto", "Berlin", "Rome", "Barcelona", "Amsterdam"};
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