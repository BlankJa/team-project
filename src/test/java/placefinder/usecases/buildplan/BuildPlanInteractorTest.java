package placefinder.usecases.buildplan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.*;
import placefinder.usecases.dataacessinterfaces.GeocodingDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;
import placefinder.usecases.searchplaces.SearchPlacesInteractor;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BuildPlanInteractor}.
 *
 * Uses Mockito to mock gateways and entities, and a simple presenter that
 * captures the last BuildPlanOutputData.
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
        PreferenceDataAccessInterface prefGateway = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);

        CapturingPresenter presenter = new CapturingPresenter();

        BuildPlanInteractor interactor =
                new BuildPlanInteractor(prefGateway, geocodingDataAccessInterface, presenter);

        BuildPlanInputData input = new BuildPlanInputData(
                1,
                "Toronto",
                "2025-11-19",
                "09:00",
                List.of(),
                10);

        interactor.execute(input);
        BuildPlanOutputData out = presenter.getOutput();

        assertNull(out.getPlan());
        assertFalse(out.isTruncated());
        assertEquals("Please select at least one place.", out.getErrorMessage());
    }

    @Test
    void geocoderFails_returnsError() throws Exception {
        PreferenceDataAccessInterface pref = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geo = mock(GeocodingDataAccessInterface.class);

        when(geo.geocode("Nowhere")).thenReturn(null);

        CapturingPresenter presenter = new CapturingPresenter();

        BuildPlanInteractor interactor =
                new BuildPlanInteractor(pref, geo, presenter);

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

        assertNull(out.getPlan());
        assertEquals("Could not find that location.", out.getErrorMessage());
    }

    @Test
    void success_createsPlanStopsSequentially() throws Exception {
        PreferenceDataAccessInterface pref = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geo = mock(GeocodingDataAccessInterface.class);

        // pref profile
        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(profile.getRadiusKm()).thenReturn(5.0);
        when(profile.getSelectedCategories()).thenReturn(Map.of());
        when(pref.loadForUser(1)).thenReturn(profile);

        // geocode OK
        GeocodeResult gr = mock(GeocodeResult.class);
        when(gr.getFormattedAddress()).thenReturn("Toronto, ON");
        when(geo.geocode("Toronto")).thenReturn(gr);

        // two places
        Place p1 = mock(Place.class);
        Place p2 = mock(Place.class);

        CapturingPresenter presenter = new CapturingPresenter();

        BuildPlanInteractor interactor =
                new BuildPlanInteractor(pref, geo, presenter);

        BuildPlanInputData input = new BuildPlanInputData(
                1,
                "Toronto",
                "2025-11-19",
                "09:00",
                List.of(p1, p2),
                null
        );

        interactor.execute(input);
        BuildPlanOutputData out = presenter.getOutput();

        assertNotNull(out.getPlan());
        assertFalse(out.isTruncated());
        assertNull(out.getErrorMessage());

        List<PlanStop> stops = out.getPlan().getRoute().getStops();
        assertEquals(2, stops.size());
        assertEquals(LocalTime.of(9,0), stops.get(0).getStartTime());
        assertEquals(LocalTime.of(10,0), stops.get(0).getEndTime());
        assertEquals(LocalTime.of(10,0), stops.get(1).getStartTime());
        assertEquals(LocalTime.of(11,0), stops.get(1).getEndTime());
    }

    @Test
    void truncatedStops_whenEndPastDay() throws Exception {
        PreferenceDataAccessInterface pref = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geo = mock(GeocodingDataAccessInterface.class);

        // Profile
        PreferenceProfile profile = mock(PreferenceProfile.class);
        when(profile.getRadiusKm()).thenReturn(5.0);
        when(profile.getSelectedCategories()).thenReturn(Map.of());
        when(pref.loadForUser(1)).thenReturn(profile);

        // geocode OK
        GeocodeResult gr = mock(GeocodeResult.class);
        when(geo.geocode("Toronto")).thenReturn(gr);

        // 5 places starting at 23:00 → only 1 fits
        Place p = mock(Place.class);

        CapturingPresenter presenter = new CapturingPresenter();

        BuildPlanInteractor interactor =
                new BuildPlanInteractor(pref, geo, presenter);

        BuildPlanInputData input = new BuildPlanInputData(
                1,
                "Toronto",
                "2025-11-19",
                "23:00",
                List.of(p, p, p, p),
                null
        );

        interactor.execute(input);
        BuildPlanOutputData out = presenter.getOutput();

        assertTrue(out.isTruncated(), "Stops should be truncated");
        assertEquals(1, out.getPlan().getRoute().getStops().size());
    }

    @Test
    void exceptionCaught_returnsError() throws Exception {
        PreferenceDataAccessInterface pref = mock(PreferenceDataAccessInterface.class);
        GeocodingDataAccessInterface geo = mock(GeocodingDataAccessInterface.class);

        // throw exception purposely
        when(geo.geocode(anyString())).thenThrow(new RuntimeException("boom"));

        CapturingPresenter presenter = new CapturingPresenter();

        BuildPlanInteractor interactor =
                new BuildPlanInteractor(pref, geo, presenter);

        BuildPlanInputData input = new BuildPlanInputData(
                1,
                "X",
                "2025-11-19",
                "09:00",
                List.of(mock(Place.class)),
                null
        );

        interactor.execute(input);

        BuildPlanOutputData out = presenter.getOutput();
        assertNull(out.getPlan());
        assertEquals("boom", out.getErrorMessage());
    }
}
