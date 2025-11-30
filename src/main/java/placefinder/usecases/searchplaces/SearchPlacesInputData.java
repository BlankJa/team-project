package placefinder.usecases.searchplaces;

public class SearchPlacesInputData {
    private final int userId;
    private final String locationText;
    private final String date; // YYYY-MM-DD

    public SearchPlacesInputData(int userId, String locationText, String date) {
        this.userId = userId;
        this.locationText = locationText;
        this.date = date;
    }

    public int getUserId() { return userId; }
    public String getLocationText() { return locationText; }
    public String getDate() { return date; }
}
