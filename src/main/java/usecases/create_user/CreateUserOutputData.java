package usecases.create_user;

import entities.User;

public class CreateUserOutputData {
    private final boolean success;
    private final User user;
    private final String message;

    public CreateUserOutputData(boolean success, User user, String message) {
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
