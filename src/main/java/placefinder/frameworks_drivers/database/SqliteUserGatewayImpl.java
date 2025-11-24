package placefinder.frameworks_drivers.database;

import placefinder.entities.User;
import placefinder.usecases.ports.UserGateway;

import java.sql.*;

/**
 * SQLite implementation of the UserGateway interface.
 * Provides database operations for user entities using SQLite.
 */
public class SqliteUserGatewayImpl implements UserGateway {

    @Override
    public User findByEmail(String email) throws Exception {
        String sql = "SELECT id, name, email, password_hash, home_city FROM users WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("home_city")
                    );
                }
                return null;
            }
        }
    }

    @Override
    public User findById(int id) throws Exception {
        String sql = "SELECT id, name, email, password_hash, home_city FROM users WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("home_city")
                    );
                }
                return null;
            }
        }
    }

    @Override
    public void save(User user) throws Exception {
        if (user.getId() == null) {
            insert(user);
        } else {
            update(user);
        }
    }

    private void insert(User user) throws Exception {
        String sql = "INSERT INTO users(name, email, password_hash, home_city) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getHomeCity());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        }
    }

    private void update(User user) throws Exception {
        String sql = "UPDATE users SET name = ?, email = ?, password_hash = ?, home_city = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getHomeCity());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        }
    }
}
