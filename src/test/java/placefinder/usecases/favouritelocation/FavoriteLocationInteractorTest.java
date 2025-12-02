package placefinder.usecases.favouritelocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.FavoriteLocation;
import placefinder.entities.GeocodeResult;
import placefinder.usecases.dataacessinterfaces.GeocodingDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Favorite Location interactors.
 *
 * <p>This suite is designed to exercise:
 * <ul>
 *     <li>All lines of code (100% line coverage).</li>
 *     <li>All decision outcomes / paths (100% branch coverage).</li>
 * </ul>
 *
 * <p>Test scenarios:
 * <ul>
 *     <li>AddFavoriteInteractor: successful add, location not found, exception handling</li>
 *     <li>DeleteFavoriteInteractor: successful delete, exception handling</li>
 * </ul>
 */
class FavoriteLocationInteractorTest {

    // -------------------------------------------------------------------------
    // Collaborators & System Under Test
    // -------------------------------------------------------------------------

    /** Persistence port used by the interactors (mock). */
    private PreferenceDataAccessInterface preferenceDataAccessInterface;

    /** Geocoding port used by AddFavoriteInteractor (mock). */
    private GeocodingDataAccessInterface geocodingDataAccessInterface;

    /** Presenter / output boundary for AddFavorite (mock). */
    private AddFavoriteOutputBoundary addFavoritePresenter;

    /** Presenter / output boundary for DeleteFavorite (mock). */
    private DeleteFavoriteOutputBoundary deleteFavoritePresenter;

    /** System under test - AddFavorite. */
    private AddFavoriteInteractor addFavoriteInteractor;

    /** System under test - DeleteFavorite. */
    private DeleteFavoriteInteractor deleteFavoriteInteractor;

    // -------------------------------------------------------------------------
    // Test lifecycle
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        preferenceDataAccessInterface = mock(PreferenceDataAccessInterface.class);
        geocodingDataAccessInterface = mock(GeocodingDataAccessInterface.class);
        addFavoritePresenter = mock(AddFavoriteOutputBoundary.class);
        deleteFavoritePresenter = mock(DeleteFavoriteOutputBoundary.class);
        addFavoriteInteractor = new AddFavoriteInteractor(
                preferenceDataAccessInterface,
                geocodingDataAccessInterface,
                addFavoritePresenter
        );
        deleteFavoriteInteractor = new DeleteFavoriteInteractor(
                preferenceDataAccessInterface,
                deleteFavoritePresenter
        );
    }

    // -------------------------------------------------------------------------
    // Utility methods
    // -------------------------------------------------------------------------

    /**
     * Captures the single {@link AddFavoriteOutputData} instance sent to the
     * presenter during an interactor execution.
     */
    private AddFavoriteOutputData captureAddFavoriteOutput() {
        ArgumentCaptor<AddFavoriteOutputData> captor =
                ArgumentCaptor.forClass(AddFavoriteOutputData.class);
        verify(addFavoritePresenter).present(captor.capture());
        return captor.getValue();
    }

    /**
     * Captures the single {@link DeleteFavoriteOutputData} instance sent to the
     * presenter during an interactor execution.
     */
    private DeleteFavoriteOutputData captureDeleteFavoriteOutput() {
        ArgumentCaptor<DeleteFavoriteOutputData> captor =
                ArgumentCaptor.forClass(DeleteFavoriteOutputData.class);
        verify(deleteFavoritePresenter).present(captor.capture());
        return captor.getValue();
    }

    // ========================================================================
    // AddFavoriteInteractor Tests
    // ========================================================================

    /**
     * Happy path: Successfully add favorite location.
     *
     * <p>Exercises the branch where geocode returns non-null and addFavorite succeeds.
     */
    @Test
    void addFavorite_successfulAdd_returnsFavoriteLocation() throws Exception {
        int userId = 1;
        String locationName = "My Home";
        String address = "123 Main St, Toronto, ON";
        String formattedAddress = "123 Main St, Toronto, ON, Canada";
        double lat = 43.6532;
        double lon = -79.3832;

        AddFavoriteInputData input = new AddFavoriteInputData(userId, locationName, address);

        GeocodeResult geo = new GeocodeResult(lat, lon, formattedAddress);
        FavoriteLocation expectedFavorite = new FavoriteLocation(
                1, userId, locationName, formattedAddress, lat, lon
        );

        when(geocodingDataAccessInterface.geocode(address)).thenReturn(geo);
        when(preferenceDataAccessInterface.addFavorite(
                userId, locationName, formattedAddress, lat, lon
        )).thenReturn(expectedFavorite);

        addFavoriteInteractor.execute(input);

        verify(geocodingDataAccessInterface).geocode(address);
        verify(preferenceDataAccessInterface).addFavorite(
                userId, locationName, formattedAddress, lat, lon
        );

        AddFavoriteOutputData out = captureAddFavoriteOutput();
        assertNull(out.getErrorMessage());
        assertNotNull(out.getFavorite());
        assertEquals(expectedFavorite.getId(), out.getFavorite().getId());
        assertEquals(userId, out.getFavorite().getUserId());
        assertEquals(locationName, out.getFavorite().getName());
        assertEquals(formattedAddress, out.getFavorite().getAddress());
        assertEquals(lat, out.getFavorite().getLat(), 0.0001);
        assertEquals(lon, out.getFavorite().getLon(), 0.0001);
    }

    /**
     * Branch 2: Location not found (geocode returns null).
     *
     * <p>Exercises the {@code geo == null} branch.
     */
    @Test
    void addFavorite_locationNotFound_returnsErrorMessage() throws Exception {
        int userId = 2;
        String locationName = "Non-existent Location";
        String address = "Invalid Address 99999";

        AddFavoriteInputData input = new AddFavoriteInputData(userId, locationName, address);

        when(geocodingDataAccessInterface.geocode(address)).thenReturn(null);

        addFavoriteInteractor.execute(input);

        verify(geocodingDataAccessInterface).geocode(address);
        verify(preferenceDataAccessInterface, never()).addFavorite(
                anyInt(), anyString(), anyString(), anyDouble(), anyDouble()
        );

        AddFavoriteOutputData out = captureAddFavoriteOutput();
        assertNull(out.getFavorite());
        assertEquals("Could not find that location.", out.getErrorMessage());
    }

    /**
     * Branch 3: geocode throws exception.
     *
     * <p>Exercises the {@code catch (Exception e)} block when geocode throws exception.
     */
    @Test
    void addFavorite_geocodeThrowsException_presenterReceivesErrorMessage() throws Exception {
        int userId = 3;
        String locationName = "Test Location";
        String address = "Test Address";

        AddFavoriteInputData input = new AddFavoriteInputData(userId, locationName, address);

        when(geocodingDataAccessInterface.geocode(address))
                .thenThrow(new Exception("Geocoding service unavailable"));

        addFavoriteInteractor.execute(input);

        verify(geocodingDataAccessInterface).geocode(address);
        verify(preferenceDataAccessInterface, never()).addFavorite(
                anyInt(), anyString(), anyString(), anyDouble(), anyDouble()
        );

        AddFavoriteOutputData out = captureAddFavoriteOutput();
        assertNull(out.getFavorite());
        assertEquals("Geocoding service unavailable", out.getErrorMessage());
    }

    /**
     * Branch 4: addFavorite throws exception.
     *
     * <p>Exercises the {@code catch (Exception e)} block when addFavorite throws exception.
     */
    @Test
    void addFavorite_addFavoriteThrowsException_presenterReceivesErrorMessage() throws Exception {
        int userId = 4;
        String locationName = "Test Location";
        String address = "456 Oak Ave, Vancouver, BC";
        String formattedAddress = "456 Oak Ave, Vancouver, BC, Canada";
        double lat = 49.2827;
        double lon = -123.1207;

        AddFavoriteInputData input = new AddFavoriteInputData(userId, locationName, address);

        GeocodeResult geo = new GeocodeResult(lat, lon, formattedAddress);

        when(geocodingDataAccessInterface.geocode(address)).thenReturn(geo);
        when(preferenceDataAccessInterface.addFavorite(
                userId, locationName, formattedAddress, lat, lon
        )).thenThrow(new Exception("Database connection failed"));

        addFavoriteInteractor.execute(input);

        verify(geocodingDataAccessInterface).geocode(address);
        verify(preferenceDataAccessInterface).addFavorite(
                userId, locationName, formattedAddress, lat, lon
        );

        AddFavoriteOutputData out = captureAddFavoriteOutput();
        assertNull(out.getFavorite());
        assertEquals("Database connection failed", out.getErrorMessage());
    }

    /**
     * Edge case: Exception with null message.
     */
    @Test
    void addFavorite_exceptionWithNullMessage_presenterReceivesNullErrorMessage() throws Exception {
        int userId = 5;
        String locationName = "Test Location";
        String address = "Test Address";

        AddFavoriteInputData input = new AddFavoriteInputData(userId, locationName, address);

        Exception ex = new Exception((String) null);
        when(geocodingDataAccessInterface.geocode(address)).thenThrow(ex);

        addFavoriteInteractor.execute(input);

        AddFavoriteOutputData out = captureAddFavoriteOutput();
        assertNull(out.getFavorite());
        assertNull(out.getErrorMessage());
    }

    /**
     * Edge case: Verify all fields are correctly passed when successfully adding.
     */
    @Test
    void addFavorite_successfulAddWithDifferentCoordinates_returnsCorrectFavorite() throws Exception {
        int userId = 6;
        String locationName = "CN Tower";
        String address = "CN Tower, Toronto";
        String formattedAddress = "290 Bremner Blvd, Toronto, ON M5V 3L9, Canada";
        double lat = 43.6426;
        double lon = -79.3871;

        AddFavoriteInputData input = new AddFavoriteInputData(userId, locationName, address);

        GeocodeResult geo = new GeocodeResult(lat, lon, formattedAddress);
        FavoriteLocation expectedFavorite = new FavoriteLocation(
                2, userId, locationName, formattedAddress, lat, lon
        );

        when(geocodingDataAccessInterface.geocode(address)).thenReturn(geo);
        when(preferenceDataAccessInterface.addFavorite(
                userId, locationName, formattedAddress, lat, lon
        )).thenReturn(expectedFavorite);

        addFavoriteInteractor.execute(input);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> addressCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> latCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> lonCaptor = ArgumentCaptor.forClass(Double.class);

        verify(preferenceDataAccessInterface).addFavorite(
                eq(userId),
                nameCaptor.capture(),
                addressCaptor.capture(),
                latCaptor.capture(),
                lonCaptor.capture()
        );

        assertEquals(locationName, nameCaptor.getValue());
        assertEquals(formattedAddress, addressCaptor.getValue());
        assertEquals(lat, latCaptor.getValue(), 0.0001);
        assertEquals(lon, lonCaptor.getValue(), 0.0001);
    }

    // ========================================================================
    // DeleteFavoriteInteractor Tests
    // ========================================================================

    /**
     * Happy path: Successfully delete favorite location.
     *
     * <p>Exercises the branch where deleteFavorite succeeds.
     */
    @Test
    void deleteFavorite_successfulDelete_returnsSuccessMessage() throws Exception {
        int userId = 1;
        int favoriteId = 10;

        DeleteFavoriteInputData input = new DeleteFavoriteInputData(userId, favoriteId);

        doNothing().when(preferenceDataAccessInterface).deleteFavorite(favoriteId, userId);

        deleteFavoriteInteractor.execute(input);

        verify(preferenceDataAccessInterface).deleteFavorite(favoriteId, userId);

        DeleteFavoriteOutputData out = captureDeleteFavoriteOutput();
        assertTrue(out.isSuccess());
        assertEquals("Favorite deleted.", out.getMessage());
    }

    /**
     * Branch 2: deleteFavorite throws exception.
     *
     * <p>Exercises the {@code catch (Exception e)} block.
     */
    @Test
    void deleteFavorite_deleteThrowsException_presenterReceivesFailureMessage() throws Exception {
        int userId = 2;
        int favoriteId = 20;

        DeleteFavoriteInputData input = new DeleteFavoriteInputData(userId, favoriteId);

        doThrow(new Exception("Favorite not found"))
                .when(preferenceDataAccessInterface).deleteFavorite(favoriteId, userId);

        deleteFavoriteInteractor.execute(input);

        verify(preferenceDataAccessInterface).deleteFavorite(favoriteId, userId);

        DeleteFavoriteOutputData out = captureDeleteFavoriteOutput();
        assertFalse(out.isSuccess());
        assertEquals("Favorite not found", out.getMessage());
    }

    /**
     * Edge case: Exception with null message.
     */
    @Test
    void deleteFavorite_exceptionWithNullMessage_presenterReceivesNullMessage() throws Exception {
        int userId = 3;
        int favoriteId = 30;

        DeleteFavoriteInputData input = new DeleteFavoriteInputData(userId, favoriteId);

        Exception ex = new Exception((String) null);
        doThrow(ex).when(preferenceDataAccessInterface).deleteFavorite(favoriteId, userId);

        deleteFavoriteInteractor.execute(input);

        DeleteFavoriteOutputData out = captureDeleteFavoriteOutput();
        assertFalse(out.isSuccess());
        assertNull(out.getMessage());
    }

    /**
     * Edge case: Verify deletion of favorite location with different ID.
     */
    @Test
    void deleteFavorite_deleteDifferentFavoriteId_returnsSuccessMessage() throws Exception {
        int userId = 4;
        int favoriteId = 999;

        DeleteFavoriteInputData input = new DeleteFavoriteInputData(userId, favoriteId);

        doNothing().when(preferenceDataAccessInterface).deleteFavorite(favoriteId, userId);

        deleteFavoriteInteractor.execute(input);

        verify(preferenceDataAccessInterface).deleteFavorite(favoriteId, userId);

        DeleteFavoriteOutputData out = captureDeleteFavoriteOutput();
        assertTrue(out.isSuccess());
        assertEquals("Favorite deleted.", out.getMessage());
    }

    /**
     * Edge case: Database connection exception.
     */
    @Test
    void deleteFavorite_databaseConnectionException_presenterReceivesErrorMessage() throws Exception {
        int userId = 5;
        int favoriteId = 50;

        DeleteFavoriteInputData input = new DeleteFavoriteInputData(userId, favoriteId);

        doThrow(new Exception("Database connection failed"))
                .when(preferenceDataAccessInterface).deleteFavorite(favoriteId, userId);

        deleteFavoriteInteractor.execute(input);

        verify(preferenceDataAccessInterface).deleteFavorite(favoriteId, userId);

        DeleteFavoriteOutputData out = captureDeleteFavoriteOutput();
        assertFalse(out.isSuccess());
        assertEquals("Database connection failed", out.getMessage());
    }
}

