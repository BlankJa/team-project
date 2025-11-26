package placefinder.usecases.weatheradvice;

public class WeatherAdviceOutputData {
    private final String summary;
    private final String advice;
    private final String errorMessage;

    public WeatherAdviceOutputData(String summary, String advice, String errorMessage) {
        this.summary = summary;
        this.advice = advice;
        this.errorMessage = errorMessage;
    }

    public String getSummary() { return summary; }
    public String getAdvice() { return advice; }
    public String getErrorMessage() { return errorMessage; }
}
