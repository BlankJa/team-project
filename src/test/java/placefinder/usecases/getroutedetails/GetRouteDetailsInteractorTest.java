package placefinder.usecases.getroutedetails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.Leg;
import placefinder.entities.Plan;
import placefinder.entities.PlanStop;
import placefinder.entities.Route;
import placefinder.entities.Step;
import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GetRouteDetailsInteractor}.
 *
 * <p>These tests follow the same structure and style as the existing
 * {@code GetPlanDetailsInteractorTest}.  They treat the interactor as a
 * pure application-layer component by mocking out its collaborators:
 *
 * <ul>
 *   <li>The persistence gateway {@link PlanDataAccessInterface} is mocked using
 *       Mockito.</li>
 *   <li>The output boundary {@link GetRouteDetailsOutputBoundary} is also
 *       mocked so that we can capture the output passed to it without
 *       invoking any real UI code.</li>
 *   <li>Domain entities such as {@link Plan}, {@link Route}, {@link Leg},
 *       {@link PlanStop} and {@link Step} are instantiated as real objects.</li>
 * </ul>
 */
class GetRouteDetailsInteractorTest {

    // -------------------------------------------------------------------------
    // Collaborators & System Under Test
    // -------------------------------------------------------------------------

    /** Gateway used to load plans from persistence (mock). */
    private PlanDataAccessInterface planDAO;

    /** Presenter used to deliver route details back to the UI (mock). */
    private GetRouteDetailsOutputBoundary presenter;

    /** System under test. */
    private GetRouteDetailsInteractor interactor;

    // -------------------------------------------------------------------------
    // Test lifecycle
    // -------------------------------------------------------------------------

    /**
     * Creates fresh mocks and a new interactor instance before each test.
     */
    @BeforeEach
    void setUp() {
        planDAO = mock(PlanDataAccessInterface.class);
        presenter = mock(GetRouteDetailsOutputBoundary.class);
        interactor = new GetRouteDetailsInteractor(planDAO, presenter);
    }

    // -------------------------------------------------------------------------
    // Utility methods
    // -------------------------------------------------------------------------

    /**
     * Captures the single {@link GetRouteDetailsOutputData} instance passed
     * to the presenter during execution.  Using an argument captor ensures
     * that tests remain resilient to implementation details of Mockito.
     */
    private GetRouteDetailsOutputData capturePresenterOutput() {
        ArgumentCaptor<GetRouteDetailsOutputData> captor =
                ArgumentCaptor.forClass(GetRouteDetailsOutputData.class);
        verify(presenter).present(captor.capture());
        return captor.getValue();
    }

    /**
     * Constructs a plan with a simple route consisting of a single leg and
     * step.  This helper avoids repeating boilerplate in multiple tests.
     *
     * @param planId unique identifier to assign to the plan and its stops
     * @return a {@link Plan} with a non-empty route
     */
    private Plan createPlanWithOneLeg(int planId) {
        // Create two stops for the leg
        PlanStop startStop = new PlanStop(0, null,
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        PlanStop endStop = new PlanStop(1, null,
                LocalTime.of(11, 0), LocalTime.of(12, 0));

        // Single navigation step
        Step step = new Step(100, 60.0, "Head north");
        List<Step> steps = new ArrayList<>();
        steps.add(step);

        // Single leg connecting startStop to endStop
        Leg leg = new Leg(100, 60.0, "", startStop, endStop, steps);
        List<Leg> legs = new ArrayList<>();
        legs.add(leg);

        // Route containing the two stops and one leg
        List<PlanStop> stops = new ArrayList<>();
        stops.add(startStop);
        stops.add(endStop);

        Route route = new Route(stops, legs, 200, 120.0, "");
        return new Plan(planId, 99, "Sample Trip",
                LocalDate.of(2025, 1, 2), LocalTime.of(8, 30),
                "456 Example Ave", route, 5.0, Collections.emptyMap());
    }

    /**
     * Constructs a plan with a route that has no legs.  The route may be
     * {@code null}, have a null legs list, or an empty legs list depending on
     * the parameters.  This supports testing the various no-route scenarios.
     *
     * @param planId plan identifier
     * @param routeVariant determines how the route is absent:
     *                     0 – route is {@code null};
     *                     1 – route exists but legs list is {@code null};
     *                     2 – route exists with an empty legs list.
     * @return a {@link Plan} configured accordingly
     */
    private Plan createPlanWithoutRoute(int planId, int routeVariant) {
        Route route;
        switch (routeVariant) {
            case 1:
                // route exists but legs list is null
                route = new Route(new ArrayList<>(), null, 0, 0.0, null);
                break;
            case 2:
                // route exists but legs list is empty
                route = new Route(new ArrayList<>(), new ArrayList<>(), 0, 0.0, null);
                break;
            default:
                // route is completely null
                route = null;
        }
        return new Plan(planId, 99, "No Route Plan",
                LocalDate.of(2025, 2, 3), LocalTime.of(7, 45),
                "789 Nowhere Rd", route, 3.0, Collections.emptyMap());
    }

    // -------------------------------------------------------------------------
    // Test cases – success paths
    // -------------------------------------------------------------------------

    /**
     * Happy-path scenario:
     *
     * <p>When the gateway returns a plan with a non-empty route, the interactor
     * should:
     * <ul>
     *     <li>Call {@link PlanDataAccessInterface#findPlanWithStops(int)} with
     *         the provided plan ID.</li>
     *     <li>Forward a {@link GetRouteDetailsOutputData} to the presenter
     *         containing the same legs as the plan’s route.</li>
     *     <li>Not set an error message (i.e., {@code getErrorMessage() == null}).</li>
     * </ul>
     */
    @Test
    void planExists_presentsLegsWithoutError() throws Exception {
        // Arrange
        int planId = 123;
        Plan plan = createPlanWithOneLeg(planId);
        when(planDAO.findPlanWithStops(planId)).thenReturn(plan);

        // Act
        interactor.execute(new GetRouteDetailsInputData(planId));

        // Assert – verify gateway invocation and captured output
        verify(planDAO, times(1)).findPlanWithStops(planId);
        GetRouteDetailsOutputData out = capturePresenterOutput();
        // Should return the same list instance
        assertSame(plan.getRoute().getLegs(), out.getLegs(),
                "Interactor must pass through the same legs list instance.");
        assertNull(out.getErrorMessage(),
                "Error message should be null on successful retrieval.");
    }

    /**
     * Behaviour-documentation variant of the happy path:
     *
     * <p>This test underscores that the interactor does not manipulate the
     * plan identifier and simply forwards it to the gateway.  Using a
     * different value from the previous test makes this intent explicit.</p>
     */
    @Test
    void forwardsExactPlanIdToGateway() throws Exception {
        // Arrange – choose a distinct identifier
        int planId = 456;
        Plan plan = createPlanWithOneLeg(planId);
        when(planDAO.findPlanWithStops(planId)).thenReturn(plan);

        // Act
        interactor.execute(new GetRouteDetailsInputData(planId));

        // Assert – gateway receives the exact same id
        verify(planDAO).findPlanWithStops(planId);
        GetRouteDetailsOutputData out = capturePresenterOutput();
        assertSame(plan.getRoute().getLegs(), out.getLegs());
        assertNull(out.getErrorMessage());
    }

    // -------------------------------------------------------------------------
    // Test cases – plan not found
    // -------------------------------------------------------------------------

    /**
     * Missing-plan scenario:
     *
     * <p>If the gateway returns {@code null}, the interactor should:
     * <ul>
     *     <li>Invoke the gateway with the requested plan ID.</li>
     *     <li>Present an output where {@code getLegs() == null} and
     *         {@code getErrorMessage()} is "Plan not found.".</li>
     * </ul>
     */
    @Test
    void planDoesNotExist_presentsNullLegsWithNotFoundMessage() throws Exception {
        // Arrange
        int planId = 999;
        when(planDAO.findPlanWithStops(planId)).thenReturn(null);

        // Act
        interactor.execute(new GetRouteDetailsInputData(planId));

        // Assert – gateway still called and presenter informed
        verify(planDAO).findPlanWithStops(planId);
        GetRouteDetailsOutputData out = capturePresenterOutput();
        assertNull(out.getLegs(), "Legs should be null when plan is absent.");
        assertEquals("Plan not found.", out.getErrorMessage());
    }

    // -------------------------------------------------------------------------
    // Test cases – no route or legs
    // -------------------------------------------------------------------------

    /**
     * Scenario where the plan exists but no route information is available.
     *
     * <p>The interactor must detect several variants of this situation:
     * <ul>
     *   <li>The {@link Route} object itself is {@code null}.</li>
     *   <li>The route exists but its legs list is {@code null}.</li>
     *   <li>The route exists but its legs list is empty.</li>
     * </ul>
     * In all of these cases, the interactor should present null legs and the
     * message "No route details available for this plan.".
     */
    @Test
    void noRouteOrLegs_presentsNullLegsWithNoRouteMessage() throws Exception {
        int planIdBase = 1000;
        // Test all variants: 0 (route null), 1 (legs null), 2 (empty legs)
        for (int variant = 0; variant < 3; variant++) {
            int planId = planIdBase + variant;
            Plan plan = createPlanWithoutRoute(planId, variant);
            when(planDAO.findPlanWithStops(planId)).thenReturn(plan);

            interactor.execute(new GetRouteDetailsInputData(planId));

            verify(planDAO).findPlanWithStops(planId);
            GetRouteDetailsOutputData out = capturePresenterOutput();
            assertNull(out.getLegs(), "Legs must be null when route is absent or empty.");
            assertEquals("No route details available for this plan.", out.getErrorMessage());

            // Reset mocks between iterations to avoid interference
            reset(planDAO, presenter);
        }
    }

    // -------------------------------------------------------------------------
    // Test cases – exception handling
    // -------------------------------------------------------------------------

    /**
     * Error-path scenario:
     *
     * <p>If {@link PlanDataAccessInterface#findPlanWithStops(int)} throws an exception,
     * the interactor should catch it, present null legs, and propagate the
     * exception’s message via {@code getErrorMessage()}.</p>
     */
    @Test
    void gatewayThrowsException_presentsErrorMessageAndNullLegs() throws Exception {
        int planId = 2020;
        doThrow(new Exception("Service unavailable")).when(planDAO).findPlanWithStops(planId);

        interactor.execute(new GetRouteDetailsInputData(planId));

        verify(planDAO).findPlanWithStops(planId);
        GetRouteDetailsOutputData out = capturePresenterOutput();
        assertNull(out.getLegs(), "Legs should be null on exception.");
        assertEquals("Service unavailable", out.getErrorMessage());
    }

    /**
     * Edge-case variation of exception handling:
     *
     * <p>When the exception thrown has a {@code null} message, the interactor
     * must forward the null unchanged.  This test ensures no concatenation or
     * manipulation is attempted on a null error message.</p>
     */
    @Test
    void gatewayThrowsExceptionWithNullMessage_presentsNullErrorMessage() throws Exception {
        int planId = 3030;
        Exception ex = new Exception((String) null);
        doThrow(ex).when(planDAO).findPlanWithStops(planId);

        interactor.execute(new GetRouteDetailsInputData(planId));

        GetRouteDetailsOutputData out = capturePresenterOutput();
        assertNull(out.getLegs());
        assertNull(out.getErrorMessage(),
                "Error message should be null when the exception message is null.");
    }
}