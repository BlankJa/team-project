package placefinder.interface_adapters.viewmodels;

public class WeatherAdviceViewModel {
    private String summary;
    private String advice;
    private String errorMessage;
    private boolean isLoading = false;

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public boolean isLoading() { return isLoading; }
    public void setLoading(boolean loading) { this.isLoading = loading; }
}
