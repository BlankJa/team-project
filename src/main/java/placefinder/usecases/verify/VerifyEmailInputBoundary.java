package placefinder.usecases.verify;

/**
 * Input boundary for the Verify Email use case.
 */
public interface VerifyEmailInputBoundary {

    /**
     * Executes the verify-email use case.
     *
     * @param inputData data required to verify the user's email address
     */
    void execute(VerifyEmailInputData inputData);
}
