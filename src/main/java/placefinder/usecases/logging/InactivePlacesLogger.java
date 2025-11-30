package placefinder.usecases.logging;

import placefinder.entities.Place;
import java.util.List;

/**
 * Inactive implementation of PlacesApiLogger.
 * This logger lets the application to run without logging overhead.
 *
 * SOLID Principles:
 * - Single Responsibility Principle (SRP): This class has one responsibility -
 *   providing a no-op logger implementation.
 * - Liskov Substitution Principle (LSP): This class can be substituted anywhere
 *   PlacesApiLogger is expected. The program works identically with or without logging.
 * - Open/Closed Principle (OCP): The system can switch between logging and no-logging
 *   without modifying existing code, just by changing the injected implementation.
 */
public class InactivePlacesLogger implements PlacesApiLogger {

    @Override
    public void logSearchRequest(String location, String date) {
        // No operation - allows program to run without any logging
    }

    @Override
    public void logSearchResponse(List<Place> places, long responseTimeMs) {
        // No operation - allows program to run without any logging
    }

    @Override
    public void logError(String error) {
        // No operation - allows program to run without any logging
    }
}