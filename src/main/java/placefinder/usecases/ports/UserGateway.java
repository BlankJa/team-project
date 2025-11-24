package placefinder.usecases.ports;

import placefinder.entities.User;

/**
 * Gateway interface for user data access operations.
 * Defines methods for finding and saving user entities.
 */
public interface UserGateway {
    User findByEmail(String email) throws Exception;
    User findById(int id) throws Exception;
    void save(User user) throws Exception;
}
