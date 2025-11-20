package usecases.authenticate_user;

import entities.User;

public class AuthenticateUserOutputData {
    private final boolean success;
    private final User user;
    private final String message;

    public AuthenticateUserOutputData(boolean success, User user, String message) {
        this.success = success;
        this.user = user;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }
}
