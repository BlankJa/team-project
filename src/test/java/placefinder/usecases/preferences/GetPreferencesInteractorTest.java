package placefinder.usecases.preferences;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.FavoriteLocation;
import placefinder.entities.PreferenceProfile;
import placefinder.usecases.ports.PreferenceGateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GetPreferencesInteractor}.
 *
 * <p>This suite is designed to exercise:
 * <ul>
 *     <li>All lines of code (100% line coverage).</li>
 *     <li>All decision outcomes / paths (100% branch coverage).</li>
 * </ul>
 *
 * <p>Test scenarios:
 * <ul>
 *     <li>Successful retrieval of preferences</li>
 *     <li>Exception handling (gateway throws exception)</li>
 * </ul>
 */
class GetPreferencesInteractorTest {

    // -------------------------------------------------------------------------
    // Collaborators & System Under Test
    // -------------------------------------------------------------------------

    /** Persistence port used by the interactor (mock). */
    private PreferenceGateway preferenceGateway;

    /** Presenter / output boundary used by the interactor (mock). */
    private GetPreferencesOutputBoundary presenter;

    /** System under test. */
    private GetPreferencesInteractor interactor;

    // -------------------------------------------------------------------------
    // Test lifecycle
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        preferenceGateway = mock(PreferenceGateway.class);
        presenter         = mock(GetPreferencesOutputBoundary.class);
        interactor        = new GetPreferencesInteractor(preferenceGateway, presenter);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Captures the single {@link GetPreferencesOutputData} instance sent to the
     * presenter during an interactor execution.
     */
    private GetPreferencesOutputData capturePresenterOutput() {
        ArgumentCaptor<GetPreferencesOutputData> captor =
                ArgumentCaptor.forClass(GetPreferencesOutputData.class);
        verify(presenter).present(captor.capture());
        return captor.getValue();
    }

    // -------------------------------------------------------------------------
    // Test cases
    // -------------------------------------------------------------------------

    /**
     * Happy path: Successfully retrieve user preferences.
     *
     * <p>Exercises the success branch where:
     * <ul>
     *     <li>PreferenceProfile is loaded successfully</li>
     *     <li>Favorites list is retrieved successfully</li>
     *     <li>Output data contains all preference information</li>
     * </ul>
     */
    @Test
    void successfulRetrieval_returnsPreferencesData() throws Exception {
        int userId = 1;
        double radiusKm = 3.5;
        GetPreferencesInputData input = new GetPreferencesInputData(userId);

        // Create preference profile
        Map<String, List<String>> selectedCategories = new HashMap<>();
        selectedCategories.put("restaurants", List.of("italian", "chinese", "mexican"));
        selectedCategories.put("attractions", List.of("museums", "parks"));
        
        PreferenceProfile profile = new PreferenceProfile(userId, radiusKm, selectedCategories);

        // Create favorite locations
        FavoriteLocation fav1 = new FavoriteLocation(1, userId, "Location 1", "123 Main St", 43.6532, -79.3832);
        FavoriteLocation fav2 = new FavoriteLocation(2, userId, "Location 2", "456 Oak Ave", 45.5017, -73.5673);
        List<FavoriteLocation> favorites = List.of(fav1, fav2);

        when(preferenceGateway.loadForUser(userId)).thenReturn(profile);
        when(preferenceGateway.listFavorites(userId)).thenReturn(favorites);

        interactor.execute(input);

        verify(preferenceGateway).loadForUser(userId);
        verify(preferenceGateway).listFavorites(userId);

        GetPreferencesOutputData out = capturePresenterOutput();
        assertNull(out.getErrorMessage());
        assertEquals(radiusKm, out.getRadiusKm());
        assertEquals(favorites, out.getFavorites());
        assertEquals(selectedCategories, out.getSelectedCategories());
    }

    /**
     * Happy path: User with no favorites and empty categories.
     */
    @Test
    void successfulRetrievalWithEmptyData_returnsDefaultPreferences() throws Exception {
        int userId = 2;
        double radiusKm = 2.0;
        GetPreferencesInputData input = new GetPreferencesInputData(userId);

        PreferenceProfile profile = new PreferenceProfile(userId, radiusKm);
        List<FavoriteLocation> emptyFavorites = List.of();

        when(preferenceGateway.loadForUser(userId)).thenReturn(profile);
        when(preferenceGateway.listFavorites(userId)).thenReturn(emptyFavorites);

        interactor.execute(input);

        verify(preferenceGateway).loadForUser(userId);
        verify(preferenceGateway).listFavorites(userId);

        GetPreferencesOutputData out = capturePresenterOutput();
        assertNull(out.getErrorMessage());
        assertEquals(radiusKm, out.getRadiusKm());
        assertEquals(emptyFavorites, out.getFavorites());
        assertNotNull(out.getSelectedCategories());
    }

    /**
     * Error path: Gateway throws exception during loadForUser.
     *
     * <p>Exercises the {@code catch (Exception e)} block.
     */
    @Test
    void gatewayThrowsExceptionOnLoad_presenterReceivesFailureWithDefaults() throws Exception {
        int userId = 3;
        GetPreferencesInputData input = new GetPreferencesInputData(userId);

        when(preferenceGateway.loadForUser(userId))
                .thenThrow(new Exception("Database connection failed"));

        interactor.execute(input);

        verify(preferenceGateway).loadForUser(userId);
        verify(preferenceGateway, never()).listFavorites(anyInt());

        GetPreferencesOutputData out = capturePresenterOutput();
        assertNotNull(out.getErrorMessage());
        assertEquals("Database connection failed", out.getErrorMessage());
        assertEquals(2.0, out.getRadiusKm());  // default value
        assertEquals(List.of(), out.getFavorites());  // empty list
        assertNull(out.getSelectedCategories());
    }

    /**
     * Error path: Gateway throws exception during listFavorites.
     */
    @Test
    void gatewayThrowsExceptionOnListFavorites_presenterReceivesFailureWithDefaults() throws Exception {
        int userId = 4;
        GetPreferencesInputData input = new GetPreferencesInputData(userId);

        PreferenceProfile profile = new PreferenceProfile(userId, 1.5);
        
        when(preferenceGateway.loadForUser(userId)).thenReturn(profile);
        when(preferenceGateway.listFavorites(userId))
                .thenThrow(new Exception("Failed to load favorites"));

        interactor.execute(input);

        verify(preferenceGateway).loadForUser(userId);
        verify(preferenceGateway).listFavorites(userId);

        GetPreferencesOutputData out = capturePresenterOutput();
        assertNotNull(out.getErrorMessage());
        assertEquals("Failed to load favorites", out.getErrorMessage());
        assertEquals(2.0, out.getRadiusKm());  // default value
        assertEquals(List.of(), out.getFavorites());  // empty list
        assertNull(out.getSelectedCategories());
    }

    /**
     * Edge case: Exception with null message.
     */
    @Test
    void gatewayThrowsExceptionWithNullMessage_presenterReceivesNullErrorMessage() throws Exception {
        int userId = 5;
        GetPreferencesInputData input = new GetPreferencesInputData(userId);

        Exception ex = new Exception((String) null);
        when(preferenceGateway.loadForUser(userId)).thenThrow(ex);

        interactor.execute(input);

        GetPreferencesOutputData out = capturePresenterOutput();
        assertNull(out.getErrorMessage());
        assertEquals(2.0, out.getRadiusKm());
        assertEquals(List.of(), out.getFavorites());
        assertNull(out.getSelectedCategories());
    }
}

