package placefinder.usecases.verify;

public class VerifyEmailOutputData {
    private final boolean success;
    private final String message;

    public VerifyEmailOutputData(boolean success, String message) {
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
