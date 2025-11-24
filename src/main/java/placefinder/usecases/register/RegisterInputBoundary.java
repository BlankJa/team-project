package placefinder.usecases.register;

/**
 * Input boundary interface for the registration use case.
 * Defines the contract for executing registration operations.
 */
public interface RegisterInputBoundary {
    void execute(RegisterInputData inputData);
}
