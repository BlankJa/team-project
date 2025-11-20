package usecases.dataaccess;

import entities.PreferenceProfile;
import entities.User;

public interface UserRepository {
    User authenticate(String email, String password);
    User createUser(User user);
    boolean userExists(String email);
    boolean updateUserPreferences(String email, PreferenceProfile profile);
    User getUserByEmail(String email);
}