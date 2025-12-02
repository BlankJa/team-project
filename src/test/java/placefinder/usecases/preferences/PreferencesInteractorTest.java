package placefinder.usecases.preferences;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.FavoriteLocation;
import placefinder.entities.PreferenceProfile;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Combined unit tests for {@link GetPreferencesInteractor} and {@link UpdatePreferencesInteractor}.
 *
 * <p>This suite is designed to exercise:
 * <ul>
 *     <li>All lines of code (100% line coverage).</li>
 *     <li>All decision outcomes / paths (100% branch coverage).</li>
 * </ul>
 */
class PreferencesInteractorTest {

    // -------------------------------------------------------------------------
    // Collaborators & System Under Test
    // -------------------------------------------------------------------------

    /** Persistence port used by the interactors (mock). */
    private PreferenceDataAccessInterface preferenceDataAccessInterface;

    /** Presenter / output boundary for GetPreferences (mock). */
    private GetPreferencesOutputBoundary getPreferencesPresenter;

    /** Presenter / output boundary for UpdatePreferences (mock). */
    private UpdatePreferencesOutputBoundary updatePreferencesPresenter;

    /** System under test - GetPreferences. */
    private GetPreferencesInteractor getPreferencesInteractor;

    /** System under test - UpdatePreferences. */
    private UpdatePreferencesInteractor updatePreferencesInteractor;

    // -------------------------------------------------------------------------
    // Test lifecycle
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        preferenceDataAccessInterface = mock(PreferenceDataAccessInterface.class);
        getPreferencesPresenter = mock(GetPreferencesOutputBoundary.class);
        updatePreferencesPresenter = mock(UpdatePreferencesOutputBoundary.class);
        getPreferencesInteractor = new GetPreferencesInteractor(preferenceDataAccessInterface, getPreferencesPresenter);
        updatePreferencesInteractor = new UpdatePreferencesInteractor(preferenceDataAccessInterface, updatePreferencesPresenter);
    }

    // -------------------------------------------------------------------------
    // Utility methods
    // -------------------------------------------------------------------------

    /**
     * Captures the single {@link GetPreferencesOutputData} instance sent to the
     * presenter during an interactor execution.
     */
    private GetPreferencesOutputData captureGetPreferencesOutput() {
        ArgumentCaptor<GetPreferencesOutputData> captor =
                ArgumentCaptor.forClass(GetPreferencesOutputData.class);
        verify(getPreferencesPresenter).present(captor.capture());
        return captor.getValue();
    }

    /**
     * Captures the single {@link UpdatePreferencesOutputData} instance sent to the
     * presenter during an interactor execution.
     */
    private UpdatePreferencesOutputData captureUpdatePreferencesOutput() {
        ArgumentCaptor<UpdatePreferencesOutputData> captor =
                ArgumentCaptor.forClass(UpdatePreferencesOutputData.class);
        verify(updatePreferencesPresenter).present(captor.capture());
        return captor.getValue();
    }

    // ========================================================================
    // GetPreferencesInteractor Tests
    // ========================================================================

    /**
     * Happy path: Successfully retrieve user preferences.
     */
    @Test
    void getPreferences_successfulRetrieval_returnsPreferencesData() throws Exception {
        int userId = 1;
        double radiusKm = 3.5;
        GetPreferencesInputData input = new GetPreferencesInputData(userId);

        Map<String, List<String>> selectedCategories = new HashMap<>();
        selectedCategories.put("restaurants", List.of("italian", "chinese", "mexican"));
        selectedCategories.put("attractions", List.of("museums", "parks"));
        
        PreferenceProfile profile = new PreferenceProfile(userId, radiusKm, selectedCategories);

        FavoriteLocation fav1 = new FavoriteLocation(1, userId, "Location 1", "123 Main St", 43.6532, -79.3832);
        FavoriteLocation fav2 = new FavoriteLocation(2, userId, "Location 2", "456 Oak Ave", 45.5017, -73.5673);
        List<FavoriteLocation> favorites = List.of(fav1, fav2);

        when(preferenceDataAccessInterface.loadForUser(userId)).thenReturn(profile);
        when(preferenceDataAccessInterface.listFavorites(userId)).thenReturn(favorites);

        getPreferencesInteractor.execute(input);

        verify(preferenceDataAccessInterface).loadForUser(userId);
        verify(preferenceDataAccessInterface).listFavorites(userId);

        GetPreferencesOutputData out = captureGetPreferencesOutput();
        assertNull(out.getErrorMessage());
        assertEquals(radiusKm, out.getRadiusKm());
        assertEquals(favorites, out.getFavorites());
        assertEquals(selectedCategories, out.getSelectedCategories());
    }

    /**
     * Happy path: User with no favorites and empty categories.
     */
    @Test
    void getPreferences_successfulRetrievalWithEmptyData_returnsDefaultPreferences() throws Exception {
        int userId = 2;
        double radiusKm = 2.0;
        GetPreferencesInputData input = new GetPreferencesInputData(userId);

        PreferenceProfile profile = new PreferenceProfile(userId, radiusKm);
        List<FavoriteLocation> emptyFavorites = List.of();

        when(preferenceDataAccessInterface.loadForUser(userId)).thenReturn(profile);
        when(preferenceDataAccessInterface.listFavorites(userId)).thenReturn(emptyFavorites);

        getPreferencesInteractor.execute(input);

        verify(preferenceDataAccessInterface).loadForUser(userId);
        verify(preferenceDataAccessInterface).listFavorites(userId);

        GetPreferencesOutputData out = captureGetPreferencesOutput();
        assertNull(out.getErrorMessage());
        assertEquals(radiusKm, out.getRadiusKm());
        assertEquals(emptyFavorites, out.getFavorites());
        assertNotNull(out.getSelectedCategories());
    }

    /**
     * Error path: Gateway throws exception during loadForUser.
     */
    @Test
    void getPreferences_gatewayThrowsExceptionOnLoad_presenterReceivesFailureWithDefaults() throws Exception {
        int userId = 3;
        GetPreferencesInputData input = new GetPreferencesInputData(userId);

        when(preferenceDataAccessInterface.loadForUser(userId))
                .thenThrow(new Exception("Database connection failed"));

        getPreferencesInteractor.execute(input);

        verify(preferenceDataAccessInterface).loadForUser(userId);
        verify(preferenceDataAccessInterface, never()).listFavorites(anyInt());

        GetPreferencesOutputData out = captureGetPreferencesOutput();
        assertNotNull(out.getErrorMessage());
        assertEquals("Database connection failed", out.getErrorMessage());
        assertEquals(2.0, out.getRadiusKm());
        assertEquals(List.of(), out.getFavorites());
        assertNull(out.getSelectedCategories());
    }

    /**
     * Error path: Gateway throws exception during listFavorites.
     */
    @Test
    void getPreferences_gatewayThrowsExceptionOnListFavorites_presenterReceivesFailureWithDefaults() throws Exception {
        int userId = 4;
        GetPreferencesInputData input = new GetPreferencesInputData(userId);

        PreferenceProfile profile = new PreferenceProfile(userId, 1.5);
        
        when(preferenceDataAccessInterface.loadForUser(userId)).thenReturn(profile);
        when(preferenceDataAccessInterface.listFavorites(userId))
                .thenThrow(new Exception("Failed to load favorites"));

        getPreferencesInteractor.execute(input);

        verify(preferenceDataAccessInterface).loadForUser(userId);
        verify(preferenceDataAccessInterface).listFavorites(userId);

        GetPreferencesOutputData out = captureGetPreferencesOutput();
        assertNotNull(out.getErrorMessage());
        assertEquals("Failed to load favorites", out.getErrorMessage());
        assertEquals(2.0, out.getRadiusKm());
        assertEquals(List.of(), out.getFavorites());
        assertNull(out.getSelectedCategories());
    }

    /**
     * Edge case: Exception with null message.
     */
    @Test
    void getPreferences_gatewayThrowsExceptionWithNullMessage_presenterReceivesNullErrorMessage() throws Exception {
        int userId = 5;
        GetPreferencesInputData input = new GetPreferencesInputData(userId);

        Exception ex = new Exception((String) null);
        when(preferenceDataAccessInterface.loadForUser(userId)).thenThrow(ex);

        getPreferencesInteractor.execute(input);

        GetPreferencesOutputData out = captureGetPreferencesOutput();
        assertNull(out.getErrorMessage());
        assertEquals(2.0, out.getRadiusKm());
        assertEquals(List.of(), out.getFavorites());
        assertNull(out.getSelectedCategories());
    }

    // ========================================================================
    // UpdatePreferencesInteractor Tests
    // ========================================================================

    /**
     * Branch 1a: Negative radius triggers validation error.
     */
    @Test
    void updatePreferences_negativeRadius_triggersValidationError() throws Exception {
        int userId = 1;
        double invalidRadius = -1.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, invalidRadius, categories);

        updatePreferencesInteractor.execute(input);

        verifyNoInteractions(preferenceDataAccessInterface);

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertFalse(out.isSuccess());
        assertEquals("Radius must be between 0 and 5 km.", out.getMessage());
    }

    /**
     * Branch 1b: Radius greater than 5 triggers validation error.
     */
    @Test
    void updatePreferences_radiusGreaterThan5_triggersValidationError() throws Exception {
        int userId = 2;
        double invalidRadius = 6.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, invalidRadius, categories);

        updatePreferencesInteractor.execute(input);

        verifyNoInteractions(preferenceDataAccessInterface);

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertFalse(out.isSuccess());
        assertEquals("Radius must be between 0 and 5 km.", out.getMessage());
    }

    /**
     * Branch 1c: Radius exactly 5 should be valid (boundary value).
     */
    @Test
    void updatePreferences_radiusExactly5_isValid() throws Exception {
        int userId = 3;
        double validRadius = 5.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 2.0);
        when(preferenceDataAccessInterface.loadForUser(userId)).thenReturn(existingProfile);

        updatePreferencesInteractor.execute(input);

        verify(preferenceDataAccessInterface).loadForUser(userId);
        verify(preferenceDataAccessInterface).saveForUser(any(PreferenceProfile.class));

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertTrue(out.isSuccess());
        assertEquals("Preferences saved.", out.getMessage());
    }

    /**
     * Branch 1d: Radius exactly 0 should be valid (boundary value).
     */
    @Test
    void updatePreferences_radiusExactly0_isValid() throws Exception {
        int userId = 4;
        double validRadius = 0.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 2.0);
        when(preferenceDataAccessInterface.loadForUser(userId)).thenReturn(existingProfile);

        updatePreferencesInteractor.execute(input);

        verify(preferenceDataAccessInterface).loadForUser(userId);
        verify(preferenceDataAccessInterface).saveForUser(any(PreferenceProfile.class));

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertTrue(out.isSuccess());
    }

    /**
     * Branch 2a: Less than 3 sub-categories triggers validation error.
     */
    @Test
    void updatePreferences_lessThan3SubCategories_triggersValidationError() throws Exception {
        int userId = 5;
        double validRadius = 2.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese"));  // only 2 sub-categories

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        updatePreferencesInteractor.execute(input);

        verifyNoInteractions(preferenceDataAccessInterface);

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please select at least 3 sub-categories.", out.getMessage());
    }

    /**
     * Branch 2b: Exactly 2 sub-categories triggers validation error.
     */
    @Test
    void updatePreferences_exactly2SubCategories_triggersValidationError() throws Exception {
        int userId = 6;
        double validRadius = 3.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian"));
        categories.put("attractions", List.of("museums"));  // total: 2 sub-categories

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        updatePreferencesInteractor.execute(input);

        verifyNoInteractions(preferenceDataAccessInterface);

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please select at least 3 sub-categories.", out.getMessage());
    }

    /**
     * Branch 2c: Null selectedCategories triggers validation error.
     */
    @Test
    void updatePreferences_nullSelectedCategories_triggersValidationError() throws Exception {
        int userId = 7;
        double validRadius = 2.0;

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, null);

        updatePreferencesInteractor.execute(input);

        verifyNoInteractions(preferenceDataAccessInterface);

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please select at least 3 sub-categories.", out.getMessage());
    }

    /**
     * Branch 2d: Empty selectedCategories triggers validation error.
     */
    @Test
    void updatePreferences_emptySelectedCategories_triggersValidationError() throws Exception {
        int userId = 8;
        double validRadius = 2.0;
        Map<String, List<String>> emptyCategories = new HashMap<>();

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, emptyCategories);

        updatePreferencesInteractor.execute(input);

        verifyNoInteractions(preferenceDataAccessInterface);

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please select at least 3 sub-categories.", out.getMessage());
    }

    /**
     * Branch 2e: SelectedCategories with null lists should be handled.
     */
    @Test
    void updatePreferences_selectedCategoriesWithNullLists_triggersValidationError() throws Exception {
        int userId = 9;
        double validRadius = 2.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", null);
        categories.put("attractions", List.of("museums", "parks"));  // only 2 valid sub-categories

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        updatePreferencesInteractor.execute(input);

        verifyNoInteractions(preferenceDataAccessInterface);

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please select at least 3 sub-categories.", out.getMessage());
    }

    /**
     * Branch 2f: Exactly 3 sub-categories should be valid (boundary value).
     */
    @Test
    void updatePreferences_exactly3SubCategories_isValid() throws Exception {
        int userId = 10;
        double validRadius = 2.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));  // exactly 3

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 1.0);
        when(preferenceDataAccessInterface.loadForUser(userId)).thenReturn(existingProfile);

        updatePreferencesInteractor.execute(input);

        verify(preferenceDataAccessInterface).loadForUser(userId);
        verify(preferenceDataAccessInterface).saveForUser(any(PreferenceProfile.class));

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertTrue(out.isSuccess());
        assertEquals("Preferences saved.", out.getMessage());
    }

    /**
     * Branch 3 (happy path): Valid input successfully updates preferences.
     */
    @Test
    void updatePreferences_validInput_updatesPreferencesSuccessfully() throws Exception {
        int userId = 11;
        double newRadius = 3.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));
        categories.put("attractions", List.of("museums", "parks", "theaters"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, newRadius, categories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 2.0);
        when(preferenceDataAccessInterface.loadForUser(userId)).thenReturn(existingProfile);

        updatePreferencesInteractor.execute(input);

        verify(preferenceDataAccessInterface).loadForUser(userId);
        
        ArgumentCaptor<PreferenceProfile> profileCaptor = 
                ArgumentCaptor.forClass(PreferenceProfile.class);
        verify(preferenceDataAccessInterface).saveForUser(profileCaptor.capture());
        
        PreferenceProfile savedProfile = profileCaptor.getValue();
        assertEquals(userId, savedProfile.getUserId());
        assertEquals(newRadius, savedProfile.getRadiusKm());
        assertEquals(categories, savedProfile.getSelectedCategories());

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertTrue(out.isSuccess());
        assertEquals("Preferences saved.", out.getMessage());
    }

    /**
     * Branch 3b: Update with valid categories saves preferences successfully.
     */
    @Test
    void updatePreferences_validInputWithValidCategories_savesPreferences() throws Exception {
        int userId = 12;
        double newRadius = 2.0;
        Map<String, List<String>> validCategories = new HashMap<>();
        validCategories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, newRadius, validCategories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 1.0);
        when(preferenceDataAccessInterface.loadForUser(userId)).thenReturn(existingProfile);

        updatePreferencesInteractor.execute(input);

        ArgumentCaptor<PreferenceProfile> profileCaptor = 
                ArgumentCaptor.forClass(PreferenceProfile.class);
        verify(preferenceDataAccessInterface).saveForUser(profileCaptor.capture());
        
        PreferenceProfile savedProfile = profileCaptor.getValue();
        assertNotNull(savedProfile.getSelectedCategories());
        assertEquals(validCategories, savedProfile.getSelectedCategories());

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertTrue(out.isSuccess());
        assertEquals("Preferences saved.", out.getMessage());
    }

    /**
     * Branch 4: Gateway throws exception during loadForUser.
     */
    @Test
    void updatePreferences_gatewayThrowsExceptionOnLoad_presenterReceivesFailureMessage() throws Exception {
        int userId = 13;
        double validRadius = 2.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        when(preferenceDataAccessInterface.loadForUser(userId))
                .thenThrow(new Exception("Database connection failed"));

        updatePreferencesInteractor.execute(input);

        verify(preferenceDataAccessInterface).loadForUser(userId);
        verify(preferenceDataAccessInterface, never()).saveForUser(any());

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertFalse(out.isSuccess());
        assertEquals("Database connection failed", out.getMessage());
    }

    /**
     * Branch 4b: Gateway throws exception during saveForUser.
     */
    @Test
    void updatePreferences_gatewayThrowsExceptionOnSave_presenterReceivesFailureMessage() throws Exception {
        int userId = 14;
        double validRadius = 2.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 1.0);
        when(preferenceDataAccessInterface.loadForUser(userId)).thenReturn(existingProfile);
        doThrow(new Exception("Save operation failed"))
                .when(preferenceDataAccessInterface).saveForUser(any(PreferenceProfile.class));

        updatePreferencesInteractor.execute(input);

        verify(preferenceDataAccessInterface).loadForUser(userId);
        verify(preferenceDataAccessInterface).saveForUser(any(PreferenceProfile.class));

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertFalse(out.isSuccess());
        assertEquals("Save operation failed", out.getMessage());
    }

    /**
     * Edge case: Exception with null message.
     */
    @Test
    void updatePreferences_gatewayThrowsExceptionWithNullMessage_presenterReceivesNullMessage() throws Exception {
        int userId = 15;
        double validRadius = 2.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        Exception ex = new Exception((String) null);
        when(preferenceDataAccessInterface.loadForUser(userId)).thenThrow(ex);

        updatePreferencesInteractor.execute(input);

        UpdatePreferencesOutputData out = captureUpdatePreferencesOutput();
        assertFalse(out.isSuccess());
        assertNull(out.getMessage());
    }

}

