package placefinder.interface_adapters.viewmodels;

/**
 * View model for the registration screen.
 * Holds the registration success status and message.
 */
public class RegisterViewModel {
    private boolean success;
    private String message;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
