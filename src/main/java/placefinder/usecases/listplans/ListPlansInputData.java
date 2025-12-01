package placefinder.usecases.listplans;

public class ListPlansInputData {
    private final int userId;

    public ListPlansInputData(int userId) {
        this.userId = userId;
    }

    public int getUserId() { return userId; }
}
