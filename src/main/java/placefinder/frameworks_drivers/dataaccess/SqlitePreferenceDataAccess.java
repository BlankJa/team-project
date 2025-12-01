package placefinder.frameworks_drivers.dataaccess;

import placefinder.entities.DayTripExperienceCategories;
import placefinder.entities.FavoriteLocation;
import placefinder.entities.PreferenceProfile;
import placefinder.frameworks_drivers.database.Database;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SQLite implementation of PreferenceGateway for storing and retrieving user preferences.
 */
public class SqlitePreferenceDataAccess implements PreferenceDataAccessInterface {

    @Override
    public PreferenceProfile loadForUser(int userId) throws Exception {
        String sql = "SELECT radius_km, selected_categories FROM preferences WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double radius = rs.getDouble("radius_km");
                    String selectedCategoriesStr = rs.getString("selected_categories");
                    Map<String, List<String>> selectedCategories = parseSelectedCategories(selectedCategoriesStr);
                    return new PreferenceProfile(userId, radius, selectedCategories);
                }
            }
        }

        // create default if not exists
        PreferenceProfile profile = new PreferenceProfile(userId, 2.0, new HashMap<>());
        saveForUser(profile);
        return profile;
    }

    @Override
    public void saveForUser(PreferenceProfile profile) throws Exception {
        String update = "UPDATE preferences SET radius_km = ?, selected_categories = ? WHERE user_id = ?";
        String insert = "INSERT INTO preferences(user_id, radius_km, selected_categories) VALUES (?, ?, ?)";

        String selectedCategoriesStr = serializeSelectedCategories(profile.getSelectedCategories());
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setDouble(1, profile.getRadiusKm());
                ps.setString(2, selectedCategoriesStr);
                ps.setInt(3, profile.getUserId());
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    try (PreparedStatement ins = conn.prepareStatement(insert)) {
                        ins.setInt(1, profile.getUserId());
                        ins.setDouble(2, profile.getRadiusKm());
                        ins.setString(3, selectedCategoriesStr);
                        ins.executeUpdate();
                    }
                }
            }
        }
    }

    @Override
    public List<FavoriteLocation> listFavorites(int userId) throws Exception {
        String sql = "SELECT id, user_id, name, address, lat, lon FROM favorite_locations WHERE user_id = ? ORDER BY id";
        List<FavoriteLocation> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new FavoriteLocation(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("address"),
                            rs.getDouble("lat"),
                            rs.getDouble("lon")
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public FavoriteLocation addFavorite(int userId, String name, String address, double lat, double lon) throws Exception {
        String sql = "INSERT INTO favorite_locations(user_id, name, address, lat, lon) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, address);
            ps.setDouble(4, lat);
            ps.setDouble(5, lon);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new FavoriteLocation(id, userId, name, address, lat, lon);
                }
            }
        }
        return null;
    }

    @Override
    public void deleteFavorite(int favoriteId, int userId) throws Exception {
        String sql = "DELETE FROM favorite_locations WHERE id = ? AND user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, favoriteId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Parse selectedCategories string
     * Format: mainCategory1:subCategory1,subCategory2|mainCategory2:subCategory1,subCategory2
     */
    private Map<String, List<String>> parseSelectedCategories(String selectedCategoriesStr) {
        Map<String, List<String>> result = new HashMap<>();
        if (selectedCategoriesStr == null || selectedCategoriesStr.isBlank()) {
            return result;
        }
        String[] mainCategories = selectedCategoriesStr.split("\\|");
        for (String mainCatEntry : mainCategories) {
            if (mainCatEntry.isEmpty()) continue;
            String[] parts = mainCatEntry.split(":", 2);
            if (parts.length == 2) {
                String mainCategory = parts[0].trim();
                String subCategoriesStr = parts[1].trim();
                if (!mainCategory.isEmpty() && !subCategoriesStr.isEmpty()) {
                    // Get valid sub-categories for this main category
                    List<String> validSubCategories = DayTripExperienceCategories.getSubCategories(mainCategory);
                    List<String> subCategories = Arrays.stream(subCategoriesStr.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .filter(validSubCategories::contains)  // Only keep valid sub-categories
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
     * Serialize selectedCategories to string
     * Format: mainCategory1:subCategory1,subCategory2|mainCategory2:subCategory1,subCategory2
     */
    private String serializeSelectedCategories(Map<String, List<String>> selectedCategories) {
        if (selectedCategories == null || selectedCategories.isEmpty()) {
            return "";
        }
        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : selectedCategories.entrySet()) {
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

