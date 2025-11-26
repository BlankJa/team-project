package placefinder.entities;

public class WeatherSummary {
    private final double temperatureC;
    private final String conditions;
    private final double uvIndex;
    private final boolean precipitationLikely;

    public WeatherSummary(double temperatureC, String conditions,
                          double uvIndex, boolean precipitationLikely) {
        this.temperatureC = temperatureC;
        this.conditions = conditions;
        this.uvIndex = uvIndex;
        this.precipitationLikely = precipitationLikely;
    }

    public double getTemperatureC() { return temperatureC; }
    public String getConditions() { return conditions; }
    public double getUvIndex() { return uvIndex; }
    public boolean isPrecipitationLikely() { return precipitationLikely; }
}
