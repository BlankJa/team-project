package service;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static DatabaseService instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:data/travelscheduler.db";

    private DatabaseService() {
        initializeDatabase();
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            // Create data directory if it doesn't exist
            new java.io.File("data").mkdirs();

            // Create database connection
            connection = DriverManager.getConnection(DB_URL);

            // Check if tables need to be created
            if (!checkTablesExist()) {
                createTables();
                System.out.println("Database tables created successfully");
            } else {
                System.out.println("Database tables already exist");
            }

            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Check if all required tables already exist in the database
     */
    private boolean checkTablesExist() {
        String[] requiredTables = {"users", "preferences"};

        try {
            DatabaseMetaData metaData = connection.getMetaData();

            for (String table : requiredTables) {
                ResultSet rs = metaData.getTables(null, null, table, null);
                if (!rs.next()) {
                    System.out.println("Table not found: " + table);
                    return false;
                }
                rs.close();
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Error checking table existence: " + e.getMessage());
            return false;
        }
    }

    private void createTables() throws SQLException {
        // Users table
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;

        // Preferences table
        String createPreferencesTable = """
            CREATE TABLE IF NOT EXISTS preferences (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                interests TEXT,
                locations TEXT,
                cities TEXT,
                radius REAL DEFAULT 10.0,
                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                UNIQUE(user_id)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createPreferencesTable);
        }
    }

    // User operations
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPassword());
            pstmt.executeUpdate();

            // Create default preference profile for the user
            createDefaultPreferences(getUserIdByEmail(user.getEmail()));
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    public User authenticateUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserName(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setProfile(getUserPreferences(getUserIdByEmail(email)));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }

    public boolean userExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }

    private int getUserIdByEmail(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("id") : -1;
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
            return -1;
        }
    }

    // Preference operations
    private void createDefaultPreferences(int userId) {
        String sql = "INSERT INTO preferences (user_id, interests, locations, radius) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, ""); // Empty interests
            pstmt.setString(3, ""); // Empty locations
            pstmt.setDouble(4, 10.0); // Default radius
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating default preferences: " + e.getMessage());
        }
    }

    public boolean updateUserPreferences(String email, PreferenceProfile profile) {
        int userId = getUserIdByEmail(email);
        if (userId == -1) return false;

        String sql = "UPDATE preferences SET interests = ?, locations = ?, cities = ?, radius = ? WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

    public PreferenceProfile getUserPreferences(int userId) {
        String sql = "SELECT * FROM preferences WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String interestsStr = rs.getString("interests");
                String locationsStr = rs.getString("locations");
                String citiesStr = rs.getString("cities");

                String[] interests = interestsStr.isEmpty() ? new String[0] : interestsStr.split(",");
                String[] locations = locationsStr.isEmpty() ? new String[0] : locationsStr.split(",");
                float radius = rs.getFloat("radius");

                // Parse cities with | separator, use default if empty
                String[] cities;
                if (citiesStr != null && !citiesStr.isEmpty()) {
                    cities = citiesStr.split("\\|");
                } else {
                    cities = new String[]{"New York", "Washington", "Vancouver", "Toronto", "Sydney", "London", "Paris", "Tokyo", "Toronto", "Berlin", "Rome", "Barcelona", "Amsterdam"};
                }

                return new PreferenceProfile(interests, locations, radius, cities);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user preferences: " + e.getMessage());
        }
        return new PreferenceProfile();
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}

