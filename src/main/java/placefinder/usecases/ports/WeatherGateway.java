package placefinder.usecases.ports;

import placefinder.entities.WeatherSummary;

import java.time.LocalDate;

public interface WeatherGateway {
    WeatherSummary getDailyWeather(double lat, double lon, LocalDate date) throws Exception;
}
