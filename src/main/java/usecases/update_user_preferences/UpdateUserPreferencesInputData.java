package usecases.update_user_preferences;

import entities.PreferenceProfile;

public class UpdateUserPreferencesInputData {
    private final String email;
    private final PreferenceProfile preferenceProfile;

    public UpdateUserPreferencesInputData(String email, PreferenceProfile preferenceProfile) {
        this.email = email;
        this.preferenceProfile = preferenceProfile;
    }

    public String getEmail() {
        return email;
    }

    public PreferenceProfile getPreferenceProfile() {
        return preferenceProfile;
    }
}
