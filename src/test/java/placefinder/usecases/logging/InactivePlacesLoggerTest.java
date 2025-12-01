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
 * Unit tests for {@link InactivePlacesLogger}.
 *
 * These tests verify that the inactive logger performs no operations
 * and produces no output, allowing the application to run without logging overhead.
 */
class InactivePlacesLoggerTest {

    private InactivePlacesLogger logger;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        logger = new InactivePlacesLogger();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void logSearchRequest_producesNoOutput() {
        // Arrange
        String location = "Toronto";
        String date = "2025-11-19";

        // Act
        logger.logSearchRequest(location, date);

        // Assert
        String stdOutput = outContent.toString();
        String errOutput = errContent.toString();
        assertTrue(stdOutput.isEmpty(), "Inactive logger should produce no stdout output");
        assertTrue(errOutput.isEmpty(), "Inactive logger should produce no stderr output");
    }

    @Test
    void logSearchResponse_withPlaces_producesNoOutput() {
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

        List<Place> places = List.of(place1);
        long responseTimeMs = 250;

        // Act
        logger.logSearchResponse(places, responseTimeMs);

        // Assert
        String stdOutput = outContent.toString();
        String errOutput = errContent.toString();
        assertTrue(stdOutput.isEmpty(), "Inactive logger should produce no stdout output");
        assertTrue(errOutput.isEmpty(), "Inactive logger should produce no stderr output");
    }

    @Test
    void logSearchResponse_withEmptyList_producesNoOutput() {
        // Arrange
        List<Place> places = new ArrayList<>();
        long responseTimeMs = 150;

        // Act
        logger.logSearchResponse(places, responseTimeMs);

        // Assert
        String stdOutput = outContent.toString();
        String errOutput = errContent.toString();
        assertTrue(stdOutput.isEmpty(), "Inactive logger should produce no stdout output");
        assertTrue(errOutput.isEmpty(), "Inactive logger should produce no stderr output");
    }

    @Test
    void logSearchResponse_withNullPlaces_producesNoOutput() {
        // Arrange
        long responseTimeMs = 100;

        // Act
        logger.logSearchResponse(null, responseTimeMs);

        // Assert
        String stdOutput = outContent.toString();
        String errOutput = errContent.toString();
        assertTrue(stdOutput.isEmpty(), "Inactive logger should produce no stdout output");
        assertTrue(errOutput.isEmpty(), "Inactive logger should produce no stderr output");
    }

    @Test
    void logError_producesNoOutput() {
        // Arrange
        String errorMessage = "API request failed: Connection timeout";

        // Act
        logger.logError(errorMessage);

        // Assert
        String stdOutput = outContent.toString();
        String errOutput = errContent.toString();
        assertTrue(stdOutput.isEmpty(), "Inactive logger should produce no stdout output");
        assertTrue(errOutput.isEmpty(), "Inactive logger should produce no stderr output");
    }

    @Test
    void logError_withNullMessage_producesNoOutput() {
        // Act
        logger.logError(null);

        // Assert
        String stdOutput = outContent.toString();
        String errOutput = errContent.toString();
        assertTrue(stdOutput.isEmpty(), "Inactive logger should produce no stdout output");
        assertTrue(errOutput.isEmpty(), "Inactive logger should produce no stderr output");
    }

    @Test
    void multipleCalls_producesNoOutput() {
        // Arrange
        Place place = new Place(
                "1",
                "Test Place",
                "Test Address",
                43.0,
                -79.0,
                1.0,
                IndoorOutdoorType.INDOOR,
                List.of("test")
        );

        // Act - make multiple calls
        logger.logSearchRequest("Toronto", "2025-11-19");
        logger.logSearchResponse(List.of(place), 200);
        logger.logError("Test error 1");
        logger.logSearchRequest("Montreal", "2025-11-20");
        logger.logError("Test error 2");

        // Assert
        String stdOutput = outContent.toString();
        String errOutput = errContent.toString();
        assertTrue(stdOutput.isEmpty(), "Inactive logger should produce no stdout output even after multiple calls");
        assertTrue(errOutput.isEmpty(), "Inactive logger should produce no stderr output even after multiple calls");
    }

    @Test
    void implementsPlacesApiLoggerInterface() {
        // Assert
        assertTrue(logger instanceof PlacesApiLogger,
                "InactivePlacesLogger should implement PlacesApiLogger interface");
    }

    @Test
    void canBeSubstitutedForPlacesApiLogger() {
        // This test verifies Liskov Substitution Principle (LSP)
        // The inactive logger should be substitutable anywhere PlacesApiLogger is expected

        // Arrange
        PlacesApiLogger apiLogger = new InactivePlacesLogger();

        // Act - use the interface reference
        apiLogger.logSearchRequest("Test", "2025-11-19");
        apiLogger.logSearchResponse(null, 100);
        apiLogger.logError("Test error");

        // Assert - should complete without throwing exceptions
        String stdOutput = outContent.toString();
        String errOutput = errContent.toString();
        assertTrue(stdOutput.isEmpty(), "Should produce no output when used via interface");
        assertTrue(errOutput.isEmpty(), "Should produce no output when used via interface");
    }
}