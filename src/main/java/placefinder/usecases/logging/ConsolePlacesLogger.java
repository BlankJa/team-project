package placefinder.usecases.logging;

import placefinder.entities.Place;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Console implementation of PlacesApiLogger for debugging.
 * Outputs detailed logs to System.out.
 *
 * SOLID Principles:
 * - Single Responsibility Principle (SRP): This class has one responsibility:
 *   logging API information to the console.
 * - Liskov Substitution Principle (LSP): This class can be substituted anywhere
 *   PlacesApiLogger is expected without breaking functionality.
 * - Open/Closed Principle (OCP): New logging behaviors can be added by creating
 *   new implementations of PlacesApiLogger, not by modifying this class.
 */
public class ConsolePlacesLogger implements PlacesApiLogger {

    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SEPARATOR = "=".repeat(80);

    @Override
    public void logSearchRequest(String location, String date) {
        // SRP: This method only handles logging the request
        System.out.println(SEPARATOR);
        System.out.println("[PLACES API DEBUG] Request started at: " +
                          LocalDateTime.now().format(TIME_FORMAT));
        System.out.println("  Location: " + location);
        System.out.println("  Date: " + date);
        System.out.println(SEPARATOR);
    }

    @Override
    public void logSearchResponse(List<Place> places, long responseTimeMs) {
        // SRP: This method only handles logging the response
        System.out.println(SEPARATOR);
        System.out.println("[PLACES API DEBUG] Response received at: " +
                          LocalDateTime.now().format(TIME_FORMAT));
        System.out.println("  Response time: " + responseTimeMs + " ms");
        System.out.println("  Number of places: " + (places != null ? places.size() : 0));

        if (places != null && !places.isEmpty()) {
            System.out.println("\n  Places returned:");
            for (int i = 0; i < places.size(); i++) {
                Place p = places.get(i);
                System.out.println("    " + (i + 1) + ". " + p.getName());
                System.out.println("       Address: " + p.getAddress());
                System.out.println("       Coordinates: (" + p.getLat() + ", " + p.getLon() + ")");
                System.out.println("       Distance: " + p.getDistanceKm() + " km");
                System.out.println("       Categories: " + p.getCategories());
                System.out.println("       Indoor/Outdoor: " + p.getIndoorOutdoorType());
                System.out.println();
            }
        }
        System.out.println(SEPARATOR);
    }

    @Override
    public void logError(String error) {
        // SRP: This method only handles logging errors
        System.err.println(SEPARATOR);
        System.err.println("[PLACES API ERROR] Error at: " +
                          LocalDateTime.now().format(TIME_FORMAT));
        System.err.println("  Error: " + error);
        System.err.println(SEPARATOR);
    }
}