package placefinder.usecases.dataacessinterfaces;

import placefinder.entities.WeatherSummary;

import java.time.LocalDate;

public interface WeatherDataAccessInterface {
    WeatherSummary getDailyWeather(double lat, double lon, LocalDate date) throws Exception;
}
