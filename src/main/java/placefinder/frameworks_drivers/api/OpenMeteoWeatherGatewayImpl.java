package placefinder.frameworks_drivers.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import placefinder.entities.WeatherSummary;
import placefinder.usecases.ports.WeatherGateway;

import java.time.LocalDate;

public class OpenMeteoWeatherGatewayImpl implements WeatherGateway {

    @Override
    public WeatherSummary getDailyWeather(double lat, double lon, LocalDate date) throws Exception {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                "&longitude=" + lon +
                "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max,uv_index_max,weathercode" +
                "&timezone=auto&start_date=" + date + "&end_date=" + date;

        String json = HttpUtil.get(url);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject daily = root.getAsJsonObject("daily");
        if (daily == null) {
            return null;
        }

        JsonArray tmaxArr = daily.getAsJsonArray("temperature_2m_max");
        JsonArray tminArr = daily.getAsJsonArray("temperature_2m_min");
        JsonArray precipArr = daily.getAsJsonArray("precipitation_probability_max");
        JsonArray uvArr = daily.getAsJsonArray("uv_index_max");
        JsonArray codeArr = daily.getAsJsonArray("weathercode");

        if (tmaxArr == null || tmaxArr.size() == 0 ||
            tminArr == null || tminArr.size() == 0 ||
            precipArr == null || precipArr.size() == 0 ||
            uvArr == null || uvArr.size() == 0 ||
            codeArr == null || codeArr.size() == 0) {
            return null;
        }

        double tmax = tmaxArr.get(0).getAsDouble();
        double tmin = tminArr.get(0).getAsDouble();
        double tempC = (tmax + tmin) / 2.0;
        int precipProb = precipArr.get(0).getAsInt();
        double uvIndex = uvArr.get(0).getAsDouble();
        int code = codeArr.get(0).getAsInt();

        String conditions = mapWeatherCode(code);
        boolean precipLikely = isPrecipitationCode(code) || precipProb >= 50;

        return new WeatherSummary(tempC, conditions, uvIndex, precipLikely);
    }

    private String mapWeatherCode(int code) {
        // based on Open-Meteo / WMO weather codes
        return switch (code) {
            case 0 -> "Clear sky";
            case 1, 2, 3 -> "Mainly clear, partly cloudy, or overcast";
            case 45, 48 -> "Fog or depositing rime fog";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing rain";
            case 71, 73, 75 -> "Snowfall";
            case 77 -> "Snow grains";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown conditions";
        };
    }

    private boolean isPrecipitationCode(int code) {
        return switch (code) {
            case 51, 53, 55,
                 56, 57,
                 61, 63, 65,
                 66, 67,
                 71, 73, 75,
                 77,
                 80, 81, 82,
                 85, 86,
                 95, 96, 99 -> true;
            default -> false;
        };
    }
}
