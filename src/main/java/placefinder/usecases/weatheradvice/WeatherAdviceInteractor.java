package placefinder.usecases.weatheradvice;

import placefinder.entities.GeocodeResult;
import placefinder.entities.WeatherSummary;
import placefinder.usecases.dataacessinterfaces.GeocodingDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.WeatherDataAccessInterface;

import java.time.LocalDate;

public class WeatherAdviceInteractor implements WeatherAdviceInputBoundary {

    private final GeocodingDataAccessInterface geocodingDataAccessInterface;
    private final WeatherDataAccessInterface weatherDataAccessInterface;
    private final WeatherAdviceOutputBoundary presenter;

    public WeatherAdviceInteractor(GeocodingDataAccessInterface geocodingDataAccessInterface,
                                   WeatherDataAccessInterface weatherDataAccessInterface,
                                   WeatherAdviceOutputBoundary presenter) {
        this.geocodingDataAccessInterface = geocodingDataAccessInterface;
        this.weatherDataAccessInterface = weatherDataAccessInterface;
        this.presenter = presenter;
    }

    @Override
    public void execute(WeatherAdviceInputData inputData) {
        try {
            GeocodeResult geo = geocodingDataAccessInterface.geocode(inputData.getLocationText());
            if (geo == null) {
                presenter.present(new WeatherAdviceOutputData(null, null, "Could not find that location."));
                return;
            }

            LocalDate date = (inputData.getDate() == null || inputData.getDate().isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(inputData.getDate());

            WeatherSummary weather = weatherDataAccessInterface.getDailyWeather(geo.getLat(), geo.getLon(), date);
            if (weather == null) {
                presenter.present(new WeatherAdviceOutputData(null, null,
                        "Unable to retrieve weather data at the moment."));
                return;
            }

            String summary = String.format(
                    "Location: %s%nDate: %s%nTemperature: %.1f°C%nConditions: %s%nUV Index: %.1f",
                    geo.getFormattedAddress(),
                    date,
                    weather.getTemperatureC(),
                    weather.getConditions(),
                    weather.getUvIndex()
            );

            StringBuilder advice = new StringBuilder();
            if (weather.getTemperatureC() <= 5) {
                advice.append("It is cold, wear a warm jacket, long pants, and consider gloves or a hat. ");
            } else if (weather.getTemperatureC() <= 15) {
                advice.append("It is cool, a light jacket or sweater is recommended. ");
            } else if (weather.getTemperatureC() <= 25) {
                advice.append("The temperature is comfortable, normal clothes are fine.  ");
            } else {
                advice.append("It is quite warm, wear light, breathable clothing.  ");
            }

            if (weather.getUvIndex() >= 6) {
                advice.append("UV index is high — use sunscreen, sunglasses, and consider a hat. ");
            } else if (weather.getUvIndex() >= 3) {
                advice.append("UV index is moderate — sunscreen and sunglasses are a good idea. ");
            }

            if (weather.isPrecipitationLikely()) {
                advice.append("Rain or snow is expected — bring an umbrella or waterproof jacket. ");
            }

            presenter.present(new WeatherAdviceOutputData(summary, advice.toString().trim(), null));
        } catch (Exception e) {
            presenter.present(new WeatherAdviceOutputData(null, null, e.getMessage()));
        }
    }
}
