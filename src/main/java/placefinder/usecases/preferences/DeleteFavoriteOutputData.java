package placefinder.usecases.preferences;

public class DeleteFavoriteOutputData {
    private final boolean success;
    private final String message;

    public DeleteFavoriteOutputData(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
