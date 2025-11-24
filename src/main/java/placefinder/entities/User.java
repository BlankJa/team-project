package placefinder.entities;

/**
 * Represents a user entity in the PlaceFinder application.
 * Contains user information including id, name, email, password hash, and home city.
 */
public class User {
    private Integer id;
    private String name;
    private String email;
    private String passwordHash;
    private String homeCity;

    public User(Integer id, String name, String email, String passwordHash, String homeCity) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.homeCity = homeCity;
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
}
