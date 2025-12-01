package placefinder.usecases.dataacessinterfaces;

import placefinder.entities.User;

/**
 * Gateway interface for user data access operations.
 * Defines methods for finding and saving user entities.
 */
public interface UserDataAccessInterface {
    User findByEmail(String email) throws Exception;
    User findById(int id) throws Exception;
    void save(User user) throws Exception;
}
