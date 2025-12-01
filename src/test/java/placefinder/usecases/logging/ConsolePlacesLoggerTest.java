package placefinder.usecases.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import placefinder.entities.IndoorOutdoorType;
import placefinder.entities.Place;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ConsolePlacesLogger}.
 *
 * These tests capture System.out and System.err to verify that
 * the logger outputs the correct information to the console.
 */
class ConsolePlacesLoggerTest {

    private ConsolePlacesLogger logger;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        logger = new ConsolePlacesLogger();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void logSearchRequest_outputsLocationAndDate() {
        // Arrange
        String location = "Toronto";
        String date = "2025-11-19";

        // Act
        logger.logSearchRequest(location, date);

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("PLACES API DEBUG"), "Should contain log prefix");
        assertTrue(output.contains("Request started at:"), "Should mention request start");
        assertTrue(output.contains("Location: " + location), "Should contain location");
        assertTrue(output.contains("Date: " + date), "Should contain date");
        assertTrue(output.contains("=".repeat(80)), "Should contain separator");
    }

    @Test
    void logSearchResponse_withPlaces_outputsPlaceDetails() {
        // Arrange
        Place place1 = new Place(
                "1",
                "CN Tower",
                "301 Front St W, Toronto",
                43.6426,
                -79.3871,
                1.5,
                IndoorOutdoorType.MIXED,
                List.of("tourism.attraction")
        );

        Place place2 = new Place(
                "2",
                "Ripley's Aquarium",
                "288 Bremner Blvd, Toronto",
                43.6424,
                -79.3860,
                1.2,
                IndoorOutdoorType.INDOOR,
                List.of("entertainment")
        );

        List<Place> places = List.of(place1, place2);
        long responseTimeMs = 250;

        // Act
        logger.logSearchResponse(places, responseTimeMs);

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("PLACES API DEBUG"), "Should contain log prefix");
        assertTrue(output.contains("Response received at:"), "Should mention response received");
        assertTrue(output.contains("Response time: " + responseTimeMs + " ms"), "Should contain response time");
        assertTrue(output.contains("Number of places: 2"), "Should contain place count");
        assertTrue(output.contains("CN Tower"), "Should contain first place name");
        assertTrue(output.contains("Ripley's Aquarium"), "Should contain second place name");
        assertTrue(output.contains("301 Front St W, Toronto"), "Should contain first place address");
        assertTrue(output.contains("1.5 km"), "Should contain distance");
        assertTrue(output.contains("tourism.attraction"), "Should contain categories");
        assertTrue(output.contains("MIXED"), "Should contain indoor/outdoor type");
    }

    @Test
    void logSearchResponse_withEmptyList_outputsZeroPlaces() {
        // Arrange
        List<Place> places = new ArrayList<>();
        long responseTimeMs = 150;

        // Act
        logger.logSearchResponse(places, responseTimeMs);

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Number of places: 0"), "Should show zero places");
        assertFalse(output.contains("Places returned:"),
                "Should not show 'Places returned:' section for empty list");
    }

    @Test
    void logSearchResponse_withNullPlaces_outputsZeroPlaces() {
        // Arrange
        long responseTimeMs = 100;

        // Act
        logger.logSearchResponse(null, responseTimeMs);

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("Number of places: 0"), "Should show zero places for null list");
        assertFalse(output.contains("Places returned:"),
                "Should not show 'Places returned:' section for null list");
    }

    @Test
    void logError_outputsToSystemErr() {
        // Arrange
        String errorMessage = "API request failed: Connection timeout";

        // Act
        logger.logError(errorMessage);

        // Assert
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("PLACES API ERROR"), "Should contain error prefix");
        assertTrue(errorOutput.contains("Error at:"), "Should mention error timestamp");
        assertTrue(errorOutput.contains("Error: " + errorMessage), "Should contain error message");
        assertTrue(errorOutput.contains("=".repeat(80)), "Should contain separator");

        // Ensure nothing was written to stdout
        String stdOutput = outContent.toString();
        assertTrue(stdOutput.isEmpty(), "Error should go to stderr, not stdout");
    }

    @Test
    void logError_withNullMessage_handlesGracefully() {
        // Act
        logger.logError(null);

        // Assert
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("PLACES API ERROR"), "Should contain error prefix");
        assertTrue(errorOutput.contains("Error: null"), "Should handle null error message");
    }

    @Test
    void multipleCalls_produceSeparateLogEntries() {
        // Act
        logger.logSearchRequest("Toronto", "2025-11-19");
        logger.logSearchResponse(List.of(), 100);
        logger.logError("Test error");

        // Assert
        String stdOutput = outContent.toString();
        String errOutput = errContent.toString();

        // Count separators in stdout (2 from request and response)
        int stdSeparatorCount = (stdOutput.length() - stdOutput.replace("=".repeat(80), "").length())
                / "=".repeat(80).length();
        assertTrue(stdSeparatorCount >= 4, "Should have at least 4 separators in stdout (2 per log entry)");

        // Count separators in stderr (2 from error)
        int errSeparatorCount = (errOutput.length() - errOutput.replace("=".repeat(80), "").length())
                / "=".repeat(80).length();
        assertTrue(errSeparatorCount >= 2, "Should have at least 2 separators in stderr");
    }
}