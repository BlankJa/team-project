package placefinder.usecases.listplans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.Plan;
import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for {@link ListPlansInteractor}.
 *
 * <p>Design approach:
 * <ul>
 *     <li>Only infrastructure-style dependencies are mocked
 *         ({@link PlanDataAccessInterface} and {@link ListPlansOutputBoundary}).</li>
 *     <li>Domain objects such as {@link Plan} are instantiated as real
 *         instances (not mocked), to avoid issues with class instrumentation
 *         and to better reflect real-world usage.</li>
 *     <li>The tests follow an Arrange–Act–Assert structure and use explicit
 *         assertions to verify both behaviour and data.</li>
 * </ul>
 */
class ListPlansInteractorTest {

    // -------------------------------------------------------------------------
    // Collaborators and System Under Test
    // -------------------------------------------------------------------------

    /** Data access abstraction used to load plans for a user (mock). */
    private PlanDataAccessInterface planDataAccessInterface;

    /** Presenter that receives output data for the view layer (mock). */
    private ListPlansOutputBoundary presenter;

    /** System under test: orchestrates the list-plans use case. */
    private ListPlansInteractor interactor;

    // -------------------------------------------------------------------------
    // Test lifecycle
    // -------------------------------------------------------------------------

    /**
     * Creates a fresh set of mocks and a new interactor for each test case.
     * This ensures test isolation and avoids shared mutable state.
     */
    @BeforeEach
    void setUp() {
        planDataAccessInterface = mock(PlanDataAccessInterface.class);
        presenter   = mock(ListPlansOutputBoundary.class);
        interactor  = new ListPlansInteractor(planDataAccessInterface, presenter);
    }

    // -------------------------------------------------------------------------
    // Utility methods
    // -------------------------------------------------------------------------

    /**
     * Captures the single {@link ListPlansOutputData} object that is sent to
     * the presenter during an interactor invocation.
     *
     * <p>All tests use this helper to keep verification concise and consistent.
     */
    private ListPlansOutputData capturePresenterOutput() {
        ArgumentCaptor<ListPlansOutputData> captor =
                ArgumentCaptor.forClass(ListPlansOutputData.class);
        verify(presenter).present(captor.capture());
        return captor.getValue();
    }

    /**
     * Helper factory method that constructs a minimal but valid {@link Plan}.
     *
     * <p>The goal is not to exercise domain rules, but to have structurally
     * valid entities that resemble what the gateway would return in production.
     *
     * @param id     unique identifier for the plan
     * @param userId owning user identifier
     * @param name   plan name for readability in debugging
     * @return fully constructed {@link Plan} instance
     */
    private Plan createPlan(int id, int userId, String name) {
        return new Plan(
                id,
                userId,
                name,
                LocalDate.of(2025, 1, 1),
                LocalTime.of(9, 0),
                "123 Test Street",
                null,                       // route is not relevant here
                5.0,
                Collections.emptyMap()
        );
    }

    // -------------------------------------------------------------------------
    // Success-path scenarios
    // -------------------------------------------------------------------------

    /**
     * Baseline success scenario:
     *
     * <p>For a user with no saved plans, the gateway returns an empty list.
     * The interactor should:
     * <ul>
     *     <li>Call {@link PlanDataAccessInterface#findPlansByUser(int)} with the correct userId.</li>
     *     <li>Forward an output object containing that empty list.</li>
     *     <li>Ensure {@code errorMessage} is {@code null} to signal success.</li>
     * </ul>
     */
    @Test
    @DisplayName("User with no plans -> empty list, no error")
    void userHasNoPlans_returnsEmptyListAndNoError() throws Exception {
        // Arrange
        int userId = 101;
        ListPlansInputData input = new ListPlansInputData(userId);

        List<Plan> emptyPlans = List.of();
        when(planDataAccessInterface.findPlansByUser(userId)).thenReturn(emptyPlans);

        // Act
        interactor.execute(input);

        // Assert
        verify(planDataAccessInterface, times(1)).findPlansByUser(userId);

        ListPlansOutputData out = capturePresenterOutput();
        assertNotNull(out.getPlans(), "Plans list must not be null on success.");
        assertTrue(out.getPlans().isEmpty(), "Plans list should be empty for a user with no plans.");
        assertSame(emptyPlans, out.getPlans(),
                "Interactor should pass through the same list instance from the gateway.");
        assertNull(out.getErrorMessage(), "Error message must be null on a successful retrieval.");
    }

    /**
     * Success scenario with actual data:
     *
     * <p>For a user with multiple plans, the interactor should:
     * <ul>
     *     <li>Return exactly the plans provided by the gateway (no filtering
     *         or transformation).</li>
     *     <li>Set {@code errorMessage} to {@code null}.</li>
     * </ul>
     */
    @Test
    @DisplayName("User with multiple plans -> list returned, no error")
    void userHasMultiplePlans_returnsPlansAndNoError() throws Exception {
        // Arrange
        int userId = 202;
        ListPlansInputData input = new ListPlansInputData(userId);

        Plan plan1 = createPlan(1, userId, "Morning Coffee Tour");
        Plan plan2 = createPlan(2, userId, "Afternoon Museum Walk");
        List<Plan> plans = List.of(plan1, plan2);

        when(planDataAccessInterface.findPlansByUser(userId)).thenReturn(plans);

        // Act
        interactor.execute(input);

        // Assert
        verify(planDataAccessInterface).findPlansByUser(userId);

        ListPlansOutputData out = capturePresenterOutput();
        assertEquals(2, out.getPlans().size(), "Exactly two plans should be returned.");
        assertSame(plans, out.getPlans(), "Plans list must be the same instance as provided by the gateway.");
        assertTrue(out.getPlans().contains(plan1));
        assertTrue(out.getPlans().contains(plan2));
        assertNull(out.getErrorMessage(), "Error message must be null on success.");
    }

    /**
     * Behaviour-documentation variant of the success path:
     *
     * <p>This test focuses on validating that the interactor simply forwards
     * whatever {@code userId} is given in the input data to the gateway.
     * It uses a different userId than the previous tests to emphasise the
     * parameter-passing behaviour.
     */
    @Test
    @DisplayName("Interactor forwards exact userId to gateway (documentation test)")
    void forwardsExactUserIdToGateway() throws Exception {
        // Arrange
        int userId = 9999; // deliberately non-default value for clarity
        ListPlansInputData input = new ListPlansInputData(userId);

        Plan plan = createPlan(10, userId, "Evening River Walk");
        List<Plan> plans = List.of(plan);

        when(planDataAccessInterface.findPlansByUser(userId)).thenReturn(plans);

        // Act
        interactor.execute(input);

        // Assert – userId propagation
        verify(planDataAccessInterface, times(1)).findPlansByUser(userId);

        ListPlansOutputData out = capturePresenterOutput();
        assertSame(plans, out.getPlans());
        assertNull(out.getErrorMessage());
    }

    /**
     * Stress-style scenario with a larger number of plans:
     *
     * <p>While the interactor itself performs no iteration beyond what the
     * gateway returns, this test documents that the use case behaves
     * identically regardless of list size.
     */
    @Test
    @DisplayName("User with many plans -> all plans returned, no error")
    void userWithManyPlans_allPlansReturnedWithoutError() throws Exception {
        // Arrange
        int userId = 303;
        ListPlansInputData input = new ListPlansInputData(userId);

        List<Plan> manyPlans = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            manyPlans.add(createPlan(1000 + i, userId, "Plan #" + i));
        }

        when(planDataAccessInterface.findPlansByUser(userId)).thenReturn(manyPlans);

        // Act
        interactor.execute(input);

        // Assert
        verify(planDataAccessInterface).findPlansByUser(userId);

        ListPlansOutputData out = capturePresenterOutput();
        assertEquals(25, out.getPlans().size(), "All plans should be forwarded to the presenter.");
        assertSame(manyPlans, out.getPlans(), "List instance must be passed through unchanged.");
        assertNull(out.getErrorMessage());
    }

    // -------------------------------------------------------------------------
    // Edge-case success variant: gateway returns null
    // -------------------------------------------------------------------------

    /**
     * Edge-case: gateway returns {@code null} instead of an empty list.
     *
     * <p>The current interactor implementation passes this value directly to
     * the presenter without applying additional normalization. This test
     * documents that behaviour explicitly, so any future change (e.g.
     * converting {@code null} to {@code List.of()}) can be detected by a
     * test failure.
     */
    @Test
    @DisplayName("Gateway returns null list -> null passed through, no error")
    void gatewayReturnsNullList_nullPlansPassedThroughWithoutError() throws Exception {
        // Arrange
        int userId = 404;
        ListPlansInputData input = new ListPlansInputData(userId);

        when(planDataAccessInterface.findPlansByUser(userId)).thenReturn(null);

        // Act
        interactor.execute(input);

        // Assert
        verify(planDataAccessInterface).findPlansByUser(userId);

        ListPlansOutputData out = capturePresenterOutput();
        assertNull(out.getPlans(), "Plans reference will be null if gateway returns null.");
        assertNull(out.getErrorMessage(), "Error message is still null (no exception occurred).");
    }

    // -------------------------------------------------------------------------
    // Failure-path scenarios: exception handling
    // -------------------------------------------------------------------------

    /**
     * Failure scenario:
     *
     * <p>If the gateway throws an exception while retrieving plans, the
     * interactor must:
     * <ul>
     *     <li>Catch the exception (rather than propagating it).</li>
     *     <li>Provide an output object where {@code plans} is an empty list.</li>
     *     <li>Expose the exception's message in {@code errorMessage}.</li>
     * </ul>
     *
     * <p>This test verifies the typical case where the exception contains a
     * human-readable message.
     */
    @Test
    @DisplayName("Gateway throws exception -> empty list + error message")
    void gatewayThrowsException_returnsEmptyListAndErrorMessage() throws Exception {
        // Arrange
        int userId = 505;
        ListPlansInputData input = new ListPlansInputData(userId);

        doThrow(new Exception("Database connection failed"))
                .when(planDataAccessInterface).findPlansByUser(userId);

        // Act
        interactor.execute(input);

        // Assert
        verify(planDataAccessInterface).findPlansByUser(userId);

        ListPlansOutputData out = capturePresenterOutput();
        assertNotNull(out.getPlans(), "Plans list must not be null on exception (use empty list).");
        assertTrue(out.getPlans().isEmpty(), "Plans list should be empty when an error occurs.");
        assertEquals("Database connection failed", out.getErrorMessage());
    }

    /**
     * Edge-case failure scenario:
     *
     * <p>An exception may not provide a message (i.e. {@code getMessage()}
     * returns {@code null}). This test verifies that the interactor simply
     * forwards the null value without attempting to manipulate it, ensuring
     * that a lack of message does not cause a secondary failure.
     */
    @Test
    @DisplayName("Gateway throws exception with null message -> empty list + null errorMessage")
    void gatewayThrowsExceptionWithNullMessage_returnsEmptyListAndNullErrorMessage() throws Exception {
        // Arrange
        int userId = 606;
        ListPlansInputData input = new ListPlansInputData(userId);

        Exception ex = new Exception((String) null);
        doThrow(ex).when(planDataAccessInterface).findPlansByUser(userId);

        // Act
        interactor.execute(input);

        // Assert
        verify(planDataAccessInterface).findPlansByUser(userId);

        ListPlansOutputData out = capturePresenterOutput();
        assertTrue(out.getPlans().isEmpty(), "Plans list should be empty on exception.");
        assertNull(out.getErrorMessage(), "Null exception message should be forwarded as null.");
    }

    // -------------------------------------------------------------------------
    // Collaboration / interaction-style tests
    // -------------------------------------------------------------------------

    /**
     * Verifies that, on a successful retrieval, the presenter is invoked
     * exactly once and no additional calls are made to either collaborator.
     */
    @Test
    @DisplayName("Success: presenter called exactly once, no extra interactions")
    void successScenario_presenterCalledOnceNoExtraInteractions() throws Exception {
        // Arrange
        int userId = 707;
        ListPlansInputData input = new ListPlansInputData(userId);

        List<Plan> plans = List.of(createPlan(1, userId, "Single Plan"));
        when(planDataAccessInterface.findPlansByUser(userId)).thenReturn(plans);

        // Act
        interactor.execute(input);

        // Assert
        verify(planDataAccessInterface, times(1)).findPlansByUser(userId);
        verify(presenter, times(1)).present(any(ListPlansOutputData.class));

        // No further interactions beyond the expected collaboration
        verifyNoMoreInteractions(planDataAccessInterface, presenter);
    }

    /**
     * Verifies that, in the error case, the presenter is still invoked
     * exactly once and the gateway is not called more than needed.
     */
    @Test
    @DisplayName("Failure: presenter called exactly once even when exception is thrown")
    void failureScenario_presenterCalledOnceOnException() throws Exception {
        // Arrange
        int userId = 808;
        ListPlansInputData input = new ListPlansInputData(userId);

        doThrow(new Exception("Transient failure"))
                .when(planDataAccessInterface).findPlansByUser(userId);

        // Act
        interactor.execute(input);

        // Assert
        verify(planDataAccessInterface, times(1)).findPlansByUser(userId);
        verify(presenter, times(1)).present(any(ListPlansOutputData.class));
        verifyNoMoreInteractions(planDataAccessInterface, presenter);
    }

    // -------------------------------------------------------------------------
    // Defensive / immutability-style documentation test
    // -------------------------------------------------------------------------

    /**
     * This test documents that the interactor does not clone, wrap, or
     * defensively copy the list of plans returned by the gateway.
     *
     * <p>The current behaviour is to pass through the same list reference.
     * While this may or may not be desirable from an API-design perspective,
     * having a dedicated test makes the behaviour explicit and alerts future
     * maintainers if it changes.
     */
    @Test
    @DisplayName("Interactor passes through list reference returned by gateway")
    void interactorPassesThroughListReferenceFromGateway() throws Exception {
        // Arrange
        int userId = 909;
        ListPlansInputData input = new ListPlansInputData(userId);

        List<Plan> mutableList = new ArrayList<>();
        mutableList.add(createPlan(1, userId, "Original"));

        when(planDataAccessInterface.findPlansByUser(userId)).thenReturn(mutableList);

        // Act
        interactor.execute(input);

        // Assert
        ListPlansOutputData out = capturePresenterOutput();
        assertSame(mutableList, out.getPlans(),
                "The same list instance from the gateway should be passed to the presenter.");

        // Optional: mutate after the fact to illustrate shared reference
        mutableList.add(createPlan(2, userId, "Added Later"));
        assertEquals(2, out.getPlans().size(),
                "Because the list is shared by reference, changes are visible in the output as well.");
    }
}
