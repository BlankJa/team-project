package frameworks_and_drivers.data.datasources.local;

import frameworks_and_drivers.data.models.PreferenceEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PreferenceLocalDataSource {
    private final DatabaseManager databaseManager;

    public PreferenceLocalDataSource(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public PreferenceEntity getUserPreferences(int userId) {
        String sql = "SELECT * FROM preferences WHERE user_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                PreferenceEntity preference = new PreferenceEntity();
                preference.setId(rs.getInt("id"));
                preference.setUserId(rs.getInt("user_id"));
                preference.setInterests(rs.getString("interests"));
                preference.setLocations(rs.getString("locations"));
                preference.setCities(rs.getString("cities"));
                preference.setRadius(rs.getFloat("radius"));
                return preference;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user preferences: " + e.getMessage());
        }
        return null;
    }

    public void createDefaultPreferences(int userId) {
        String sql = "INSERT INTO preferences (user_id, interests, locations, radius) VALUES (?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, "");
            pstmt.setString(3, "");
            pstmt.setDouble(4, 10.0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating default preferences: " + e.getMessage());
        }
    }

    public boolean updateUserPreferences(int userId, PreferenceEntity preference) {
        String sql = "UPDATE preferences SET interests = ?, locations = ?, cities = ?, radius = ? WHERE user_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, preference.getInterests());
            pstmt.setString(2, preference.getLocations());
            pstmt.setString(3, preference.getCities());
            pstmt.setDouble(4, preference.getRadius());
            pstmt.setInt(5, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating preferences: " + e.getMessage());
            return false;
        }
    }
}