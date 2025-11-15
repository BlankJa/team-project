package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userName;
    private String email;
    private String password;
    private PreferenceProfile profile;

    public User() {
        this.profile = new PreferenceProfile();
    }

    public User(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.profile = new PreferenceProfile();
    }

    // Getters and setters
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public PreferenceProfile getProfile() { return profile; }
    public void setProfile(PreferenceProfile profile) { this.profile = profile; }

    public void updateProfile(PreferenceProfile profile) {
        this.profile = profile;
    }

    @Override
    public String toString() {
        return "User{userName='" + userName + "', email='" + email + "'}";
    }
}

