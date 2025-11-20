package frameworks_and_drivers.data.datasources.local;

import entities.PreferenceProfile;
import frameworks_and_drivers.data.models.UserEntity;

import java.sql.*;

public class UserLocalDataSource {
    private final DatabaseManager databaseManager;

    public UserLocalDataSource(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public UserEntity authenticate(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                UserEntity user = new UserEntity();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }

    public UserEntity createUser(UserEntity user) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                user.setId(rs.getInt(1));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
        return null;
    }

    public boolean userExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }

    public UserEntity getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                UserEntity user = new UserEntity();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setCreatedAt(rs.getString("created_at"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
        }
        return null;
    }

    public int getUserIdByEmail(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
            return -1;
        }
    }

    public boolean updateUserPreferences(String email, PreferenceProfile profile) {
        int userId = getUserIdByEmail(email);
        if (userId == -1) return false;

        String sql = "UPDATE preferences SET interests = ?, locations = ?, cities = ?, radius = ? WHERE user_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, String.join(",", profile.getInterests()));
            pstmt.setString(2, String.join(",", profile.getLocations()));
            pstmt.setString(3, String.join("|", profile.getCities())); // Store cities with | separator
            pstmt.setDouble(4, profile.getRadius());
            pstmt.setInt(5, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating preferences: " + e.getMessage());
            return false;
        }
    }
}