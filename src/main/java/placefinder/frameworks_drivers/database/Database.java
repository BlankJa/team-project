package placefinder.frameworks_drivers.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database connection manager for SQLite.
 * Handles database initialization and provides connection access.
 * Creates necessary tables on first initialization.
 */
public class Database {

    private static final String DB_URL = "jdbc:sqlite:placefinder.db";

    static {
        init();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void init() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON");

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "email TEXT NOT NULL UNIQUE," +
                    "password_hash TEXT NOT NULL," +
                    "home_city TEXT," +
                    "is_verified INTEGER NOT NULL DEFAULT 0," +
                    "verification_code TEXT" +
                    ")");

            // Tables to persist routes, legs and steps associated with a plan
            stmt.execute("CREATE TABLE IF NOT EXISTS plan_routes (" +
                    "plan_id INTEGER PRIMARY KEY," +
                    "distance INTEGER," +
                    "duration REAL," +
                    "encoded_polyline TEXT," +
                    "FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS plan_legs (" +
                    "plan_id INTEGER NOT NULL," +
                    "leg_index INTEGER NOT NULL," +
                    "distance INTEGER," +
                    "duration REAL," +
                    "encoded_polyline TEXT," +
                    "start_seq INTEGER NOT NULL," +
                    "end_seq INTEGER NOT NULL," +
                    "PRIMARY KEY(plan_id, leg_index)," +
                    "FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS plan_steps (" +
                    "plan_id INTEGER NOT NULL," +
                    "leg_index INTEGER NOT NULL," +
                    "step_index INTEGER NOT NULL," +
                    "distance INTEGER," +
                    "duration REAL," +
                    "nav_instruction TEXT," +
                    "PRIMARY KEY(plan_id, leg_index, step_index)," +
                    "FOREIGN KEY(plan_id, leg_index) REFERENCES plan_legs(plan_id, leg_index) ON DELETE CASCADE" +
                    ")");



            // Backward compatibility: add columns if DB existed before we added them
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN is_verified INTEGER NOT NULL DEFAULT 0");
            } catch (SQLException e) {
                // Column already exists, ignore
            }

            try {
                stmt.execute("ALTER TABLE users ADD COLUMN verification_code TEXT");
            } catch (SQLException e) {
                // Column already exists, ignore
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS preferences (" +
                    "user_id INTEGER PRIMARY KEY," +
                    "radius_km REAL NOT NULL DEFAULT 2.0," +
                    "selected_categories TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");

            try {
                stmt.execute("ALTER TABLE preferences ADD COLUMN selected_categories TEXT");
            } catch (SQLException e) {
                // Column already exists, ignore
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS favorite_locations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "address TEXT NOT NULL," +
                    "lat REAL NOT NULL," +
                    "lon REAL NOT NULL," +
                    "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS plans (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "name TEXT NOT NULL," +
                    "date TEXT NOT NULL," +
                    "start_time TEXT NOT NULL," +
                    "origin_address TEXT NOT NULL," +
                    "snapshot_radius_km REAL NOT NULL," +
                    "snapshot_categories TEXT," +
                    "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");

            try {
                stmt.execute("ALTER TABLE plans ADD COLUMN snapshot_categories TEXT");
            } catch (SQLException e) {
                // Column already exists, ignore
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS plan_stops (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "plan_id INTEGER NOT NULL," +
                    "seq INTEGER NOT NULL," +
                    "place_id TEXT," +
                    "place_name TEXT NOT NULL," +
                    "place_address TEXT," +
                    "lat REAL," +
                    "lon REAL," +
                    "start_time TEXT NOT NULL," +
                    "end_time TEXT NOT NULL," +
                    "FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE" +
                    ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
