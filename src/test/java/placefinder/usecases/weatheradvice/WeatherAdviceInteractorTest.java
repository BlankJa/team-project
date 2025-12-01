package placefinder.usecases.weatheradvice;

import org.junit.jupiter.api.Test;
import placefinder.entities.GeocodeResult;
import placefinder.entities.WeatherSummary;
import placefinder.usecases.ports.GeocodingGateway;
import placefinder.usecases.ports.WeatherGateway;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WeatherAdviceInteractor}.
 *
 * NOTE: These tests are written to be robust against small wording changes
 * in the natural-language advice. They focus on the logical effect
 * (e.g., "cold branch chosen", "rain branch chosen") using keywords,
 * not exact full sentences.
 */
class WeatherAdviceInteractorTest {

    /**
     * Simple presenter that captures the last output for assertions.
     */
    private static class CapturingPresenter implements WeatherAdviceOutputBoundary {
        private WeatherAdviceOutputData output;

        @Override
        public void present(WeatherAdviceOutputData outputData) {
            this.output = outputData;
        }

        public WeatherAdviceOutputData getOutput() {
            return output;
        }
    }

    @Test
    void geocodingFails_returnsError() throws Exception {
        // Arrange
        GeocodingGateway geocodingGateway = mock(GeocodingGateway.class);
        WeatherGateway weatherGateway = mock(WeatherGateway.class);

        // location not found
        when(geocodingGateway.geocode("NowhereLand")).thenReturn(null);

        CapturingPresenter presenter = new CapturingPresenter();
        WeatherAdviceInteractor interactor =
                new WeatherAdviceInteractor(geocodingGateway, weatherGateway, presenter);

        WeatherAdviceInputData input =
                new WeatherAdviceInputData("NowhereLand", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        WeatherAdviceOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getSummary());
        assertNull(out.getAdvice());
        assertEquals("Could not find that location.", out.getErrorMessage());
    }

    @Test
    void weatherNull_returnsWeatherUnavailableError() throws Exception {
        // Arrange
        GeocodingGateway geocodingGateway = mock(GeocodingGateway.class);
        WeatherGateway weatherGateway = mock(WeatherGateway.class);

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");

        when(geocodingGateway.geocode("Toronto")).thenReturn(geo);

        // Weather gateway returns null
        when(weatherGateway.getDailyWeather(
                anyDouble(), anyDouble(), any(LocalDate.class))
        ).thenReturn(null);

        CapturingPresenter presenter = new CapturingPresenter();
        WeatherAdviceInteractor interactor =
                new WeatherAdviceInteractor(geocodingGateway, weatherGateway, presenter);

        WeatherAdviceInputData input =
                new WeatherAdviceInputData("Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        WeatherAdviceOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getSummary());
        assertNull(out.getAdvice());
        assertEquals("Unable to retrieve weather data at the moment.", out.getErrorMessage());
    }

    @Test
    void coldWetLowUv_generatesWarmClothesAndRainAdvice() throws Exception {
        // Arrange
        GeocodingGateway geocodingGateway = mock(GeocodingGateway.class);
        WeatherGateway weatherGateway = mock(WeatherGateway.class);

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");

        when(geocodingGateway.geocode("Toronto")).thenReturn(geo);

        WeatherSummary weather = mock(WeatherSummary.class);
        when(weather.getTemperatureC()).thenReturn(2.0);      // cold branch
        when(weather.getUvIndex()).thenReturn(1.0);           // low UV: no UV message
        when(weather.getConditions()).thenReturn("Snow");
        when(weather.isPrecipitationLikely()).thenReturn(true); // rain/snow branch

        when(weatherGateway.getDailyWeather(
                43.65, -79.38, LocalDate.parse("2025-11-19"))
        ).thenReturn(weather);

        CapturingPresenter presenter = new CapturingPresenter();
        WeatherAdviceInteractor interactor =
                new WeatherAdviceInteractor(geocodingGateway, weatherGateway, presenter);

        WeatherAdviceInputData input =
                new WeatherAdviceInputData("Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        WeatherAdviceOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getErrorMessage());

        String summary = out.getSummary();
        String advice = out.getAdvice();

        assertNotNull(summary);
        assertFalse(summary.isBlank());
        assertNotNull(advice);
        assertFalse(advice.isBlank());

        String adviceLower = advice.toLowerCase();

        // Check that it's clearly about cold + layering
        assertTrue(
                adviceLower.contains("cold")
                        || adviceLower.contains("warm jacket")
                        || adviceLower.contains("jacket"),
                "Advice should mention cold / warm jacket / jacket"
        );

        // Check rain/snow hint is present
        assertTrue(
                adviceLower.contains("rain or snow")
                        || adviceLower.contains("umbrella")
                        || adviceLower.contains("waterproof"),
                "Advice should mention rain/snow or umbrella/waterproof"
        );

        // Low UV -> should NOT contain explicit 'UV index is high'
        assertFalse(
                adviceLower.contains("uv index is high"),
                "High UV message should not appear for low UV"
        );
    }

    @Test
    void hotHighUvNoPrecip_generatesLightClothesAndHighUvAdvice() throws Exception {
        // Arrange
        GeocodingGateway geocodingGateway = mock(GeocodingGateway.class);
        WeatherGateway weatherGateway = mock(WeatherGateway.class);

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geocodingGateway.geocode("Toronto")).thenReturn(geo);

        WeatherSummary weather = mock(WeatherSummary.class);
        when(weather.getTemperatureC()).thenReturn(30.0);     // warm/hot branch
        when(weather.getUvIndex()).thenReturn(7.0);           // high UV branch
        when(weather.getConditions()).thenReturn("Sunny");
        when(weather.isPrecipitationLikely()).thenReturn(false);

        when(weatherGateway.getDailyWeather(
                43.65, -79.38, LocalDate.parse("2025-11-19"))
        ).thenReturn(weather);

        CapturingPresenter presenter = new CapturingPresenter();
        WeatherAdviceInteractor interactor =
                new WeatherAdviceInteractor(geocodingGateway, weatherGateway, presenter);

        WeatherAdviceInputData input =
                new WeatherAdviceInputData("Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        WeatherAdviceOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getErrorMessage());
        assertNotNull(out.getSummary());

        String advice = out.getAdvice();
        assertNotNull(advice);
        assertFalse(advice.isBlank());

        String adviceLower = advice.toLowerCase();

        // Should mention heat / light clothing
        assertTrue(
                adviceLower.contains("quite warm")
                        || adviceLower.contains("warm")
                        || adviceLower.contains("light, breathable")
                        || adviceLower.contains("light clothing"),
                "Advice should mention it is warm/hot or light clothing"
        );

        // High UV branch
        assertTrue(
                adviceLower.contains("uv index is high")
                        || (adviceLower.contains("uv") && adviceLower.contains("sunscreen")),
                "Advice should mention high UV or sunscreen due to strong sun"
        );

        // No precipitation warning
        assertFalse(
                adviceLower.contains("rain or snow is expected"),
                "Should not mention 'rain or snow is expected' when precipitation is unlikely"
        );
    }

    @Test
    void nullOrBlankDate_usesTodayButStillProducesAdvice() throws Exception {
        // Arrange
        GeocodingGateway geocodingGateway = mock(GeocodingGateway.class);
        WeatherGateway weatherGateway = mock(WeatherGateway.class);

        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getLat()).thenReturn(43.65);
        when(geo.getLon()).thenReturn(-79.38);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geocodingGateway.geocode("Toronto")).thenReturn(geo);

        WeatherSummary weather = mock(WeatherSummary.class);
        when(weather.getTemperatureC()).thenReturn(20.0);
        when(weather.getUvIndex()).thenReturn(3.5);
        when(weather.getConditions()).thenReturn("Cloudy");
        when(weather.isPrecipitationLikely()).thenReturn(false);

        when(weatherGateway.getDailyWeather(
                eq(43.65), eq(-79.38), any(LocalDate.class))
        ).thenReturn(weather);

        CapturingPresenter presenter = new CapturingPresenter();
        WeatherAdviceInteractor interactor =
                new WeatherAdviceInteractor(geocodingGateway, weatherGateway, presenter);

        // Using null date → should default to today
        WeatherAdviceInputData input =
                new WeatherAdviceInputData("Toronto", null);

        // Act
        interactor.execute(input);

        // Assert
        WeatherAdviceOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getErrorMessage());

        String summary = out.getSummary();
        String advice = out.getAdvice();

        assertNotNull(summary);
        assertFalse(summary.isBlank());
        assertNotNull(advice);
        assertFalse(advice.isBlank());

        // We don't assert exact date text, only structure & key parts
        String summaryLower = summary.toLowerCase();
        assertTrue(summaryLower.contains("location:"), "Summary should contain a 'Location:' line");
        assertTrue(summaryLower.contains("temperature:"), "Summary should contain a 'Temperature:' line");
        assertTrue(summaryLower.contains("uv index"), "Summary should mention 'UV Index'");

        // And that the location string from geocode appears somewhere
        assertTrue(summary.contains("Toronto"),
                "Summary should contain the geocoded location name (Toronto)");
    }

    @Test
    void exceptionThrown_resultsInErrorMessage() throws Exception {
        // Arrange
        GeocodingGateway geocodingGateway = mock(GeocodingGateway.class);
        WeatherGateway weatherGateway = mock(WeatherGateway.class);

        when(geocodingGateway.geocode("Toronto"))
                .thenThrow(new RuntimeException("Geocoding service not reachable"));

        CapturingPresenter presenter = new CapturingPresenter();
        WeatherAdviceInteractor interactor =
                new WeatherAdviceInteractor(geocodingGateway, weatherGateway, presenter);

        WeatherAdviceInputData input =
                new WeatherAdviceInputData("Toronto", "2025-11-19");

        // Act
        interactor.execute(input);

        // Assert
        WeatherAdviceOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getSummary());
        assertNull(out.getAdvice());
        assertEquals("Geocoding service not reachable", out.getErrorMessage());
    }
}
