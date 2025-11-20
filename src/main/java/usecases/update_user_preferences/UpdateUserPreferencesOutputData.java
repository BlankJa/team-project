package usecases.update_user_preferences;

public class UpdateUserPreferencesOutputData {
    private final boolean success;
    private final String message;

    public UpdateUserPreferencesOutputData(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
