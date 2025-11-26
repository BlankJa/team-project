package placefinder.entities;

/**
 * Represents a user entity in the PlaceFinder application.
 * Contains user information including id, name, email, password hash, home city,
 * and email verification state.
 */
public class User {

    private Integer id;
    private String name;
    private String email;
    private String passwordHash;
    private String homeCity;
    private boolean verified;          // maps to is_verified (0/1) in DB
    private String verificationCode;   // last sent verification code

    // Full constructor (used when loading from SQLite)
    public User(Integer id,
                String name,
                String email,
                String passwordHash,
                String homeCity,
                boolean verified,
                String verificationCode) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.homeCity = homeCity;
        this.verified = verified;
        this.verificationCode = verificationCode;
    }

    // Convenience constructor for NEW users (registration)
    public User(Integer id,
                String name,
                String email,
                String passwordHash,
                String homeCity) {
        this(id, name, email, passwordHash, homeCity, false, null);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getHomeCity() { return homeCity; }
    public void setHomeCity(String homeCity) { this.homeCity = homeCity; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
}
