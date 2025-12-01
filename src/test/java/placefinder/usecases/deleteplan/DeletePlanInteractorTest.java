package placefinder.usecases.deleteplan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.usecases.ports.PlanGateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DeletePlanInteractor}.
 *
 * <ul>
 *     <li>Happy path – the gateway successfully deletes the plan.</li>
 *     <li>Failure path – the gateway throws an exception that must be
 *         translated into a user-facing error message.</li>
 * </ul>
 *
 * <p>Design notes:
 * <ul>
 *     <li>External collaborators ({@link PlanGateway} and
 *         {@link DeletePlanOutputBoundary}) are mocked.</li>
 *     <li>The interactor itself is treated as a pure application service:
 *         no database, UI, or framework concerns appear in these tests.</li>
 * </ul>
 */
class DeletePlanInteractorTest {

    // -------------------------------------------------------------------------
    // Collaborators and System Under Test
    // -------------------------------------------------------------------------

    /** Gateway abstraction used to perform the actual delete operation. */
    private PlanGateway planGateway;

    /** Presenter responsible for the turning output data into a view model. */
    private DeletePlanOutputBoundary presenter;

    /** System under test (SUT). */
    private DeletePlanInteractor interactor;

    // -------------------------------------------------------------------------
    // Test lifecycle
    // -------------------------------------------------------------------------

    /**
     * Creates fresh mocks and a new interactor instance before each test.
     * This keeps tests isolated and avoids cross-test interference.
     */
    @BeforeEach
    void setUp() {
        planGateway = mock(PlanGateway.class);
        presenter   = mock(DeletePlanOutputBoundary.class);
        interactor  = new DeletePlanInteractor(planGateway, presenter);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Helper method to capture the {@link DeletePlanOutputData} object sent
     * to the presenter during the execution of the interactor.
     */
    private DeletePlanOutputData capturePresenterOutput() {
        ArgumentCaptor<DeletePlanOutputData> captor =
                ArgumentCaptor.forClass(DeletePlanOutputData.class);
        verify(presenter).present(captor.capture());
        return captor.getValue();
    }

    // -------------------------------------------------------------------------
    // Test cases
    // -------------------------------------------------------------------------

    /**
     * Happy-path scenario:
     *
     * <p>When the gateway successfully deletes the plan (no exception thrown),
     * the interactor should:
     * <ul>
     *     <li>Invoke {@link PlanGateway#deletePlan(int, int)} once with the
     *         exact plan and user identifiers provided in the input.</li>
     *     <li>Notify the presenter with a successful {@link DeletePlanOutputData}
     *         instance and the message <em>"Plan deleted."</em>.</li>
     * </ul>
     */
    @Test
    void successfulDeletion_callsGatewayAndPresentsSuccess() throws Exception {
        // Arrange
        int planId = 42;
        int userId = 7;
        DeletePlanInputData input = new DeletePlanInputData(planId, userId);

        // No stubbing required – the default behaviour is "do nothing / no exception".

        // Act
        interactor.execute(input);

        // Assert – the interactor must delegate to the gateway with the same ids
        verify(planGateway).deletePlan(planId, userId);

        // And it must inform the presenter of success with the standard message
        DeletePlanOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
        assertEquals("Plan deleted.", out.getMessage());
    }

    /**
     * Variant of the happy path with different identifiers, to document that
     * the interactor simply passes through whatever IDs it is given.
     *
     * <p>This is mostly a behavioural documentation test – it makes the intent
     * explicit that no additional validation or transformation of IDs is done
     * inside the interactor.
     */
    @Test
    void delegatesExactIdsToGateway_noAdditionalTransformation() throws Exception {
        // Arrange – deliberately use non-trivial values
        int planId = 999;
        int userId = 12345;
        DeletePlanInputData input = new DeletePlanInputData(planId, userId);

        // Act
        interactor.execute(input);

        // Assert – we expect a single call with the exact same arguments
        verify(planGateway, times(1)).deletePlan(planId, userId);

        DeletePlanOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
        assertEquals("Plan deleted.", out.getMessage());
    }

    /**
     * Error-path scenario:
     *
     * <p>If the {@link PlanGateway} throws an exception while trying to delete
     * the plan, the interactor should:
     * <ul>
     *     <li>Catch the exception (rather than letting it propagate).</li>
     *     <li>Produce an output data object with {@code success == false}.</li>
     *     <li>Surface the exception's message as the user-facing error message.</li>
     * </ul>
     *
     * <p>This test specifically verifies that infrastructure failures are
     * translated into a safe, controlled response for the caller.
     */
    @Test
    void gatewayThrowsException_presentsFailureWithMessage() throws Exception {
        // Arrange
        int planId = 10;
        int userId = 20;
        DeletePlanInputData input = new DeletePlanInputData(planId, userId);

        // Simulate a persistence-layer failure
        doThrow(new Exception("Database unavailable"))
                .when(planGateway).deletePlan(planId, userId);

        // Act
        interactor.execute(input);

        // Assert – gateway was still invoked exactly once with the same ids
        verify(planGateway).deletePlan(planId, userId);

        // And the presenter must receive a failure result reflecting the error
        DeletePlanOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Database unavailable", out.getMessage());
    }

    /**
     * Edge-case variation of the error path:
     *
     * <p>Some exceptions may not provide a message (i.e., {@code getMessage()}
     * returns {@code null}). This test ensures that such a case is still
     * handled without throwing a secondary error inside the interactor.
     */
    @Test
    void gatewayThrowsExceptionWithNullMessage_presentsFailureWithNullMessage() throws Exception {
        // Arrange
        int planId = 1;
        int userId = 2;
        DeletePlanInputData input = new DeletePlanInputData(planId, userId);

        // Exception with a null message
        Exception ex = new Exception((String) null);
        doThrow(ex).when(planGateway).deletePlan(planId, userId);

        // Act
        interactor.execute(input);

        // Assert
        DeletePlanOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertNull(out.getMessage());
    }
}