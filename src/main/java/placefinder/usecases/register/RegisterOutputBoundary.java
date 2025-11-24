package placefinder.usecases.register;

/**
 * Output boundary interface for the registration use case.
 * Defines the contract for presenting registration results.
 */
public interface RegisterOutputBoundary {
    void present(RegisterOutputData outputData);
}
