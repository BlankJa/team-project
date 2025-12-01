package placefinder.usecases.deleteplan;

public class DeletePlanOutputData {
    private final boolean success;
    private final String message;

    public DeletePlanOutputData(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
