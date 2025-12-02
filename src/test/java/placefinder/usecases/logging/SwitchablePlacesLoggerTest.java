package placefinder.usecases.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import placefinder.entities.IndoorOutdoorType;
import placefinder.entities.Place;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SwitchablePlacesLogger}.
 *
 * These tests verify that the switchable logger correctly delegates
 * to the appropriate logger implementation based on its active state.
 */
class SwitchablePlacesLoggerTest {

    private SwitchablePlacesLogger logger;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void constructor_withStartActiveTrue_createsActiveLogger() {
        // Act
        logger = new SwitchablePlacesLogger(true);

        // Assert
        assertTrue(logger.isActive(), "Logger should be active when constructed with true");
    }

    @Test
    void constructor_withStartActiveFalse_createsInactiveLogger() {
        // Act
        logger = new SwitchablePlacesLogger(false);

        // Assert
        assertFalse(logger.isActive(), "Logger should be inactive when constructed with false");
    }

    @Test
    void constructor_withStartActiveTrue_delegatesToConsoleLogger() {
        // Arrange
        logger = new SwitchablePlacesLogger(true);

        // Act
        logger.logSearchRequest("Toronto", "2025-11-19");

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("PLACES API DEBUG"), "Should output to console when active");
        assertTrue(output.contains("Toronto"), "Should contain location");
    }

    @Test
    void constructor_withStartActiveFalse_delegatesToInactiveLogger() {
        // Arrange
        logger = new SwitchablePlacesLogger(false);

        // Act
        logger.logSearchRequest("Toronto", "2025-11-19");

        // Assert
        String output = outContent.toString();
        assertTrue(output.isEmpty(), "Should not output to console when inactive");
    }

    @Test
    void toggle_fromActiveToInactive_disablesLogging() {
        // Arrange
        logger = new SwitchablePlacesLogger(true);
        assertTrue(logger.isActive(), "Should start active");

        // Act
        logger.toggle();

        // Assert
        assertFalse(logger.isActive(), "Should be inactive after toggle");

        // Verify no output after toggle
        logger.logSearchRequest("Toronto", "2025-11-19");
        String output = outContent.toString();
        assertTrue(output.isEmpty(), "Should not output after toggling to inactive");
    }

    @Test
    void toggle_fromInactiveToActive_enablesLogging() {
        // Arrange
        logger = new SwitchablePlacesLogger(false);
        assertFalse(logger.isActive(), "Should start inactive");

        // Act
        logger.toggle();

        // Assert
        assertTrue(logger.isActive(), "Should be active after toggle");

        // Verify output after toggle
        logger.logSearchRequest("Toronto", "2025-11-19");
        String output = outContent.toString();
        assertTrue(output.contains("PLACES API DEBUG"), "Should output after toggling to active");
    }

    @Test
    void toggle_multipleTimes_switchesStateCorrectly() {
        // Arrange
        logger = new SwitchablePlacesLogger(true);

        // Act & Assert
        assertTrue(logger.isActive(), "Should start active");

        logger.toggle();
        assertFalse(logger.isActive(), "Should be inactive after first toggle");

        logger.toggle();
        assertTrue(logger.isActive(), "Should be active after second toggle");

        logger.toggle();
        assertFalse(logger.isActive(), "Should be inactive after third toggle");
    }

    @Test
    void enable_whenAlreadyActive_doesNothing() {
        // Arrange
        logger = new SwitchablePlacesLogger(true);
        assertTrue(logger.isActive(), "Should start active");

        // Act
        logger.enable();

        // Assert
        assertTrue(logger.isActive(), "Should remain active");

        // Verify it still logs correctly
        logger.logSearchRequest("Toronto", "2025-11-19");
        String output = outContent.toString();
        assertTrue(output.contains("PLACES API DEBUG"), "Should still output to console");
    }

    @Test
    void enable_whenInactive_activatesLogging() {
        // Arrange
        logger = new SwitchablePlacesLogger(false);
        assertFalse(logger.isActive(), "Should start inactive");

        // Act
        logger.enable();

        // Assert
        assertTrue(logger.isActive(), "Should be active after enable");

        // Verify it logs after enabling
        logger.logSearchRequest("Toronto", "2025-11-19");
        String output = outContent.toString();
        assertTrue(output.contains("PLACES API DEBUG"), "Should output after enabling");
    }

    @Test
    void disable_whenAlreadyInactive_doesNothing() {
        // Arrange
        logger = new SwitchablePlacesLogger(false);
        assertFalse(logger.isActive(), "Should start inactive");

        // Act
        logger.disable();

        // Assert
        assertFalse(logger.isActive(), "Should remain inactive");

        // Verify it still doesn't log
        logger.logSearchRequest("Toronto", "2025-11-19");
        String output = outContent.toString();
        assertTrue(output.isEmpty(), "Should still not output to console");
    }

    @Test
    void disable_whenActive_deactivatesLogging() {
        // Arrange
        logger = new SwitchablePlacesLogger(true);
        assertTrue(logger.isActive(), "Should start active");

        // Act
        logger.disable();

        // Assert
        assertFalse(logger.isActive(), "Should be inactive after disable");

        // Verify it doesn't log after disabling
        logger.logSearchRequest("Toronto", "2025-11-19");
        String output = outContent.toString();
        assertTrue(output.isEmpty(), "Should not output after disabling");
    }

    @Test
    void logSearchRequest_whenActive_delegatesToConsoleLogger() {
        // Arrange
        logger = new SwitchablePlacesLogger(true);
        String location = "Toronto";
        String date = "2025-11-19";

        // Act
        logger.logSearchRequest(location, date);

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("PLACES API DEBUG"), "Should contain log prefix");
        assertTrue(output.contains("Location: " + location), "Should contain location");
        assertTrue(output.contains("Date: " + date), "Should contain date");
    }

    @Test
    void logSearchRequest_whenInactive_doesNotOutput() {
        // Arrange
        logger = new SwitchablePlacesLogger(false);

        // Act
        logger.logSearchRequest("Toronto", "2025-11-19");

        // Assert
        String output = outContent.toString();
        assertTrue(output.isEmpty(), "Should not output when inactive");
    }

    @Test
    void logSearchResponse_whenActive_delegatesToConsoleLogger() {
        // Arrange
        logger = new SwitchablePlacesLogger(true);
        Place place = new Place(
                "1",
                "CN Tower",
                "301 Front St W, Toronto",
                43.6426,
                -79.3871,
                1.5,
                IndoorOutdoorType.MIXED,
                List.of("tourism.attraction")
        );
        List<Place> places = List.of(place);
        long responseTimeMs = 250;

        // Act
        logger.logSearchResponse(places, responseTimeMs);

        // Assert
        String output = outContent.toString();
        assertTrue(output.contains("PLACES API DEBUG"), "Should contain log prefix");
        assertTrue(output.contains("Response time: " + responseTimeMs + " ms"), "Should contain response time");
        assertTrue(output.contains("Number of places: 1"), "Should contain place count");
        assertTrue(output.contains("CN Tower"), "Should contain place name");
    }

    @Test
    void logSearchResponse_whenInactive_doesNotOutput() {
        // Arrange
        logger = new SwitchablePlacesLogger(false);
        Place place = new Place(
                "1",
                "CN Tower",
                "301 Front St W, Toronto",
                43.6426,
                -79.3871,
                1.5,
                IndoorOutdoorType.MIXED,
                List.of("tourism.attraction")
        );
        List<Place> places = List.of(place);

        // Act
        logger.logSearchResponse(places, 250);

        // Assert
        String output = outContent.toString();
        assertTrue(output.isEmpty(), "Should not output when inactive");
    }

    @Test
    void logError_whenActive_delegatesToConsoleLogger() {
        // Arrange
        logger = new SwitchablePlacesLogger(true);
        String errorMessage = "API request failed";

        // Act
        logger.logError(errorMessage);

        // Assert
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("PLACES API ERROR"), "Should contain error prefix");
        assertTrue(errorOutput.contains("Error: " + errorMessage), "Should contain error message");
    }

    @Test
    void logError_whenInactive_doesNotOutput() {
        // Arrange
        logger = new SwitchablePlacesLogger(false);

        // Act
        logger.logError("API request failed");

        // Assert
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.isEmpty(), "Should not output error when inactive");
    }

    @Test
    void isActive_reflectsCurrentState() {
        // Arrange & Act & Assert
        logger = new SwitchablePlacesLogger(true);
        assertTrue(logger.isActive(), "Should be active initially");

        logger.disable();
        assertFalse(logger.isActive(), "Should be inactive after disable");

        logger.enable();
        assertTrue(logger.isActive(), "Should be active after enable");

        logger.toggle();
        assertFalse(logger.isActive(), "Should be inactive after toggle");
    }

    @Test
    void complexScenario_switchingBetweenStates() {
        // Arrange
        logger = new SwitchablePlacesLogger(true);

        // Act & Assert - Initial active state
        logger.logSearchRequest("Toronto", "2025-11-19");
        String output1 = outContent.toString();
        assertTrue(output1.contains("Toronto"), "Should log when active");
        outContent.reset();

        // Disable
        logger.disable();
        logger.logSearchRequest("Montreal", "2025-11-20");
        String output2 = outContent.toString();
        assertTrue(output2.isEmpty(), "Should not log when disabled");

        // Enable
        logger.enable();
        logger.logSearchRequest("Vancouver", "2025-11-21");
        String output3 = outContent.toString();
        assertTrue(output3.contains("Vancouver"), "Should log after re-enabling");
        outContent.reset();

        // Toggle to inactive
        logger.toggle();
        logger.logSearchRequest("Calgary", "2025-11-22");
        String output4 = outContent.toString();
        assertTrue(output4.isEmpty(), "Should not log after toggling to inactive");

        // Toggle back to active
        logger.toggle();
        logger.logSearchRequest("Ottawa", "2025-11-23");
        String output5 = outContent.toString();
        assertTrue(output5.contains("Ottawa"), "Should log after toggling back to active");
    }
}