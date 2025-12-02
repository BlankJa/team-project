package placefinder.usecases.login;

import placefinder.entities.User;

/**
 * Data transfer object containing login output information.
 * Holds the success status, a message, and the authenticated user (if successful).
 */
public class LoginOutputData {

    private final boolean success;
    private final String message;
    private final User user;

    public LoginOutputData(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}
