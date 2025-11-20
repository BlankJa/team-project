package frameworks_and_drivers.data.mappers;

import entities.PreferenceProfile;
import frameworks_and_drivers.data.models.PreferenceEntity;

public class PreferenceMapper {
    public PreferenceProfile toDomain(PreferenceEntity entity) {
        if (entity == null) return new PreferenceProfile();

        String[] interests = entity.getInterests().isEmpty() ? new String[0] : entity.getInterests().split(",");
        String[] locations = entity.getLocations().isEmpty() ? new String[0] : entity.getLocations().split(",");
        float radius = entity.getRadius();

        String[] cities;
        if (entity.getCities() != null && !entity.getCities().isEmpty()) {
            cities = entity.getCities().split("\\|");
        } else {
            cities = new String[]{"Vancouver", "Toronto", "New York", "Ottawa"};
        }

        return new PreferenceProfile(interests, locations, radius, cities);
    }

    public PreferenceEntity toData(PreferenceProfile domain) {
        if (domain == null) return new PreferenceEntity();

        PreferenceEntity entity = new PreferenceEntity();
        entity.setInterests(String.join(",", domain.getInterests()));
        entity.setLocations(String.join(",", domain.getLocations()));
        entity.setRadius(domain.getRadius());
        entity.setCities(String.join("|", domain.getCities()));
        return entity;
    }
}