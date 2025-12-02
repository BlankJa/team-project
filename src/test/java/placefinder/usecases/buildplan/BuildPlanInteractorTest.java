package placefinder.usecases.buildplan;

import org.junit.jupiter.api.Test;
import placefinder.entities.*;
import placefinder.usecases.dataacessinterfaces.GeocodingDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.RouteDataAccessInterface;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BuildPlanInteractor}.
 *
 * These tests avoid mocking entity classes because Mockito cannot
 * instrument POJOs on Java 24 with inline mocks.
 * Only gateways and presenters are mocked.
 */
class BuildPlanInteractorTest {

    /** Simple presenter capturing last output. */
    private static class CapturingPresenter implements BuildPlanOutputBoundary {
        private BuildPlanOutputData output;
        @Override
        public void present(BuildPlanOutputData outputData) {
            this.output = outputData;
        }
        public BuildPlanOutputData getOutput() {
            return output;
        }
    }

    @Test
    void noSelectedPlaces_returnsError() throws Exception {
        PreferenceDataAccessInterface pref = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geo = mock(GeocodingDataAccessInterface.class);
        RouteDataAccessInterface route = mock(RouteDataAccessInterface.class);

        CapturingPresenter presenter = new CapturingPresenter();

        BuildPlanInteractor interactor =
                new BuildPlanInteractor(pref, geo, route, presenter);

        BuildPlanInputData input = new BuildPlanInputData(
                1, "Toronto", "2025-11-19", "09:00",
                List.of(), null
        );

        interactor.execute(input);
        BuildPlanOutputData out = presenter.getOutput();

        assertNull(out.getPlan());
        assertEquals("Please select at least one place.", out.getErrorMessage());
    }

    @Test
    void geocoderFails_returnsError() throws Exception {
        PreferenceDataAccessInterface pref = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geo = mock(GeocodingDataAccessInterface.class);
        RouteDataAccessInterface route = mock(RouteDataAccessInterface.class);

        when(geo.geocode("Nowhere")).thenReturn(null);

        CapturingPresenter presenter = new CapturingPresenter();

        BuildPlanInteractor interactor =
                new BuildPlanInteractor(pref, geo, route, presenter);

        BuildPlanInputData input = new BuildPlanInputData(
                1, "Nowhere", "2025-11-19", "09:00",
                List.of(new Place("1","A","B",0,0,0,null,List.of())),
                null
        );

        interactor.execute(input);
        BuildPlanOutputData out = presenter.getOutput();

        assertNull(out.getPlan());
        assertEquals("Could not find that location.", out.getErrorMessage());
    }

    @Test
    void routeNull_returnsError() throws Exception {
        PreferenceDataAccessInterface pref = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geo = mock(GeocodingDataAccessInterface.class);
        RouteDataAccessInterface routeGateway = mock(RouteDataAccessInterface.class);

        // Real objects (no mocking!)
        GeocodeResult geocode = new GeocodeResult(10.0, 20.0, "Test Location");
        when(geo.geocode("Toronto")).thenReturn(geocode);

        PreferenceProfile profile =
                new PreferenceProfile(1, 7.5, Map.of());
        when(pref.loadForUser(1)).thenReturn(profile);

        when(routeGateway.computeRoute(any(), any(), any())).thenReturn(null);

        CapturingPresenter presenter = new CapturingPresenter();

        BuildPlanInteractor interactor =
                new BuildPlanInteractor(pref, geo, routeGateway, presenter);

        BuildPlanInputData input = new BuildPlanInputData(
                1, "Toronto", "2025-11-19", "09:00",
                List.of(new Place("1","A","B",0,0,0,null,List.of())), null
        );

        interactor.execute(input);
        BuildPlanOutputData out = presenter.getOutput();

        assertNull(out.getPlan());
        assertEquals("Could not find route between locations.", out.getErrorMessage());
    }

    @Test
    void success_buildsPlanCorrectly() throws Exception {
        PreferenceDataAccessInterface pref = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geo = mock(GeocodingDataAccessInterface.class);
        RouteDataAccessInterface routeGateway = mock(RouteDataAccessInterface.class);

        // Real geocode result
        GeocodeResult geocode = new GeocodeResult(43.7, -79.4, "Toronto, ON");
        when(geo.geocode("Toronto")).thenReturn(geocode);

        // Real profile
        PreferenceProfile profile =
                new PreferenceProfile(1, 5.0, Map.of("food", List.of("cafe")));
        when(pref.loadForUser(1)).thenReturn(profile);

        // Real places
        Place p1 = new Place("1","CN Tower","A",1,1,0,null,List.of());
        Place p2 = new Place("2","ROM","B",1,1,0,null,List.of());

        // Real PlanStops for Route
        PlanStop s1 = new PlanStop(1, p1, LocalTime.of(9,0), LocalTime.of(10,0));
        PlanStop s2 = new PlanStop(2, p2, LocalTime.of(10,0), LocalTime.of(11,0));

        Route route = new Route(List.of(s1, s2), List.of(), 1000, 2.0, "encoded");
        when(routeGateway.computeRoute(any(), any(), any())).thenReturn(route);

        CapturingPresenter presenter = new CapturingPresenter();

        BuildPlanInteractor interactor =
                new BuildPlanInteractor(pref, geo, routeGateway, presenter);

        BuildPlanInputData input = new BuildPlanInputData(
                1, "Toronto", "2025-11-19", "09:00", List.of(p1, p2), null
        );

        interactor.execute(input);
        BuildPlanOutputData out = presenter.getOutput();

        assertNotNull(out.getPlan());
        assertNull(out.getErrorMessage());

        Plan plan = out.getPlan();

        assertEquals(1, plan.getUserId());
        assertEquals(LocalDate.of(2025,11,19), plan.getDate());
        assertEquals(LocalTime.of(9,0), plan.getStartTime());
        assertEquals("Toronto, ON", plan.getOriginAddress());
        assertEquals(5.0, plan.getSnapshotRadiusKm());
        assertEquals(Map.of("food", List.of("cafe")), plan.getSnapshotCategories());

        assertEquals(2, plan.getRoute().getStops().size());
    }

    @Test
    void exceptionDuringExecution_returnsExceptionMessage() throws Exception {
        PreferenceDataAccessInterface pref = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geo = mock(GeocodingDataAccessInterface.class);
        RouteDataAccessInterface route = mock(RouteDataAccessInterface.class);

        when(geo.geocode(anyString())).thenThrow(new RuntimeException("boom"));

        CapturingPresenter presenter = new CapturingPresenter();

        BuildPlanInteractor interactor =
                new BuildPlanInteractor(pref, geo, route, presenter);

        BuildPlanInputData input = new BuildPlanInputData(
                1, "X", "2025-11-19", "09:00",
                List.of(new Place("1","A","B",0,0,0,null,List.of())), null
        );

        interactor.execute(input);
        BuildPlanOutputData out = presenter.getOutput();

        assertNull(out.getPlan());
        assertEquals("boom", out.getErrorMessage());
    }
}
