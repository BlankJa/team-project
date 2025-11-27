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
        String sql = "SELECT id, name, email, password_hash, home_city, " +
                "is_verified, verification_code " +
                "FROM users WHERE email = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapRowToUser(rs);
                    System.out.println("[GATEWAY] findByEmail(" + email + ") -> id=" + user.getId());
                    return user;
                } else {
                    System.out.println("[GATEWAY] findByEmail(" + email + ") -> null");
                    return null;
                }
            }
        }
    }

    @Override
    public User findById(int id) throws Exception {
        String sql = "SELECT id, name, email, password_hash, home_city, " +
                "is_verified, verification_code " +
                "FROM users WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
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
        String sql =
                "INSERT INTO users(name, email, password_hash, home_city, " +
                        "is_verified, verification_code) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getHomeCity());
            ps.setInt(5, user.isVerified() ? 1 : 0);
            ps.setString(6, user.getVerificationCode());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }

            System.out.println("[GATEWAY] insert user email=" + user.getEmail()
                    + " id=" + user.getId());
        }
    }

    private void update(User user) throws Exception {
        String sql =
                "UPDATE users SET " +
                        "name = ?, " +
                        "email = ?, " +
                        "password_hash = ?, " +
                        "home_city = ?, " +
                        "is_verified = ?, " +
                        "verification_code = ? " +
                        "WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getHomeCity());
            ps.setInt(5, user.isVerified() ? 1 : 0);
            ps.setString(6, user.getVerificationCode());
            ps.setInt(7, user.getId());

            ps.executeUpdate();

            System.out.println("[GATEWAY] update user id=" + user.getId()
                    + " verified=" + user.isVerified()
                    + " code=" + user.getVerificationCode());
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("home_city"),
                rs.getInt("is_verified") == 1,
                rs.getString("verification_code")
        );
    }
}