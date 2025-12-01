package placefinder.usecases.getplandetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.Plan;
import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GetPlanDetailsInteractor}.
 *
 * <p>The tests treat the interactor as a pure application-layer service:
 * <ul>
 *     <li>The persistence interface {@link PlanDataAccessInterface} is mocked.</li>
 *     <li>The output boundary {@link GetPlanDetailsOutputBoundary} is mocked.</li>
 *     <li>The domain entity {@link Plan} is created as a real object,
 *         avoiding any ByteBuddy / mocking issues with Java 25.</li>
 * </ul>
 */
class GetPlanDetailsInteractorTest {

    // -------------------------------------------------------------------------
    // Collaborators & System Under Test
    // -------------------------------------------------------------------------

    /** Gateway abstraction used to load plans (mock). */
    private PlanDataAccessInterface planDataAccessInterface;

    /** Presenter used to forward output data to the UI layer (mock). */
    private GetPlanDetailsOutputBoundary presenter;

    /** System under test (SUT). */
    private GetPlanDetailsInteractor interactor;

    // -------------------------------------------------------------------------
    // Test lifecycle
    // -------------------------------------------------------------------------

    /**
     * Creates a fresh set of mocks and a new interactor instance before each test.
     * This ensures that tests remain isolated and side-effect free.
     */
    @BeforeEach
    void setUp() {
        planDataAccessInterface = mock(PlanDataAccessInterface.class);
        presenter   = mock(GetPlanDetailsOutputBoundary.class);
        interactor  = new GetPlanDetailsInteractor(planDataAccessInterface, presenter);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Captures the single {@link GetPlanDetailsOutputData} instance that is
     * passed to the presenter during interactor execution.
     */
    private GetPlanDetailsOutputData capturePresenterOutput() {
        ArgumentCaptor<GetPlanDetailsOutputData> captor =
                ArgumentCaptor.forClass(GetPlanDetailsOutputData.class);
        verify(presenter).present(captor.capture());
        return captor.getValue();
    }

    /**
     * Creates a minimal but valid {@link Plan} instance for test purposes.
     * Only the constructor parameters are populated; we do not care about
     * domain correctness here, just identity and non-null behaviour.
     */
    private Plan createSamplePlan(int id) {
        return new Plan(
                id,
                99,                            // userId
                "Weekend Trip",                // name
                LocalDate.of(2025, 1, 1),      // date
                LocalTime.of(9, 0),            // startTime
                "123 Test Street",             // originAddress
                null,                          // route (not needed here)
                5.0,                           // snapshotRadiusKm
                Collections.emptyMap()         // snapshotCategories
        );
    }

    // -------------------------------------------------------------------------
    // Test cases – success paths
    // -------------------------------------------------------------------------

    /**
     * Happy-path scenario:
     *
     * <p>When the gateway returns a non-null {@link Plan}, the interactor should:
     * <ul>
     *     <li>Call {@link PlanDataAccessInterface#findPlanWithStops(int)} with the plan ID
     *         provided in {@link GetPlanDetailsInputData}.</li>
     *     <li>Forward a {@link GetPlanDetailsOutputData} instance to the presenter
     *         where {@code getPlan()} is that plan and {@code getErrorMessage()}
     *         is {@code null}.</li>
     * </ul>
     */
    @Test
    void planExists_presentsPlanWithoutError() throws Exception {
        // Arrange
        int planId = 42;
        GetPlanDetailsInputData input = new GetPlanDetailsInputData(planId);
        Plan plan = createSamplePlan(planId);

        when(planDataAccessInterface.findPlanWithStops(planId)).thenReturn(plan);

        // Act
        interactor.execute(input);

        // Assert – gateway called exactly once with the same planId
        verify(planDataAccessInterface, times(1)).findPlanWithStops(planId);

        // And presenter receives a successful output (plan set, no error)
        GetPlanDetailsOutputData out = capturePresenterOutput();
        assertSame(plan, out.getPlan(), "Interactor should pass through the same Plan instance.");
        assertNull(out.getErrorMessage(), "Error message must be null when plan is found.");
    }

    /**
     * Behaviour-documentation variant of the happy path:
     *
     * <p>This test emphasises that the interactor performs no transformation on
     * the identifier; it simply forwards whatever {@code planId} it is given
     * to the gateway. Using a different value from the previous test makes this
     * intent explicit.
     */
    @Test
    void forwardsExactPlanIdToGateway() throws Exception {
        // Arrange – deliberately choose a different, non-default identifier
        int planId = 999;
        GetPlanDetailsInputData input = new GetPlanDetailsInputData(planId);
        Plan plan = createSamplePlan(planId);

        when(planDataAccessInterface.findPlanWithStops(planId)).thenReturn(plan);

        // Act
        interactor.execute(input);

        // Assert – gateway is called with the exact same id
        verify(planDataAccessInterface).findPlanWithStops(planId);

        GetPlanDetailsOutputData out = capturePresenterOutput();
        assertSame(plan, out.getPlan());
        assertNull(out.getErrorMessage());
    }

    // -------------------------------------------------------------------------
    // Test cases – plan not found
    // -------------------------------------------------------------------------

    /**
     * Missing-plan scenario:
     *
     * <p>If the gateway returns {@code null} for the given plan ID, the interactor
     * must:
     * <ul>
     *     <li>Call the gateway with the requested ID.</li>
     *     <li>Inform the presenter that no plan was found by sending an output
     *         object where {@code plan == null} and
     *         {@code errorMessage == "Plan not found."}.</li>
     * </ul>
     */
    @Test
    void planDoesNotExist_presentsNullPlanWithNotFoundMessage() throws Exception {
        // Arrange
        int planId = 123;
        GetPlanDetailsInputData input = new GetPlanDetailsInputData(planId);

        when(planDataAccessInterface.findPlanWithStops(planId)).thenReturn(null);

        // Act
        interactor.execute(input);

        // Assert – gateway still invoked
        verify(planDataAccessInterface).findPlanWithStops(planId);

        // Presenter should receive a null plan and a user-friendly error
        GetPlanDetailsOutputData out = capturePresenterOutput();
        assertNull(out.getPlan(), "Plan should be null when not found.");
        assertEquals("Plan not found.", out.getErrorMessage());
    }

    // -------------------------------------------------------------------------
    // Test cases – exception handling
    // -------------------------------------------------------------------------

    /**
     * Error-path scenario:
     *
     * <p>If {@link PlanDataAccessInterface#findPlanWithStops(int)} throws an exception, the
     * interactor should:
     * <ul>
     *     <li>Catch the exception (not allow it to propagate).</li>
     *     <li>Provide an output object where {@code plan == null}.</li>
     *     <li>Surface the exception message via {@code getErrorMessage()}.</li>
     * </ul>
     *
     * <p>This ensures that infrastructure failures are translated into a safe,
     * predictable response for the caller.
     */
    @Test
    void gatewayThrowsException_presentsErrorMessageAndNullPlan() throws Exception {
        // Arrange
        int planId = 55;
        GetPlanDetailsInputData input = new GetPlanDetailsInputData(planId);

        doThrow(new Exception("Database is temporarily unavailable"))
                .when(planDataAccessInterface).findPlanWithStops(planId);

        // Act
        interactor.execute(input);

        // Assert – gateway was called even though it failed
        verify(planDataAccessInterface).findPlanWithStops(planId);

        // Presenter receives a failure-style output: no plan, error message set
        GetPlanDetailsOutputData out = capturePresenterOutput();
        assertNull(out.getPlan(), "Plan must be null when an exception occurs.");
        assertEquals("Database is temporarily unavailable", out.getErrorMessage());
    }

    /**
     * Edge-case variation of exception handling:
     *
     * <p>Some exceptions may not provide a descriptive message
     * (i.e., {@code getMessage()} returns {@code null}). This test verifies that
     * the interactor still behaves safely in that case and does not attempt to
     * further manipulate the null message (e.g., by concatenation).
     */
    @Test
    void gatewayThrowsExceptionWithNullMessage_presentsNullErrorMessage() throws Exception {
        // Arrange
        int planId = 77;
        GetPlanDetailsInputData input = new GetPlanDetailsInputData(planId);

        Exception ex = new Exception((String) null);
        doThrow(ex).when(planDataAccessInterface).findPlanWithStops(planId);

        // Act
        interactor.execute(input);

        // Assert
        GetPlanDetailsOutputData out = capturePresenterOutput();
        assertNull(out.getPlan());
        assertNull(out.getErrorMessage(),
                "Error message should remain null if the exception message is null.");
    }
}