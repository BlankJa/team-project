package placefinder.usecases.weatheradvice;

public class WeatherAdviceInputData {
    private final String locationText;
    private final String date; // optional, can be null or ""

    public WeatherAdviceInputData(String locationText, String date) {
        this.locationText = locationText;
        this.date = date;
    }

    public String getLocationText() { return locationText; }
    public String getDate() { return date; }
}
