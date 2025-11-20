package usecases.update_user_preferences;

import entities.PreferenceProfile;
import usecases.common.Result;
import usecases.dataaccess.UserRepository;

public class UpdateUserPreferencesUseCase {
    private final UserRepository userRepository;

    public UpdateUserPreferencesUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Result<Boolean> execute(String email, PreferenceProfile profile) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return Result.error("Email cannot be empty");
            }
            if (profile == null) {
                return Result.error("Profile cannot be null");
            }

            boolean success = userRepository.updateUserPreferences(email, profile);
            return Result.success(success);
        } catch (Exception e) {
            return Result.error("Failed to update preferences: " + e.getMessage());
        }
    }
}