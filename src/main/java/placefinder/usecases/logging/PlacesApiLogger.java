package placefinder.usecases.logging;

import placefinder.entities.Place;
import java.util.List;

/**
 * Logger interface for debugging Places API calls.
 *
 * SOLID Principles:
 * - Interface Segregation Principle (ISP): This interface contains only the methods
 *   needed for logging Places API operations, nothing more.
 * - Dependency Inversion Principle (DIP): High-level modules (use cases) will depend
 *   on this abstraction rather than concrete logger implementations.
 */
public interface PlacesApiLogger {

    /**
     * Log the start of a places search request.
     * @param location The location being searched
     * @param date The date for the search
     */
    void logSearchRequest(String location, String date);

    /**
     * Log the API response containing places.
     * @param places The list of places returned from the API
     * @param responseTimeMs The time taken for the API call in milliseconds
     */
    void logSearchResponse(List<Place> places, long responseTimeMs);

    /**
     * Log an error that occurred during the API call.
     * @param error The error message or exception
     */
    void logError(String error);
}