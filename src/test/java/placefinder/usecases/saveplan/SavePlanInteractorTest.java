package placefinder.usecases.saveplan;

import placefinder.entities.Leg;
import placefinder.entities.Plan;
import placefinder.entities.Place;
import placefinder.entities.PlanStop;
import placefinder.entities.Route;
import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SavePlanInteractor}.
 */
class SavePlanInteractorTest {

    // =====================================================================
    //  Success scenarios (various naming paths)
    // =====================================================================

    /**
     * Verifies that a valid plan with an explicit non-blank name is saved
     * and that the presenter receives a successful result.
     */
    @Test
    void execute_savesPlanWithExplicitName() throws Exception {
        final InMemoryPlanGateway gateway = new InMemoryPlanGateway();
        final CapturingSavePlanPresenter presenter = new CapturingSavePlanPresenter();
        final SavePlanInteractor interactor = new SavePlanInteractor(gateway, presenter);

        final Plan plan = buildValidPlan();
        plan.setName("Original name");

        final SavePlanInputData inputData =
                new SavePlanInputData(plan, "Custom trip name");

        interactor.execute(inputData);

        // Plan should have been persisted once with the same instance.
        assertEquals(1, gateway.getSaveCallCount());
        assertSame(plan, gateway.getSavedPlan());

        // Presenter should receive success output.
        final SavePlanOutputData output = presenter.getLastOutput();
        assertNotNull(output);
        assertTrue(output.isSuccess());
        assertEquals("Plan saved.", output.getMessage());
        assertSame(plan, output.getPlan());

        // Name on the plan should be the explicit custom name.
        assertEquals("Custom trip name", plan.getName());
    }

    /**
     * Verifies that a missing custom name (null) results in an automatically
     * generated name based on the plan date and origin.
     */
    @Test
    void execute_generatesNameWhenMissing() throws Exception {
        final InMemoryPlanGateway gateway = new InMemoryPlanGateway();
        final CapturingSavePlanPresenter presenter = new CapturingSavePlanPresenter();
        final SavePlanInteractor interactor = new SavePlanInteractor(gateway, presenter);

        final Plan plan = buildValidPlan();
        // Existing internal name will be overridden.
        plan.setName(null);

        final SavePlanInputData inputData =
                new SavePlanInputData(plan, null);

        final String expectedName = "Plan - " + plan.getDate()
                + " - " + plan.getOriginAddress();

        interactor.execute(inputData);

        // Plan should be saved.
        assertEquals(1, gateway.getSaveCallCount());
        assertSame(plan, gateway.getSavedPlan());

        // Name should be generated.
        assertEquals(expectedName, plan.getName());

        // Presenter should receive success.
        final SavePlanOutputData output = presenter.getLastOutput();
        assertNotNull(output);
        assertTrue(output.isSuccess());
        assertEquals("Plan saved.", output.getMessage());
        assertSame(plan, output.getPlan());
    }

    /**
     * Verifies that a custom name that is non-null but blank is treated as
     * missing and replaced by the generated name.
     */
    @Test
    void execute_generatesNameWhenExplicitNameBlank() throws Exception {
        final InMemoryPlanGateway gateway = new InMemoryPlanGateway();
        final CapturingSavePlanPresenter presenter = new CapturingSavePlanPresenter();
        final SavePlanInteractor interactor = new SavePlanInteractor(gateway, presenter);

        final Plan plan = buildValidPlan();
        plan.setName("Original name");

        final SavePlanInputData inputData =
                new SavePlanInputData(plan, "   "); // blank but non-null

        final String expectedName = "Plan - " + plan.getDate()
                + " - " + plan.getOriginAddress();

        interactor.execute(inputData);

        // Persisted once.
        assertEquals(1, gateway.getSaveCallCount());
        assertSame(plan, gateway.getSavedPlan());

        // Name should be generated, not kept as blank.
        assertEquals(expectedName, plan.getName());

        final SavePlanOutputData output = presenter.getLastOutput();
        assertNotNull(output);
        assertTrue(output.isSuccess());
        assertEquals("Plan saved.", output.getMessage());
        assertSame(plan, output.getPlan());
    }

    /**
     * Verifies the case where the plan's current name is blank and no custom
     * name is provided; the interactor should still generate a default name.
     */
    @Test
    void execute_generatesNameWhenExistingNameBlankAndNoCustomName() throws Exception {
        final InMemoryPlanGateway gateway = new InMemoryPlanGateway();
        final CapturingSavePlanPresenter presenter = new CapturingSavePlanPresenter();
        final SavePlanInteractor interactor = new SavePlanInteractor(gateway, presenter);

        final Plan plan = buildValidPlan();
        // Existing name is blank.
        plan.setName("   ");

        final SavePlanInputData inputData =
                new SavePlanInputData(plan, null);

        final String expectedName = "Plan - " + plan.getDate()
                + " - " + plan.getOriginAddress();

        interactor.execute(inputData);

        // Plan should be saved.
        assertEquals(1, gateway.getSaveCallCount());
        assertSame(plan, gateway.getSavedPlan());

        // Name should be generated.
        assertEquals(expectedName, plan.getName());

        final SavePlanOutputData output = presenter.getLastOutput();
        assertNotNull(output);
        assertTrue(output.isSuccess());
        assertEquals("Plan saved.", output.getMessage());
        assertSame(plan, output.getPlan());
    }

    // =====================================================================
    //  Validation failures
    // =====================================================================

    /**
     * When the plan is {@code null}, no persistence call is made
     * and the presenter is invoked with a failure result.
     */
    @Test
    void execute_failsWhenPlanIsNull() throws Exception {
        final InMemoryPlanGateway gateway = new InMemoryPlanGateway();
        final CapturingSavePlanPresenter presenter = new CapturingSavePlanPresenter();
        final SavePlanInteractor interactor = new SavePlanInteractor(gateway, presenter);

        final SavePlanInputData inputData =
                new SavePlanInputData(null, "Any name");

        interactor.execute(inputData);

        // No save should occur.
        assertEquals(0, gateway.getSaveCallCount());

        final SavePlanOutputData output = presenter.getLastOutput();
        assertNotNull(output);
        assertFalse(output.isSuccess());
        assertEquals("No plan to save. Please generate a plan first.", output.getMessage());
        assertNull(output.getPlan());
    }

    /**
     * When the plan has a {@code null} route, saving is rejected
     * and the presenter receives a failure result.
     */
    @Test
    void execute_failsWhenRouteIsNull() throws Exception {
        final InMemoryPlanGateway gateway = new InMemoryPlanGateway();
        final CapturingSavePlanPresenter presenter = new CapturingSavePlanPresenter();
        final SavePlanInteractor interactor = new SavePlanInteractor(gateway, presenter);

        final Plan plan = buildValidPlan();
        plan.setRoute(null);

        final SavePlanInputData inputData =
                new SavePlanInputData(plan, "Name");

        interactor.execute(inputData);

        // No save should occur.
        assertEquals(0, gateway.getSaveCallCount());

        final SavePlanOutputData output = presenter.getLastOutput();
        assertNotNull(output);
        assertFalse(output.isSuccess());
        assertEquals("No plan to save. Please generate a plan first.", output.getMessage());
        assertNull(output.getPlan());
    }

    /**
     * When the route contains an empty list of stops, saving is rejected and
     * the presenter receives a failure result.
     */
    @Test
    void execute_failsWhenRouteHasNoStops() throws Exception {
        final InMemoryPlanGateway gateway = new InMemoryPlanGateway();
        final CapturingSavePlanPresenter presenter = new CapturingSavePlanPresenter();
        final SavePlanInteractor interactor = new SavePlanInteractor(gateway, presenter);

        final Plan plan = buildValidPlan();
        // Route with no stops.
        final Route emptyRoute =
                new Route(Collections.emptyList(), Collections.emptyList(), 0, 0.0, null);
        plan.setRoute(emptyRoute);

        final SavePlanInputData inputData =
                new SavePlanInputData(plan, "Name");

        interactor.execute(inputData);

        // No save should occur.
        assertEquals(0, gateway.getSaveCallCount());

        final SavePlanOutputData output = presenter.getLastOutput();
        assertNotNull(output);
        assertFalse(output.isSuccess());
        assertEquals("No plan to save. Please generate a plan first.", output.getMessage());
        assertNull(output.getPlan());
    }

    /**
     * When the route has a {@code null} stops list, the "stops == null"
     * branch in the validation should be taken, and saving should be rejected.
     * This test is specifically here to exercise that branch for coverage.
     */
    @Test
    void execute_failsWhenRouteHasNullStopsList() throws Exception {
        final InMemoryPlanGateway gateway = new InMemoryPlanGateway();
        final CapturingSavePlanPresenter presenter = new CapturingSavePlanPresenter();
        final SavePlanInteractor interactor = new SavePlanInteractor(gateway, presenter);

        final Plan plan = buildValidPlan();

        // Create a Route whose stops list is null; other fields can be anything.
        final Route nullStopsRoute =
                new Route(null, Collections.emptyList(), 0, 0.0, null);
        plan.setRoute(nullStopsRoute);

        final SavePlanInputData inputData =
                new SavePlanInputData(plan, "Name");

        interactor.execute(inputData);

        // No save should occur.
        assertEquals(0, gateway.getSaveCallCount());

        final SavePlanOutputData output = presenter.getLastOutput();
        assertNotNull(output);
        assertFalse(output.isSuccess());
        assertEquals("No plan to save. Please generate a plan first.", output.getMessage());
        assertNull(output.getPlan());
    }

    // =====================================================================
    //  Exception handling
    // =====================================================================

    /**
     * Verifies that a persistence exception is caught and converted
     * into a failure result for the presenter.
     */
    @Test
    void execute_reportsFailureWhenGatewayThrows() throws Exception {
        final FailingPlanGateway gateway = new FailingPlanGateway("Simulated failure");
        final CapturingSavePlanPresenter presenter = new CapturingSavePlanPresenter();
        final SavePlanInteractor interactor = new SavePlanInteractor(gateway, presenter);

        final Plan plan = buildValidPlan();
        final SavePlanInputData inputData =
                new SavePlanInputData(plan, "Name");

        interactor.execute(inputData);

        final SavePlanOutputData output = presenter.getLastOutput();
        assertNotNull(output);
        assertFalse(output.isSuccess());
        assertEquals("Simulated failure", output.getMessage());
        assertNull(output.getPlan());
    }

    // =====================================================================
    //  Helpers and test doubles
    // =====================================================================

    /**
     * Builds a minimal valid {@link Plan} instance with a date,
     * origin address and a route containing a single stop.
     */
    private Plan buildValidPlan() {
        final LocalDate date = LocalDate.of(2025, 1, 1);
        final LocalTime startTime = LocalTime.of(9, 0);
        final String origin = "Toronto";

        final Place place = new Place();
        place.setId("Place-id");
        place.setName("Museum");
        place.setAddress(origin);
        place.setLat(43.0);
        place.setLon(-79.0);
        place.setDistanceKm(0.0);

        final PlanStop stop = new PlanStop(1, place, startTime, startTime.plusHours(1));
        final List<PlanStop> stops = new ArrayList<>();
        stops.add(stop);

        final List<Leg> legs = new ArrayList<>();
        final Route route = new Route(stops, legs, 1000, 3600.0, "encoded");

        final Map<String, List<String>> snapshotCategories = new HashMap<>();
        snapshotCategories.put("museum", Collections.singletonList("art"));

        return new Plan(
                null,          // id
                1,             // userId
                "Initial name",
                date,
                startTime,
                origin,
                route,
                5.0,
                snapshotCategories
        );
    }

    /**
     * Simple in-memory gateway capturing the last saved plan.
     */
    private static class InMemoryPlanGateway implements PlanDataAccessInterface {

        private Plan savedPlan;
        private int saveCallCount;

        @Override
        public void savePlan(Plan plan) {
            this.savedPlan = plan;
            this.saveCallCount++;
        }

        @Override
        public List<Plan> findPlansByUser(int userId) {
            return Collections.emptyList();
        }

        @Override
        public Plan findPlanWithStops(int planId) {
            return null;
        }

        @Override
        public void deletePlan(int planId, int userId) {
            // Intentionally left empty.
        }

        Plan getSavedPlan() {
            return savedPlan;
        }

        int getSaveCallCount() {
            return saveCallCount;
        }
    }

    /**
     * Gateway whose {@link #savePlan(Plan)} method always throws
     * a runtime exception with a fixed message.
     */
    private static class FailingPlanGateway implements PlanDataAccessInterface {

        private final String message;

        FailingPlanGateway(String message) {
            this.message = message;
        }

        @Override
        public void savePlan(Plan plan) {
            throw new RuntimeException(message);
        }

        @Override
        public List<Plan> findPlansByUser(int userId) {
            return Collections.emptyList();
        }

        @Override
        public Plan findPlanWithStops(int planId) {
            return null;
        }

        @Override
        public void deletePlan(int planId, int userId) {
            // Intentionally left empty.
        }
    }

    /**
     * Presenter that captures the last {@link SavePlanOutputData}
     * instance passed to it.
     */
    private static class CapturingSavePlanPresenter implements SavePlanOutputBoundary {

        private SavePlanOutputData lastOutput;

        @Override
        public void present(SavePlanOutputData outputData) {
            this.lastOutput = outputData;
        }

        SavePlanOutputData getLastOutput() {
            return lastOutput;
        }
    }
}
