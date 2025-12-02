package placefinder.usecases.getroutedetails;

import placefinder.entities.Leg;
import java.util.List;

/**
 * Output data for the Get Route Details use case.
 *
 * <p>This class bundles the list of legs (each containing steps) with an
 * optional error message.  Passing the domain objects directly allows the
 * presenter to decide how to transform them for the UI.  If an error
 * occurred, {@code legs} will be {@code null} and {@code errorMessage}
 * will describe the failure.</p>
 */
public class GetRouteDetailsOutputData {
    private final List<Leg> legs;
    private final String errorMessage;

    /**
     * Construct output data with either legs or an error message.
     *
     * @param legs         the list of legs to present; may be {@code null} on error
     * @param errorMessage a message explaining the failure; {@code null} if successful
     */
    public GetRouteDetailsOutputData(List<Leg> legs, String errorMessage) {
        this.legs = legs;
        this.errorMessage = errorMessage;
    }

    /**
     * @return the legs comprising the route, or {@code null} if there was an error
     */
    public List<Leg> getLegs() {
        return legs;
    }

    /**
     * @return a message describing any error that occurred, or {@code null}
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}