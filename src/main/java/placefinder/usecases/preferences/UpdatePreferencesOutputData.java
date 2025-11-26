package placefinder.usecases.preferences;

/**
 * Output data containing the result of updating user preferences.
 */
public class UpdatePreferencesOutputData {
    private final boolean success;
    private final String message;

    public UpdatePreferencesOutputData(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}

