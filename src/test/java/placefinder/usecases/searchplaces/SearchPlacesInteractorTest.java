package placefinder.usecases.searchplaces;

import org.junit.jupiter.api.Test;
import placefinder.entities.*;
import placefinder.usecases.dataacessinterfaces.GeocodingDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PlacesDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.WeatherDataAccessInterface;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SearchPlacesInteractor}.
 *
 * Uses Mockito to mock gateways and entities, and a simple presenter that
 * captures the last SearchPlacesOutputData.
 */
class SearchPlacesInteractorTest {

    /**
     * Simple presenter that just stores the last output for assertions.
     */
    private static class CapturingPresenter implements SearchPlacesOutputBoundary {
        private SearchPlacesOutputData output;

        @Override
        public void present(SearchPlacesOutputData outputData) {
            this.output = outputData;
        }

        public SearchPlacesOutputData getOutput() {
            return output;
        }
    }

    @Test
    void geocodingFails_returnsErrorAndNoPlaces() throws Exception {
        // Arrange
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);

        // Geocoder returns null => location not found
        when(geocodingDataAccessInterface.geocode("NowhereLand")).thenReturn(null);

        CapturingPresenter presenter = new CapturingPresenter();

        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway,
                geocodingDataAccessInterface,
                placesDataAccessInterface,
                weatherDataAccessInterface,
                presenter
        );

        SearchPlacesInputData input =
                new SearchPlacesInputData(1, "NowhereLand", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertTrue(out.getPlaces().isEmpty(), "Places list should be empty");
        assertNull(out.getOriginAddress(), "Origin address should be null when location not found");
        assertFalse(out.isWeatherUsed(), "Weather should not be used when location not found");
        assertEquals("Could not find that location.", out.getErrorMessage());
    }

    @Test
    void noInterests_generalSearchWithNullCategories() throws Exception {
        // Arrange
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);
        when(profile.getSelectedCategories()).thenReturn(Map.of());  // no interests
        when(profile.getRadiusKm()).thenReturn(3.0);

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geocodingDataAccessInterface.geocode("Toronto")).thenReturn(geo);

        WeatherSummary weather = mock(WeatherSummary.class);
        when(weatherDataAccessInterface.getDailyWeather(43.65, -79.38, LocalDate.parse("2025-11-19")))
                .thenReturn(weather);
        when(weather.isPrecipitationLikely()).thenReturn(false);
        when(weather.getTemperatureC()).thenReturn(20.0);
        when(weather.getUvIndex()).thenReturn(5.0);

        Place place = mock(Place.class);
        when(place.getCategories()).thenReturn(List.of("poi"));
        when(place.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(place.getDistanceKm()).thenReturn(1.2);

        // when no interests, categories param is null
        when(placesDataAccessInterface.searchPlaces(
                anyDouble(), anyDouble(), anyDouble(), isNull())
        ).thenReturn(List.of(place));

        CapturingPresenter presenter = new CapturingPresenter();

        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway,
                geocodingDataAccessInterface,
                placesDataAccessInterface,
                weatherDataAccessInterface,
                presenter
        );

        SearchPlacesInputData input =
                new SearchPlacesInputData(1, "Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getErrorMessage(), "Should not have error");
        assertEquals("Toronto, ON", out.getOriginAddress());
        assertEquals(1, out.getPlaces().size());
        assertTrue(out.isWeatherUsed(), "Weather should be used when API returns data");
        assertNotNull(out.getWeatherAdvice());
        assertTrue(out.getWeatherAdvice().contains("Temperature is"),
                "Advice should include temperature sentence");
    }

    @Test
    void wetWeather_favoursIndoorPlacesAndAdviceMentionsIndoor() throws Exception {
        // Arrange
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);
        when(profile.getRadiusKm()).thenReturn(2.0);
        when(profile.getSelectedCategories())
                .thenReturn(Map.of("leisure", List.of("park")));

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geocodingDataAccessInterface.geocode("Toronto")).thenReturn(geo);

        WeatherSummary weather = mock(WeatherSummary.class);
        when(weatherDataAccessInterface.getDailyWeather(43.65, -79.38, LocalDate.parse("2025-11-19")))
                .thenReturn(weather);
        when(weather.isPrecipitationLikely()).thenReturn(true); // WET
        when(weather.getTemperatureC()).thenReturn(8.0);
        when(weather.getUvIndex()).thenReturn(2.5);

        // Create two places: one indoor, one outdoor, same distance, same category
        Place indoor = mock(Place.class);
        when(indoor.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.INDOOR);
        when(indoor.getCategories()).thenReturn(List.of("leisure.park"));
        when(indoor.getDistanceKm()).thenReturn(1.0);

        Place outdoor = mock(Place.class);
        when(outdoor.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(outdoor.getCategories()).thenReturn(List.of("leisure.park"));
        when(outdoor.getDistanceKm()).thenReturn(1.0);

        // Interactor will call searchPlaces once per interest; we just return both
        when(placesDataAccessInterface.searchPlaces(
                anyDouble(), anyDouble(), anyDouble(), anyMap())
        ).thenReturn(List.of(indoor, outdoor));

        CapturingPresenter presenter = new CapturingPresenter();

        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway,
                geocodingDataAccessInterface,
                placesDataAccessInterface,
                weatherDataAccessInterface,
                presenter
        );

        SearchPlacesInputData input =
                new SearchPlacesInputData(1, "Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getErrorMessage(), "Should not have error");
        assertTrue(out.isWeatherUsed(), "Weather should be used");
        assertNotNull(out.getWeatherAdvice());

        // Advice should say we favour indoor locations in wet weather
        assertTrue(out.getWeatherAdvice().contains("favouring indoor"),
                "Advice should mention favouring indoor locations");

        // First place should be the indoor one (higher score due to wet)
        assertEquals(indoor, out.getPlaces().get(0),
                "Indoor place should be ranked first in wet weather");
    }

    @Test
    void dryWeather_favoursOutdoorPlacesAndAdviceMentionsOutdoor() throws Exception {
        // Arrange
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);
        when(profile.getRadiusKm()).thenReturn(2.0);
        when(profile.getSelectedCategories())
                .thenReturn(Map.of("leisure", List.of("park")));

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geocodingDataAccessInterface.geocode("Toronto")).thenReturn(geo);

        WeatherSummary weather = mock(WeatherSummary.class);
        when(weatherDataAccessInterface.getDailyWeather(43.65, -79.38, LocalDate.parse("2025-11-19")))
                .thenReturn(weather);
        when(weather.isPrecipitationLikely()).thenReturn(false); // DRY
        when(weather.getTemperatureC()).thenReturn(22.0);
        when(weather.getUvIndex()).thenReturn(7.0);

        Place indoor = mock(Place.class);
        when(indoor.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.INDOOR);
        when(indoor.getCategories()).thenReturn(List.of("leisure.park"));
        when(indoor.getDistanceKm()).thenReturn(1.0);

        Place outdoor = mock(Place.class);
        when(outdoor.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(outdoor.getCategories()).thenReturn(List.of("leisure.park"));
        when(outdoor.getDistanceKm()).thenReturn(1.0);

        when(placesDataAccessInterface.searchPlaces(
                anyDouble(), anyDouble(), anyDouble(), anyMap())
        ).thenReturn(List.of(indoor, outdoor));

        CapturingPresenter presenter = new CapturingPresenter();

        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway,
                geocodingDataAccessInterface,
                placesDataAccessInterface,
                weatherDataAccessInterface,
                presenter
        );

        SearchPlacesInputData input =
                new SearchPlacesInputData(1, "Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getErrorMessage());
        assertTrue(out.isWeatherUsed());
        assertNotNull(out.getWeatherAdvice());
        assertTrue(out.getWeatherAdvice().contains("favouring outdoor"),
                "Advice should mention favouring outdoor locations");

        // Outdoor place should be ranked first in dry weather
        assertEquals(outdoor, out.getPlaces().get(0),
                "Outdoor place should be ranked first in dry weather");
    }

    @Test
    void weatherApiFailure_stillReturnsPlacesWithWeatherUnavailableMessage() throws Exception {
        // Arrange
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);
        when(profile.getRadiusKm()).thenReturn(2.0);
        when(profile.getSelectedCategories()).thenReturn(Map.of());

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geocodingDataAccessInterface.geocode("Toronto")).thenReturn(geo);

        // Weather gateway throws exception
        when(weatherDataAccessInterface.getDailyWeather(anyDouble(), anyDouble(), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Weather API down"));

        Place place = mock(Place.class);
        when(place.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.INDOOR);
        when(place.getCategories()).thenReturn(List.of("poi"));
        when(place.getDistanceKm()).thenReturn(0.5);

        when(placesDataAccessInterface.searchPlaces(
                anyDouble(), anyDouble(), anyDouble(), any())
        ).thenReturn(List.of(place));

        CapturingPresenter presenter = new CapturingPresenter();

        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway,
                geocodingDataAccessInterface,
                placesDataAccessInterface,
                weatherDataAccessInterface,
                presenter
        );

        SearchPlacesInputData input =
                new SearchPlacesInputData(1, "Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertFalse(out.isWeatherUsed(), "Weather should not be marked as used when API fails");
        assertEquals("Weather data unavailable. Results are not weather-optimized.",
                out.getWeatherAdvice());
        assertNotNull(out.getPlaces());
        assertFalse(out.getPlaces().isEmpty(), "Places should still be returned");
        assertNull(out.getErrorMessage(), "Should not be treated as a hard error");
    }

    @Test
    void noPlacesFound_returnsFriendlyError() throws Exception {
        // Arrange
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);
        when(profile.getRadiusKm()).thenReturn(2.0);
        when(profile.getSelectedCategories()).thenReturn(Map.of());

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geocodingDataAccessInterface.geocode("Toronto")).thenReturn(geo);

        // Weather is fine
        WeatherSummary weather = mock(WeatherSummary.class);
        when(weatherDataAccessInterface.getDailyWeather(anyDouble(), anyDouble(), any(LocalDate.class)))
                .thenReturn(weather);
        when(weather.isPrecipitationLikely()).thenReturn(false);
        when(weather.getTemperatureC()).thenReturn(15.0);
        when(weather.getUvIndex()).thenReturn(3.0);

        // No places returned at all
        when(placesDataAccessInterface.searchPlaces(
                anyDouble(), anyDouble(), anyDouble(), any())
        ).thenReturn(List.of());

        CapturingPresenter presenter = new CapturingPresenter();

        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway,
                geocodingDataAccessInterface,
                placesDataAccessInterface,
                weatherDataAccessInterface,
                presenter
        );

        SearchPlacesInputData input =
                new SearchPlacesInputData(1, "Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertTrue(out.getPlaces().isEmpty());
        assertEquals("Toronto, ON", out.getOriginAddress());
        assertEquals("No places found near this location. Try increasing radius or changing interests.",
                out.getErrorMessage());
    }
}
