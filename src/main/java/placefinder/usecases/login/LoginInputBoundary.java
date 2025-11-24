package placefinder.usecases.login;

/**
 * Input boundary interface for the login use case.
 * Defines the contract for executing login operations.
 */
public interface LoginInputBoundary {
    void execute(LoginInputData inputData);
}
