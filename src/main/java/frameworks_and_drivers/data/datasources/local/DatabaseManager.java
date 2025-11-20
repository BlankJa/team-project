package frameworks_and_drivers.data.datasources.local;

import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:data/travelscheduler.db";

    private DatabaseManager() {
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            // Create data directory if it doesn't exist
            new java.io.File("data").mkdirs();

            // Create database connection
            connection = DriverManager.getConnection(DB_URL);

            connection.setAutoCommit(true);

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
        String[] requiredTables = {"users", "preferences", "plans", "locations", "plan_locations"};

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

        // Plans table
        String createPlansTable = """
            CREATE TABLE IF NOT EXISTS plans (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                plan_name TEXT NOT NULL,
                plan_date TEXT NOT NULL,
                total_distance REAL DEFAULT 0.0,
                estimated_time REAL DEFAULT 0.0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
            )
        """;

        // Locations table
        String createLocationsTable = """
            CREATE TABLE IF NOT EXISTS locations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                address TEXT NOT NULL,
                city TEXT NOT NULL,
                cost INTEGER DEFAULT 0,
                type TEXT DEFAULT 'mixed',
                opening_hours TEXT DEFAULT '09:00-18:00'
            )
        """;

        // Plan locations junction table
        String createPlanLocationsTable = """
            CREATE TABLE IF NOT EXISTS plan_locations (
                plan_id INTEGER NOT NULL,
                location_id INTEGER NOT NULL,
                stop_order INTEGER NOT NULL,
                PRIMARY KEY (plan_id, location_id),
                FOREIGN KEY (plan_id) REFERENCES plans (id) ON DELETE CASCADE,
                FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE CASCADE
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createPreferencesTable);
            stmt.execute(createPlansTable);
            stmt.execute(createLocationsTable);
            stmt.execute(createPlanLocationsTable);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                reconnect();
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Error checking database connection: " + e.getMessage());
            reconnect();
            return connection;
        }
    }

    private synchronized void reconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL");
            }
        } catch (SQLException e) {
            System.err.println("Error reconnecting to database: " + e.getMessage());
        }
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

    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed on shutdown");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection on shutdown: " + e.getMessage());
        }
    }
}