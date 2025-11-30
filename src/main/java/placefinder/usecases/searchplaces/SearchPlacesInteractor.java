package placefinder.usecases.searchplaces;

import placefinder.entities.Place;
import placefinder.entities.PreferenceProfile;
import placefinder.entities.WeatherSummary;
import placefinder.entities.GeocodeResult;
import placefinder.entities.IndoorOutdoorType;
import placefinder.usecases.ports.GeocodingGateway;
import placefinder.usecases.ports.PlacesGateway;
import placefinder.usecases.ports.PreferenceGateway;
import placefinder.usecases.ports.WeatherGateway;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchPlacesInteractor implements SearchPlacesInputBoundary {

    private final PreferenceGateway preferenceGateway;
    private final GeocodingGateway geocodingGateway;
    private final PlacesGateway placesGateway;
    private final WeatherGateway weatherGateway;
    private final SearchPlacesOutputBoundary presenter;

    public SearchPlacesInteractor(PreferenceGateway preferenceGateway,
                                  GeocodingGateway geocodingGateway,
                                  PlacesGateway placesGateway,
                                  WeatherGateway weatherGateway,
                                  SearchPlacesOutputBoundary presenter) {
        this.preferenceGateway = preferenceGateway;
        this.geocodingGateway = geocodingGateway;
        this.placesGateway = placesGateway;
        this.weatherGateway = weatherGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(SearchPlacesInputData inputData) {
        try {
            PreferenceProfile profile = preferenceGateway.loadForUser(inputData.getUserId());

            GeocodeResult geo = geocodingGateway.geocode(inputData.getLocationText());
            if (geo == null) {
                presenter.present(new SearchPlacesOutputData(
                        List.of(),
                        null,
                        false,
                        null,
                        "Could not find that location."
                ));
                return;
            }

            LocalDate date = LocalDate.parse(inputData.getDate());

            WeatherSummary weather = null;
            boolean weatherUsed = false;
            String weatherAdvice = null;

            try {
                weather = weatherGateway.getDailyWeather(geo.getLat(), geo.getLon(), date);
                if (weather != null) {
                    weatherUsed = true;
                    weatherAdvice = buildWeatherAdvice(weather);
                }
            } catch (Exception e) {
                // weather API failed – we just proceed without optimization
                weatherUsed = false;
                weatherAdvice = "Weather data unavailable. Results are not weather-optimized.";
            }

            List<Place> places = new ArrayList<>();
            Map<String, List<String>> selectedCategories = profile.getSelectedCategories();

            // If user has no interests, just search general places
            if (selectedCategories == null || selectedCategories.isEmpty()) {
                places.addAll(placesGateway.searchPlaces(
                        geo.getLat(), geo.getLon(), profile.getRadiusKm(), null
                ));
            } else {
                // For each interest, pull up to 5 places
                for (Map.Entry<String, List<String>> entry : selectedCategories.entrySet()) {
                    Map<String, List<String>> singleInterestMap = Map.of(entry.getKey(), entry.getValue());

                    List<Place> result = placesGateway.searchPlaces(
                            geo.getLat(), geo.getLon(), profile.getRadiusKm(), singleInterestMap
                    );

                    if (!result.isEmpty()) {
                        places.addAll(result.stream().limit(5).toList());
                    }
                }
            }

            if (places.isEmpty()) {
                presenter.present(new SearchPlacesOutputData(
                        List.of(),
                        geo.getFormattedAddress(),
                        weatherUsed,
                        weatherAdvice,
                        "No places found near this location. Try increasing radius or changing interests."
                ));
                return;
            }

            rankPlaces(places, selectedCategories, weather);
            presenter.present(new SearchPlacesOutputData(
                    places,
                    geo.getFormattedAddress(),
                    weatherUsed,
                    weatherAdvice,
                    null
            ));
        } catch (Exception e) {
            presenter.present(new SearchPlacesOutputData(
                    List.of(),
                    null,
                    false,
                    null,
                    e.getMessage()
            ));
        }
    }

    private void rankPlaces(List<Place> places,
                            Map<String, List<String>> selectedCategories,
                            WeatherSummary weather) {
        boolean wet = weather != null && weather.isPrecipitationLikely();
        places.sort(Comparator.comparingDouble((Place p) -> -scorePlace(p, selectedCategories, wet)));
    }

    private double scorePlace(Place place,
                              Map<String, List<String>> selectedCategories,
                              boolean wet) {
        double score = 0;

        if (selectedCategories != null && !selectedCategories.isEmpty()) {
            List<String> selectedSubCategories = selectedCategories.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            List<String> placeCategories = place.getCategories();
            for (String selectedCategory : selectedSubCategories) {
                for (String placeCategory : placeCategories) {
                    if (placeCategory.equals(selectedCategory)
                            || placeCategory.startsWith(selectedCategory)) {
                        score += 10;
                        break;
                    }
                }
            }
        }

        if (wet && place.getIndoorOutdoorType() == IndoorOutdoorType.INDOOR) {
            score += 5;
        }
        if (!wet && place.getIndoorOutdoorType() == IndoorOutdoorType.OUTDOOR) {
            score += 5;
        }

        score -= place.getDistanceKm();

        return score;
    }

    // Very similar style to your WeatherAdvicePanel: full sentence advice
    private String buildWeatherAdvice(WeatherSummary weather) {
        StringBuilder sb = new StringBuilder();

        double temp = weather.getTemperatureC();
        sb.append(String.format("Temperature is %.1f°C. ", temp));
        if (temp <= 5) {
            sb.append("It is cold — wear a warm jacket, maybe a hat and gloves. ");
        } else if (temp <= 15) {
            sb.append("A light jacket or sweater is recommended. ");
        } else if (temp <= 25) {
            sb.append("Comfortable temperature — light layers are fine. ");
        } else {
            sb.append("It is quite warm — wear light clothing and stay hydrated. ");
        }

        double uv = weather.getUvIndex();
        sb.append(String.format("UV index is %.1f. ", uv));
        if (uv >= 6) {
            sb.append("Use sunscreen, sunglasses, and consider a hat. ");
        } else if (uv >= 3) {
            sb.append("Sunscreen is a good idea if you will be outside. ");
        } else {
            sb.append("Sun exposure risk is low. ");
        }

        if (weather.isPrecipitationLikely()) {
            sb.append("Rain or snow is likely — bring an umbrella or waterproof jacket. ");
            sb.append("We are favouring indoor locations for you.");
        } else {
            sb.append("Rain is unlikely — good conditions to be outside. ");
            sb.append("We are favouring outdoor locations for you.");
        }

        return sb.toString().trim();
    }
}
