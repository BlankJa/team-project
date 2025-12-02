package placefinder.frameworks_drivers.dataaccess;

/**
 * Configuration class for email credentials.
 * Reads from environment variables with fallback to default values.
 */
public class EmailConfig {
    private final String username;
    private final String password;

    public EmailConfig() {
        this.username = System.getenv().getOrDefault("EMAIL_USERNAME", "subhanakbar908@gmail.com");
        this.password = System.getenv().getOrDefault("EMAIL_PASSWORD", "eqrsbydralnvylzm");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}