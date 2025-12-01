package placefinder.usecases.preferences;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import placefinder.entities.PreferenceProfile;
import placefinder.usecases.ports.PreferenceGateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UpdatePreferencesInteractor}.
 *
 * <p>This suite is designed to exercise:
 * <ul>
 *     <li>All lines of code (100% line coverage).</li>
 *     <li>All decision outcomes / paths (100% branch coverage).</li>
 * </ul>
 *
 * <p>Test scenarios:
 * <ul>
 *     <li>Radius validation (negative radius)</li>
 *     <li>Radius validation (radius > 5)</li>
 *     <li>Sub-categories validation (less than 3 sub-categories)</li>
 *     <li>Sub-categories validation (null selectedCategories)</li>
 *     <li>Sub-categories validation (empty selectedCategories)</li>
 *     <li>Successful update</li>
 *     <li>Exception handling (gateway throws exception)</li>
 * </ul>
 */
class UpdatePreferencesInteractorTest {

    // -------------------------------------------------------------------------
    // Collaborators & System Under Test
    // -------------------------------------------------------------------------

    /** Persistence port used by the interactor (mock). */
    private PreferenceGateway preferenceGateway;

    /** Presenter / output boundary used by the interactor (mock). */
    private UpdatePreferencesOutputBoundary presenter;

    /** System under test. */
    private UpdatePreferencesInteractor interactor;

    // -------------------------------------------------------------------------
    // Test lifecycle
    // -------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        preferenceGateway = mock(PreferenceGateway.class);
        presenter         = mock(UpdatePreferencesOutputBoundary.class);
        interactor        = new UpdatePreferencesInteractor(preferenceGateway, presenter);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Captures the single {@link UpdatePreferencesOutputData} instance sent to the
     * presenter during an interactor execution.
     */
    private UpdatePreferencesOutputData capturePresenterOutput() {
        ArgumentCaptor<UpdatePreferencesOutputData> captor =
                ArgumentCaptor.forClass(UpdatePreferencesOutputData.class);
        verify(presenter).present(captor.capture());
        return captor.getValue();
    }

    // -------------------------------------------------------------------------
    // Validation branch tests
    // -------------------------------------------------------------------------

    /**
     * Branch 1a: Negative radius triggers validation error.
     */
    @Test
    void negativeRadius_triggersValidationError() throws Exception {
        int userId = 1;
        double invalidRadius = -1.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, invalidRadius, categories);

        interactor.execute(input);

        verifyNoInteractions(preferenceGateway);

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Radius must be between 0 and 5 km.", out.getMessage());
    }

    /**
     * Branch 1b: Radius greater than 5 triggers validation error.
     */
    @Test
    void radiusGreaterThan5_triggersValidationError() throws Exception {
        int userId = 2;
        double invalidRadius = 6.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, invalidRadius, categories);

        interactor.execute(input);

        verifyNoInteractions(preferenceGateway);

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Radius must be between 0 and 5 km.", out.getMessage());
    }

    /**
     * Branch 1c: Radius exactly 5 should be valid (boundary value).
     */
    @Test
    void radiusExactly5_isValid() throws Exception {
        int userId = 3;
        double validRadius = 5.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 2.0);
        when(preferenceGateway.loadForUser(userId)).thenReturn(existingProfile);

        interactor.execute(input);

        verify(preferenceGateway).loadForUser(userId);
        verify(preferenceGateway).saveForUser(any(PreferenceProfile.class));

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
        assertEquals("Preferences saved.", out.getMessage());
    }

    /**
     * Branch 1d: Radius exactly 0 should be valid (boundary value).
     */
    @Test
    void radiusExactly0_isValid() throws Exception {
        int userId = 4;
        double validRadius = 0.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 2.0);
        when(preferenceGateway.loadForUser(userId)).thenReturn(existingProfile);

        interactor.execute(input);

        verify(preferenceGateway).loadForUser(userId);
        verify(preferenceGateway).saveForUser(any(PreferenceProfile.class));

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
    }

    /**
     * Branch 2a: Less than 3 sub-categories triggers validation error.
     */
    @Test
    void lessThan3SubCategories_triggersValidationError() throws Exception {
        int userId = 5;
        double validRadius = 2.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese"));  // only 2 sub-categories

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        interactor.execute(input);

        verifyNoInteractions(preferenceGateway);

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please select at least 3 sub-categories.", out.getMessage());
    }

    /**
     * Branch 2b: Exactly 2 sub-categories triggers validation error.
     */
    @Test
    void exactly2SubCategories_triggersValidationError() throws Exception {
        int userId = 6;
        double validRadius = 3.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian"));
        categories.put("attractions", List.of("museums"));  // total: 2 sub-categories

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        interactor.execute(input);

        verifyNoInteractions(preferenceGateway);

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please select at least 3 sub-categories.", out.getMessage());
    }

    /**
     * Branch 2c: Null selectedCategories triggers validation error.
     */
    @Test
    void nullSelectedCategories_triggersValidationError() throws Exception {
        int userId = 7;
        double validRadius = 2.0;

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, null);

        interactor.execute(input);

        verifyNoInteractions(preferenceGateway);

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please select at least 3 sub-categories.", out.getMessage());
    }

    /**
     * Branch 2d: Empty selectedCategories triggers validation error.
     */
    @Test
    void emptySelectedCategories_triggersValidationError() throws Exception {
        int userId = 8;
        double validRadius = 2.0;
        Map<String, List<String>> emptyCategories = new HashMap<>();

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, emptyCategories);

        interactor.execute(input);

        verifyNoInteractions(preferenceGateway);

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please select at least 3 sub-categories.", out.getMessage());
    }

    /**
     * Branch 2e: SelectedCategories with null lists should be handled.
     */
    @Test
    void selectedCategoriesWithNullLists_triggersValidationError() throws Exception {
        int userId = 9;
        double validRadius = 2.0;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", null);
        categories.put("attractions", List.of("museums", "parks"));  // only 2 valid sub-categories

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        interactor.execute(input);

        verifyNoInteractions(preferenceGateway);

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Please select at least 3 sub-categories.", out.getMessage());
    }

    /**
     * Branch 2f: Exactly 3 sub-categories should be valid (boundary value).
     */
    @Test
    void exactly3SubCategories_isValid() throws Exception {
        int userId = 10;
        double validRadius = 2.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));  // exactly 3

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 1.0);
        when(preferenceGateway.loadForUser(userId)).thenReturn(existingProfile);

        interactor.execute(input);

        verify(preferenceGateway).loadForUser(userId);
        verify(preferenceGateway).saveForUser(any(PreferenceProfile.class));

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
        assertEquals("Preferences saved.", out.getMessage());
    }

    // -------------------------------------------------------------------------
    // Success path
    // -------------------------------------------------------------------------

    /**
     * Branch 3 (happy path): Valid input successfully updates preferences.
     */
    @Test
    void validInput_updatesPreferencesSuccessfully() throws Exception {
        int userId = 11;
        double newRadius = 3.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));
        categories.put("attractions", List.of("museums", "parks", "theaters"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, newRadius, categories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 2.0);
        when(preferenceGateway.loadForUser(userId)).thenReturn(existingProfile);

        interactor.execute(input);

        verify(preferenceGateway).loadForUser(userId);
        
        ArgumentCaptor<PreferenceProfile> profileCaptor = 
                ArgumentCaptor.forClass(PreferenceProfile.class);
        verify(preferenceGateway).saveForUser(profileCaptor.capture());
        
        PreferenceProfile savedProfile = profileCaptor.getValue();
        assertEquals(userId, savedProfile.getUserId());
        assertEquals(newRadius, savedProfile.getRadiusKm());
        assertEquals(categories, savedProfile.getSelectedCategories());

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
        assertEquals("Preferences saved.", out.getMessage());
    }

    /**
     * Branch 3b: Update with valid categories saves preferences successfully.
     */
    @Test
    void validInputWithValidCategories_savesPreferences() throws Exception {
        int userId = 12;
        double newRadius = 2.0;
        Map<String, List<String>> validCategories = new HashMap<>();
        validCategories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, newRadius, validCategories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 1.0);
        when(preferenceGateway.loadForUser(userId)).thenReturn(existingProfile);

        interactor.execute(input);

        ArgumentCaptor<PreferenceProfile> profileCaptor = 
                ArgumentCaptor.forClass(PreferenceProfile.class);
        verify(preferenceGateway).saveForUser(profileCaptor.capture());
        
        PreferenceProfile savedProfile = profileCaptor.getValue();
        assertNotNull(savedProfile.getSelectedCategories());
        assertEquals(validCategories, savedProfile.getSelectedCategories());

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertTrue(out.isSuccess());
        assertEquals("Preferences saved.", out.getMessage());
    }

    // -------------------------------------------------------------------------
    // Exception handling
    // -------------------------------------------------------------------------

    /**
     * Branch 4: Gateway throws exception during loadForUser.
     */
    @Test
    void gatewayThrowsExceptionOnLoad_presenterReceivesFailureMessage() throws Exception {
        int userId = 13;
        double validRadius = 2.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        when(preferenceGateway.loadForUser(userId))
                .thenThrow(new Exception("Database connection failed"));

        interactor.execute(input);

        verify(preferenceGateway).loadForUser(userId);
        verify(preferenceGateway, never()).saveForUser(any());

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Database connection failed", out.getMessage());
    }

    /**
     * Branch 4b: Gateway throws exception during saveForUser.
     */
    @Test
    void gatewayThrowsExceptionOnSave_presenterReceivesFailureMessage() throws Exception {
        int userId = 14;
        double validRadius = 2.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        PreferenceProfile existingProfile = new PreferenceProfile(userId, 1.0);
        when(preferenceGateway.loadForUser(userId)).thenReturn(existingProfile);
        doThrow(new Exception("Save operation failed"))
                .when(preferenceGateway).saveForUser(any(PreferenceProfile.class));

        interactor.execute(input);

        verify(preferenceGateway).loadForUser(userId);
        verify(preferenceGateway).saveForUser(any(PreferenceProfile.class));

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertEquals("Save operation failed", out.getMessage());
    }

    /**
     * Edge case: Exception with null message.
     */
    @Test
    void gatewayThrowsExceptionWithNullMessage_presenterReceivesNullMessage() throws Exception {
        int userId = 15;
        double validRadius = 2.5;
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("restaurants", List.of("italian", "chinese", "mexican"));

        UpdatePreferencesInputData input = new UpdatePreferencesInputData(
                userId, validRadius, categories);

        Exception ex = new Exception((String) null);
        when(preferenceGateway.loadForUser(userId)).thenThrow(ex);

        interactor.execute(input);

        UpdatePreferencesOutputData out = capturePresenterOutput();
        assertFalse(out.isSuccess());
        assertNull(out.getMessage());
    }
}

