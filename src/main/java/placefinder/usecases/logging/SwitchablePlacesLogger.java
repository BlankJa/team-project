package placefinder.usecases.logging;

import placefinder.entities.Place;
import java.util.List;

/**
 * Switchable implementation of PlacesApiLogger that allows toggling
 * between active (console) and inactive logging at runtime.
 */
public class SwitchablePlacesLogger implements PlacesApiLogger {

    private PlacesApiLogger currentLogger;
    private boolean isActive;

    public SwitchablePlacesLogger(boolean startActive) {
        this.isActive = startActive;
        this.currentLogger = startActive ? new ConsolePlacesLogger() : new InactivePlacesLogger();
    }

    /**
     * Toggle between active (console) and inactive logging.
     */
    public void toggle() {
        isActive = !isActive;
        currentLogger = isActive ? new ConsolePlacesLogger() : new InactivePlacesLogger();
    }

    /**
     * Enable console logging.
     */
    public void enable() {
        if (!isActive) {
            isActive = true;
            currentLogger = new ConsolePlacesLogger();
        }
    }

    /**
     * Disable logging (use inactive logger).
     */
    public void disable() {
        if (isActive) {
            isActive = false;
            currentLogger = new InactivePlacesLogger();
        }
    }

    /**
     * Check if logging is currently active.
     * @return true if console logging is enabled, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void logSearchRequest(String location, String date) {
        currentLogger.logSearchRequest(location, date);
    }

    @Override
    public void logSearchResponse(List<Place> places, long responseTimeMs) {
        currentLogger.logSearchResponse(places, responseTimeMs);
    }

    @Override
    public void logError(String error) {
        currentLogger.logError(error);
    }
}