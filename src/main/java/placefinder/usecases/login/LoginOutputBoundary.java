package placefinder.usecases.login;

/**
 * Output boundary interface for the Login use case.
 *
 * <p>This interface defines how the LoginInteractor communicates results
 * back to the presentation layer (e.g., a presenter or view model).
 * Implementations of this interface should format or adapt the
 * LoginOutputData into a form suitable for the UI layer.</p>
 *
 * <p>By depending on this boundary, the use case remains independent of
 * UI frameworks (Swing, JavaFX, etc.) and follows Clean Architecture
 * principles.</p>
 */
public interface LoginOutputBoundary {

    /**
     * Present the result of a login attempt.
     *
     * @param outputData The data returned by the login interactor,
     *                   containing information such as success status
     *                   and user details.
     */
    void present(LoginOutputData outputData);
}
