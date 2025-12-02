package placefinder.usecases.login;

/**
 * Input boundary interface for the Login use case.
 *
 * <p>This interface defines how the presentation layer (e.g., controllers)
 * initiates the login process. By depending on this boundary, the UI layer
 * can trigger the LoginInteractor without knowing any business logic
 * details, helping maintain Clean Architecture separation.</p>
 *
 * <p>Implementations of this interface should validate the input, perform
 * authentication, and pass results to the corresponding output boundary.</p>
 */
public interface LoginInputBoundary {

    /**
     * Execute the login use case using the provided input data.
     *
     * @param inputData the user's submitted login credentials (email + password)
     */
    void execute(LoginInputData inputData);
}
