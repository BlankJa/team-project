package placefinder.usecases.register;

/**
 * Data transfer object containing registration output information.
 * Holds the success status and message for registration operations.
 */
public class RegisterOutputData {
    private final boolean success;
    private final String message;

    public RegisterOutputData(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
