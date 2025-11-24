package placefinder.usecases.login;

/**
 * Output boundary interface for the login use case.
 * Defines the contract for presenting login results.
 */
public interface LoginOutputBoundary {
    void present(LoginOutputData outputData);
}
