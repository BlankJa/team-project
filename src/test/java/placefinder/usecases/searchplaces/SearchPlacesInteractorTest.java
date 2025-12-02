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
        // changed to hit temp <= 5 branch
        when(weather.getTemperatureC()).thenReturn(2.0);
        when(weather.getUvIndex()).thenReturn(2.5);

        // Create two places: one indoor, one outdoor, same distance, same category
        Place indoor = mock(Place.class);
        when(indoor.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.INDOOR);
        // changed to simple "park" to trigger category match branch
        when(indoor.getCategories()).thenReturn(List.of("park"));
        when(indoor.getDistanceKm()).thenReturn(1.0);

        Place outdoor = mock(Place.class);
        when(outdoor.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(outdoor.getCategories()).thenReturn(List.of("park"));
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
        // changed to hit temp > 25 branch
        when(weather.getTemperatureC()).thenReturn(30.0);
        when(weather.getUvIndex()).thenReturn(7.0);

        Place indoor = mock(Place.class);
        when(indoor.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.INDOOR);
        when(indoor.getCategories()).thenReturn(List.of("park"));
        when(indoor.getDistanceKm()).thenReturn(1.0);

        Place outdoor = mock(Place.class);
        when(outdoor.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(outdoor.getCategories()).thenReturn(List.of("park_outdoor"));
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

    // -------------------------------------------------------------------------
    // NEW TESTS BELOW – to hit remaining branches / lines
    // -------------------------------------------------------------------------

    @Test
    void weatherReturnsNull_weatherNotUsedAndNoAdvice() throws Exception {
        // covers: weather != null IF false (without exception)
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);
        when(profile.getRadiusKm()).thenReturn(2.0);
        when(profile.getSelectedCategories()).thenReturn(Map.of());

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.0);
        when(geo.getLon()).thenReturn(-79.0);
        when(geo.getFormattedAddress()).thenReturn("Somewhere");
        when(geocodingDataAccessInterface.geocode("Toronto")).thenReturn(geo);

        // Weather returns null (no exception)
        when(weatherDataAccessInterface.getDailyWeather(43.0, -79.0, LocalDate.parse("2025-11-19")))
                .thenReturn(null);

        Place place = mock(Place.class);
        when(place.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(place.getCategories()).thenReturn(List.of("poi"));
        when(place.getDistanceKm()).thenReturn(1.0);

        when(placesDataAccessInterface.searchPlaces(
                anyDouble(), anyDouble(), anyDouble(), any())
        ).thenReturn(List.of(place));

        CapturingPresenter presenter = new CapturingPresenter();
        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway, geocodingDataAccessInterface, placesDataAccessInterface, weatherDataAccessInterface, presenter
        );

        SearchPlacesInputData input = new SearchPlacesInputData(1, "Toronto", "2025-11-19");

        interactor.execute(input);

        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertFalse(out.isWeatherUsed());
        assertNull(out.getWeatherAdvice());
        assertNull(out.getErrorMessage());
        assertEquals(1, out.getPlaces().size());
    }

    @Test
    void selectedCategoriesNull_usesGeneralSearchBranch() throws Exception {
        // covers: selectedCategories == null part of (selectedCategories == null || isEmpty())
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);
        when(profile.getRadiusKm()).thenReturn(3.0);
        when(profile.getSelectedCategories()).thenReturn(null);   // <-- null, not empty map

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.5);
        when(geo.getLon()).thenReturn(-79.4);
        when(geo.getFormattedAddress()).thenReturn("Some City");
        when(geocodingDataAccessInterface.geocode("Toronto")).thenReturn(geo);

        WeatherSummary weather = mock(WeatherSummary.class);
        when(weatherDataAccessInterface.getDailyWeather(43.5, -79.4, LocalDate.parse("2025-11-19")))
                .thenReturn(weather);
        when(weather.isPrecipitationLikely()).thenReturn(false);
        when(weather.getTemperatureC()).thenReturn(18.0);
        when(weather.getUvIndex()).thenReturn(4.0);

        Place place = mock(Place.class);
        when(place.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(place.getCategories()).thenReturn(List.of("poi"));
        when(place.getDistanceKm()).thenReturn(1.0);

        when(placesDataAccessInterface.searchPlaces(
                anyDouble(), anyDouble(), anyDouble(), isNull())
        ).thenReturn(List.of(place));

        CapturingPresenter presenter = new CapturingPresenter();
        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway, geocodingDataAccessInterface, placesDataAccessInterface, weatherDataAccessInterface, presenter
        );

        SearchPlacesInputData input = new SearchPlacesInputData(1, "Toronto", "2025-11-19");
        interactor.execute(input);

        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertEquals("Some City", out.getOriginAddress());
        assertEquals(1, out.getPlaces().size());
        assertTrue(out.isWeatherUsed());
        assertNull(out.getErrorMessage());
    }

    @Test
    void selectedCategoriesNonEmptyButSearchResultsEmpty_hitsEmptyResultBranch() throws Exception {
        // covers: if (!result.isEmpty()) false branch inside for-each
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);
        when(profile.getRadiusKm()).thenReturn(5.0);
        when(profile.getSelectedCategories())
                .thenReturn(Map.of("leisure", List.of("park")));

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(1.0);
        when(geo.getLon()).thenReturn(2.0);
        when(geo.getFormattedAddress()).thenReturn("EmptyLand");
        when(geocodingDataAccessInterface.geocode("Toronto")).thenReturn(geo);

        WeatherSummary weather = mock(WeatherSummary.class);
        when(weatherDataAccessInterface.getDailyWeather(1.0, 2.0, LocalDate.parse("2025-11-19")))
                .thenReturn(weather);
        when(weather.isPrecipitationLikely()).thenReturn(false);
        when(weather.getTemperatureC()).thenReturn(16.0);
        when(weather.getUvIndex()).thenReturn(3.0);

        // For each interest, searchPlaces returns empty list
        when(placesDataAccessInterface.searchPlaces(
                anyDouble(), anyDouble(), anyDouble(), anyMap())
        ).thenReturn(List.of());

        CapturingPresenter presenter = new CapturingPresenter();
        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway, geocodingDataAccessInterface, placesDataAccessInterface, weatherDataAccessInterface, presenter
        );

        SearchPlacesInputData input = new SearchPlacesInputData(1, "Toronto", "2025-11-19");
        interactor.execute(input);

        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertTrue(out.getPlaces().isEmpty());
        assertEquals("EmptyLand", out.getOriginAddress());
        assertEquals("No places found near this location. Try increasing radius or changing interests.",
                out.getErrorMessage());
    }

    @Test
    void preferenceLoadThrows_outerCatchHandlesException() throws Exception {
        // covers: outer try-catch catch-block
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        // loadForUser throws
        when(prefGateway.loadForUser(1)).thenThrow(new RuntimeException("DB error"));

        CapturingPresenter presenter = new CapturingPresenter();
        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway, geocodingDataAccessInterface, placesDataAccessInterface, weatherDataAccessInterface, presenter
        );

        SearchPlacesInputData input = new SearchPlacesInputData(1, "Toronto", "2025-11-19");
        interactor.execute(input);

        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertTrue(out.getPlaces().isEmpty());
        assertNull(out.getOriginAddress());
        assertFalse(out.isWeatherUsed());
        assertNull(out.getWeatherAdvice());
        assertEquals("DB error", out.getErrorMessage());
    }

    @Test
    void categoryDoesNotMatch_selectedCategories_nonMatchingPlaceGetsNoCategoryScore() throws Exception {
        // Arrange
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);
        when(profile.getRadiusKm()).thenReturn(5.0);
        // non-empty selected categories so we enter the scoring loop
        when(profile.getSelectedCategories())
                .thenReturn(Map.of("leisure", List.of("park")));

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geocodingDataAccessInterface.geocode("Toronto")).thenReturn(geo);

        // Dry weather so wet == false in rankPlaces
        WeatherSummary weather = mock(WeatherSummary.class);
        when(weatherDataAccessInterface.getDailyWeather(43.65, -79.38, LocalDate.parse("2025-11-19")))
                .thenReturn(weather);
        when(weather.isPrecipitationLikely()).thenReturn(false);
        when(weather.getTemperatureC()).thenReturn(18.0);
        when(weather.getUvIndex()).thenReturn(4.0);

        // One place that matches "park", one that doesn't match at all
        Place matching = mock(Place.class);
        when(matching.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(matching.getCategories()).thenReturn(List.of("park")); // matches selected category
        when(matching.getDistanceKm()).thenReturn(1.0);

        Place nonMatching = mock(Place.class);
        when(nonMatching.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(nonMatching.getCategories()).thenReturn(List.of("museum")); // does NOT equal or startWith "park"
        when(nonMatching.getDistanceKm()).thenReturn(1.0);

        // searchPlaces returns both; ranking will call scorePlace on both
        when(placesDataAccessInterface.searchPlaces(
                anyDouble(), anyDouble(), anyDouble(), anyMap())
        ).thenReturn(List.of(matching, nonMatching));

        CapturingPresenter presenter = new CapturingPresenter();
        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway, geocodingDataAccessInterface,
                placesDataAccessInterface, weatherDataAccessInterface, presenter
        );

        SearchPlacesInputData input =
                new SearchPlacesInputData(1, "Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getErrorMessage());
        assertEquals(2, out.getPlaces().size());

        // The matching place should score higher (gets +10 for category match),
        // so it should be ranked first.
        assertEquals(matching, out.getPlaces().get(0));
        assertEquals(nonMatching, out.getPlaces().get(1));
    }
    @Test
    void noSelectedCategories_stillRanksPlacesByDistance() throws Exception {
        // Arrange
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);
        when(profile.getRadiusKm()).thenReturn(5.0);
        // IMPORTANT: null, not map, and we want scorePlace to be called
        when(profile.getSelectedCategories()).thenReturn(null);

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geocodingDataAccessInterface.geocode("Toronto")).thenReturn(geo);

        // Weather is null so 'wet' == false in rankPlaces
        when(weatherDataAccessInterface.getDailyWeather(
                43.65, -79.38, LocalDate.parse("2025-11-19")
        )).thenReturn(null);

        // Two places so the comparator (and thus scorePlace) is actually invoked
        Place closer = mock(Place.class);
        when(closer.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(closer.getCategories()).thenReturn(List.of("poi"));
        when(closer.getDistanceKm()).thenReturn(1.0);

        Place farther = mock(Place.class);
        when(farther.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(farther.getCategories()).thenReturn(List.of("poi"));
        when(farther.getDistanceKm()).thenReturn(3.0);

        when(placesDataAccessInterface.searchPlaces(
                anyDouble(), anyDouble(), anyDouble(), isNull())
        ).thenReturn(List.of(closer, farther));

        CapturingPresenter presenter = new CapturingPresenter();
        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway, geocodingDataAccessInterface,
                placesDataAccessInterface, weatherDataAccessInterface, presenter
        );

        SearchPlacesInputData input =
                new SearchPlacesInputData(1, "Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getErrorMessage());
        assertEquals("Toronto, ON", out.getOriginAddress());
        assertEquals(2, out.getPlaces().size());

        // With no selected categories and dry weather, score is basically -distance,
        // so the closer place should be ranked first.
        assertEquals(closer, out.getPlaces().get(0));
        assertEquals(farther, out.getPlaces().get(1));
    }

    @Test
    void emptySelectedCategories_stillRanksPlacesByDistance() throws Exception {
        // Arrange
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        PlacesDataAccessInterface placesDataAccessInterface = mock(PlacesDataAccessInterface.class);
        WeatherDataAccessInterface weatherDataAccessInterface = mock(WeatherDataAccessInterface.class);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(prefGateway.loadForUser(1)).thenReturn(profile);
        when(profile.getRadiusKm()).thenReturn(5.0);
        // IMPORTANT: non-null but empty
        when(profile.getSelectedCategories()).thenReturn(Map.of());

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geocodingDataAccessInterface.geocode("Toronto")).thenReturn(geo);

        // Weather can be anything; we just need rankPlaces to run
        WeatherSummary weather = mock(WeatherSummary.class);
        when(weatherDataAccessInterface.getDailyWeather(
                43.65, -79.38, LocalDate.parse("2025-11-19")))
                .thenReturn(weather);
        when(weather.isPrecipitationLikely()).thenReturn(false);
        when(weather.getTemperatureC()).thenReturn(18.0);
        when(weather.getUvIndex()).thenReturn(4.0);

        // TWO places so the comparator (scorePlace) is invoked
        Place closer = mock(Place.class);
        when(closer.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(closer.getCategories()).thenReturn(List.of("poi"));
        when(closer.getDistanceKm()).thenReturn(1.0);

        Place farther = mock(Place.class);
        when(farther.getIndoorOutdoorType()).thenReturn(IndoorOutdoorType.OUTDOOR);
        when(farther.getCategories()).thenReturn(List.of("poi"));
        when(farther.getDistanceKm()).thenReturn(3.0);

        // Because selectedCategories is empty, execute() calls searchPlaces with null categories
        when(placesDataAccessInterface.searchPlaces(
                anyDouble(), anyDouble(), anyDouble(), isNull())
        ).thenReturn(List.of(closer, farther));

        CapturingPresenter presenter = new CapturingPresenter();
        SearchPlacesInteractor interactor = new SearchPlacesInteractor(
                prefGateway, geocodingDataAccessInterface,
                placesDataAccessInterface, weatherDataAccessInterface, presenter
        );

        SearchPlacesInputData input =
                new SearchPlacesInputData(1, "Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        SearchPlacesOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getErrorMessage());
        assertEquals("Toronto, ON", out.getOriginAddress());
        assertEquals(2, out.getPlaces().size());

        // With no selected categories, score is basically -distance,
        // so the closer place should be ranked first.
        assertEquals(closer, out.getPlaces().get(0));
        assertEquals(farther, out.getPlaces().get(1));
    }


}
