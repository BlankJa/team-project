package placefinder.usecases.buildplan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.GeocodeResult;
import placefinder.entities.Place;
import placefinder.entities.Plan;
import placefinder.entities.PreferenceProfile;
import placefinder.entities.Route;
import placefinder.entities.PlanStop;
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
 */
class BuildPlanInteractorTest {

    private PreferenceDataAccessInterface preferenceGateway;
    private GeocodingDataAccessInterface geocodingGateway;
    private RouteDataAccessInterface routeGateway;
    private CapturingPresenter presenter;
    private BuildPlanInteractor interactor;

    /**
     * Simple presenter implementation that captures the last
     * {@link BuildPlanOutputData} passed to it.
     */
    private static class CapturingPresenter implements BuildPlanOutputBoundary {
        private BuildPlanOutputData output;

        @Override
        public void present(BuildPlanOutputData outputData) {
            this.output = outputData;
        }

        BuildPlanOutputData getOutput() {
            return output;
        }
    }

    @BeforeEach
    void setUp() {
        preferenceGateway = mock(PreferenceDataAccessInterface.class);
        geocodingGateway = mock(GeocodingDataAccessInterface.class);
        routeGateway = mock(RouteDataAccessInterface.class);
        presenter = new CapturingPresenter();

        interactor = new BuildPlanInteractor(
                preferenceGateway,
                geocodingGateway,
                routeGateway,
                presenter
        );
    }

    /**
     * If no places are selected, the interactor should not call any gateways
     * and should return an error message.
     */
    @Test
    void noSelectedPlaces_returnsError() {
        BuildPlanInputData input = new BuildPlanInputData(
                1,
                "Toronto",
                "2025-11-19",
                "09:00",
                List.of(),   // no places
                10           // existing plan id (arbitrary)
        );

        interactor.execute(input);

        BuildPlanOutputData out = presenter.getOutput();
        assertNotNull(out, "Output should be provided");
        assertNull(out.getPlan(), "Plan should be null when no places are selected");
        assertEquals("Please select at least one place.", out.getErrorMessage());

        verifyNoInteractions(geocodingGateway, preferenceGateway, routeGateway);
    }

    /**
     * If geocoding fails (returns null), the interactor should return an
     * appropriate error and not attempt to build a route.
     */
    @Test
    void geocoderFails_returnsError() throws Exception {
        when(geocodingGateway.geocode("Nowhere")).thenReturn(null);

        BuildPlanInputData input = new BuildPlanInputData(
                1,
                "Nowhere",
                "2025-11-19",
                "09:00",
                List.of(mock(Place.class)),
                10
        );

        interactor.execute(input);

        BuildPlanOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getPlan());
        assertEquals("Could not find that location.", out.getErrorMessage());

        verify(geocodingGateway).geocode("Nowhere");
        verifyNoInteractions(preferenceGateway, routeGateway);
    }

    /**
     * If the routing gateway cannot compute a route and returns null,
     * the interactor should surface a route-specific error.
     */
    @Test
    void routeNull_returnsRouteError() throws Exception {
        // Geocoding succeeds.
        GeocodeResult geo = mock(GeocodeResult.class);
        when(geocodingGateway.geocode("Toronto")).thenReturn(geo);

        // Preferences are loaded, but the exact values are not important here.
        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(profile.getRadiusKm()).thenReturn(5.0);
        when(profile.getSelectedCategories()).thenReturn(Map.of());
        when(preferenceGateway.loadForUser(1)).thenReturn(profile);

        // Route computation fails.
        when(routeGateway.computeRoute(eq(geo), any(LocalTime.class), anyList()))
                .thenReturn(null);

        BuildPlanInputData input = new BuildPlanInputData(
                1,
                "Toronto",
                "2025-11-19",
                "09:00",
                List.of(mock(Place.class)),
                null
        );

        interactor.execute(input);

        BuildPlanOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getPlan());
        assertEquals("Could not find route between locations.", out.getErrorMessage());
    }

    /**
     * On success, the interactor should construct a {@link Plan} whose fields
     * are consistent with the input data and the values returned by the
     * gateways.
     */
    @Test
    void success_buildsPlanWithExpectedFields() throws Exception {
        // Geocoding result
        GeocodeResult geo = mock(GeocodeResult.class);
        when(geo.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geocodingGateway.geocode("Toronto")).thenReturn(geo);

        // Preferences
        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(profile.getRadiusKm()).thenReturn(7.5);
        Map<String, List<String>> categories = Map.of("food", List.of("cafe"));
        when(profile.getSelectedCategories()).thenReturn(categories);
        when(preferenceGateway.loadForUser(1)).thenReturn(profile);

        // Route
        Route route = mock(Route.class);
        when(route.getStops()).thenReturn(List.of(mock(PlanStop.class)));
        when(routeGateway.computeRoute(geo, LocalTime.of(9, 0), anyList()))
                .thenReturn(route);

        Place place1 = mock(Place.class);
        Place place2 = mock(Place.class);

        BuildPlanInputData input = new BuildPlanInputData(
                1,
                "Toronto",
                "2025-11-19",
                "09:00",
                List.of(place1, place2),
                42   // existing plan id
        );

        interactor.execute(input);

        BuildPlanOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getErrorMessage(), "Error message should be null on success");

        Plan plan = out.getPlan();
        assertNotNull(plan, "Plan should be created");

        assertEquals(42, plan.getId());
        assertEquals(1, plan.getUserId());
        assertEquals("", plan.getName(), "Name is set later by SavePlanInteractor");
        assertEquals(LocalDate.parse("2025-11-19"), plan.getDate());
        assertEquals(LocalTime.parse("09:00"), plan.getStartTime());
        assertEquals("Toronto, ON", plan.getOriginAddress());
        assertSame(route, plan.getRoute(), "Plan should hold the route returned by the gateway");
        assertEquals(7.5, plan.getSnapshotRadiusKm());
        assertEquals(categories, plan.getSnapshotCategories());
    }

    /**
     * If any gateway throws an exception, the interactor should catch it and
     * pass the exception message back via the presenter.
     */
    @Test
    void exceptionDuringExecution_returnsExceptionMessage() throws Exception {
        when(geocodingGateway.geocode("Toronto"))
                .thenThrow(new RuntimeException("Unexpected failure"));

        BuildPlanInputData input = new BuildPlanInputData(
                1,
                "Toronto",
                "2025-11-19",
                "09:00",
                List.of(mock(Place.class)),
                null
        );

        interactor.execute(input);

        BuildPlanOutputData out = presenter.getOutput();
        assertNotNull(out);
        assertNull(out.getPlan());
        assertEquals("Unexpected failure", out.getErrorMessage());
    }

    /**
     * Verifies that the interactor passes the correct parameters to the
     * {@link RouteDataAccessInterface} when building the route.
     */
    @Test
    void success_passesCorrectArgumentsToRouteGateway() throws Exception {
        GeocodeResult geo = mock(GeocodeResult.class);
        when(geocodingGateway.geocode("Toronto")).thenReturn(geo);

        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(profile.getRadiusKm()).thenReturn(5.0);
        when(profile.getSelectedCategories()).thenReturn(Map.of());
        when(preferenceGateway.loadForUser(1)).thenReturn(profile);

        Route route = mock(Route.class);
        when(routeGateway.computeRoute(any(), any(), anyList())).thenReturn(route);

        Place p1 = mock(Place.class);
        Place p2 = mock(Place.class);

        BuildPlanInputData input = new BuildPlanInputData(
                1,
                "Toronto",
                "2025-11-19",
                "09:00",
                List.of(p1, p2),
                null
        );

        interactor.execute(input);

        ArgumentCaptor<GeocodeResult> originCaptor = ArgumentCaptor.forClass(GeocodeResult.class);
        ArgumentCaptor<LocalTime> timeCaptor = ArgumentCaptor.forClass(LocalTime.class);
        ArgumentCaptor<List<Place>> placesCaptor = ArgumentCaptor.forClass(List.class);

        verify(routeGateway).computeRoute(
                originCaptor.capture(),
                timeCaptor.capture(),
                placesCaptor.capture()
        );

        assertSame(geo, originCaptor.getValue(), "Origin should match geocoding result");
        assertEquals(LocalTime.of(9, 0), timeCaptor.getValue());
        assertEquals(List.of(p1, p2), placesCaptor.getValue());
    }
}
