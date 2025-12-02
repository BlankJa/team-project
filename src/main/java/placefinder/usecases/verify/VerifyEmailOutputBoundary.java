package placefinder.usecases.verify;

/**
 * Output boundary for the Verify Email use case.
 */
public interface VerifyEmailOutputBoundary {

    /**
     * Presents the result of the verify-email operation.
     *
     * @param outputData the data to present to the UI layer
     */
    void present(VerifyEmailOutputData outputData);
}
