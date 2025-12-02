package placefinder.usecases.plans;

import org.junit.jupiter.api.Test;
import placefinder.entities.Plan;
import placefinder.entities.PreferenceProfile;
import placefinder.entities.FavoriteLocation;
import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ApplyPreferencesFromPlanInteractor}.
 */
class ApplyPreferencesFromPlanInteractorTest {

    /**
     * When the plan is not found, preferences are not touched
     * and the presenter receives a failure result.
     */
    @Test
    void execute_failsWhenPlanNotFound() {
        RecordingPlanGateway planGateway =
                new RecordingPlanGateway(null, false, null);
        RecordingPreferenceGateway preferenceGateway =
                new RecordingPreferenceGateway();
        CapturingApplyPreferencesFromPlanPresenter presenter =
                new CapturingApplyPreferencesFromPlanPresenter();

        ApplyPreferencesFromPlanInteractor interactor =
                new ApplyPreferencesFromPlanInteractor(
                        planGateway, preferenceGateway, presenter);

        ApplyPreferencesFromPlanInputData input =
                new ApplyPreferencesFromPlanInputData(5, 99);

        interactor.execute(input);

        assertEquals(1, planGateway.getFindCallCount());
        assertEquals(0, preferenceGateway.getLoadCallCount());
        assertEquals(0, preferenceGateway.getSaveCallCount());

        ApplyPreferencesFromPlanOutputData out = presenter.getLastOutput();
        assertNotNull(out);
        assertFalse(out.isSuccess());
        assertEquals("Plan not found.", out.getMessage());
    }

    /**
     * When the plan exists, the snapshot prefs are applied to the loaded profile.
     */
    @Test
    void execute_appliesPreferencesSuccessfully() throws Exception {
        // Build a plan with snapshot preferences.
        Map<String, List<String>> snapshotCategories = new HashMap<>();
        snapshotCategories.put("museum", List.of("art"));
        double snapshotRadius = 9.5;

        Plan plan = new Plan(
                10, 1, "Trip",
                LocalDate.now(),
                LocalTime.NOON,
                "Toronto",
                null,
                snapshotRadius,
                snapshotCategories
        );

        RecordingPlanGateway planGateway =
                new RecordingPlanGateway(plan, true, null);
        RecordingPreferenceGateway preferenceGateway =
                new RecordingPreferenceGateway();
        CapturingApplyPreferencesFromPlanPresenter presenter =
                new CapturingApplyPreferencesFromPlanPresenter();

        ApplyPreferencesFromPlanInteractor interactor =
                new ApplyPreferencesFromPlanInteractor(
                        planGateway, preferenceGateway, presenter);

        int userId = 7;
        int planId = 10;

        ApplyPreferencesFromPlanInputData input =
                new ApplyPreferencesFromPlanInputData(userId, planId);

        interactor.execute(input);

        assertEquals(1, planGateway.getFindCallCount());
        assertEquals(1, preferenceGateway.getLoadCallCount());
        assertEquals(1, preferenceGateway.getSaveCallCount());

        PreferenceProfile saved = preferenceGateway.getSavedProfile();
        assertEquals(snapshotRadius, saved.getRadiusKm());
        assertEquals(snapshotCategories, saved.getSelectedCategories()); // maps copy correctly

        ApplyPreferencesFromPlanOutputData out = presenter.getLastOutput();
        assertNotNull(out);
        assertTrue(out.isSuccess());
        assertEquals("Preferences updated from plan.", out.getMessage());
    }

    /**
     * If the plan gateway throws an exception, the interactor presents failure.
     */
    @Test
    void execute_reportsFailureWhenGatewayThrows() {
        String msg = "Simulated failure";
        RecordingPlanGateway planGateway =
                new RecordingPlanGateway(null, true, msg);
        RecordingPreferenceGateway prefGateway =
                new RecordingPreferenceGateway();
        CapturingApplyPreferencesFromPlanPresenter presenter =
                new CapturingApplyPreferencesFromPlanPresenter();

        ApplyPreferencesFromPlanInteractor interactor =
                new ApplyPreferencesFromPlanInteractor(
                        planGateway, prefGateway, presenter);

        ApplyPreferencesFromPlanInputData input =
                new ApplyPreferencesFromPlanInputData(1, 222);

        interactor.execute(input);

        ApplyPreferencesFromPlanOutputData out = presenter.getLastOutput();
        assertNotNull(out);
        assertFalse(out.isSuccess());
        assertEquals(msg, out.getMessage());
    }

    // ============================================================
    //  Test Doubles
    // ============================================================

    /** In-memory fake Plan gateway. */
    private static class RecordingPlanGateway implements PlanDataAccessInterface {

        private final Plan plan;
        private final boolean shouldFind;
        private final String exceptionMessage;

        private int findCallCount;
        private int lastPlanId;

        RecordingPlanGateway(Plan plan, boolean shouldFind, String exceptionMessage) {
            this.plan = plan;
            this.shouldFind = shouldFind;
            this.exceptionMessage = exceptionMessage;
        }

        @Override
        public Plan findPlanWithStops(int planId) {
            findCallCount++;
            lastPlanId = planId;
            if (exceptionMessage != null) throw new RuntimeException(exceptionMessage);
            return shouldFind ? plan : null;
        }

        @Override public void savePlan(Plan plan) {}
        @Override public List<Plan> findPlansByUser(int userId) { return List.of(); }
        @Override public void deletePlan(int planId, int userId) {}

        int getFindCallCount() { return findCallCount; }
        int getLastPlanId() { return lastPlanId; }
    }

    /** Fake Preference gateway using real PreferenceProfile objects. */
    private static class RecordingPreferenceGateway implements PreferenceDataAccessInterface {

        private int loadCallCount;
        private int saveCallCount;
        private int lastLoadedUserId;

        private PreferenceProfile savedProfile;
        private PreferenceProfile loadedProfile;

        @Override
        public PreferenceProfile loadForUser(int userId) {
            loadCallCount++;
            lastLoadedUserId = userId;

            // Build a real PreferenceProfile like your actual constructor requires
            loadedProfile = new PreferenceProfile(userId, 5.0);
            return loadedProfile;
        }

        @Override
        public void saveForUser(PreferenceProfile profile) {
            saveCallCount++;
            this.savedProfile = profile;
        }

        @Override public List<FavoriteLocation> listFavorites(int userId) { return List.of(); }
        @Override public FavoriteLocation addFavorite(int userId, String n, String a, double la, double lo) { return null; }
        @Override public void deleteFavorite(int favoriteId, int userId) {}

        int getLoadCallCount() { return loadCallCount; }
        int getSaveCallCount() { return saveCallCount; }
        int getLastLoadedUserId() { return lastLoadedUserId; }
        PreferenceProfile getSavedProfile() { return savedProfile; }
        PreferenceProfile getLoadedProfile() { return loadedProfile; }
    }

    /** Presenter capturing the last output. */
    private static class CapturingApplyPreferencesFromPlanPresenter
            implements ApplyPreferencesFromPlanOutputBoundary {

        private ApplyPreferencesFromPlanOutputData last;

        @Override
        public void present(ApplyPreferencesFromPlanOutputData data) {
            this.last = data;
        }

        ApplyPreferencesFromPlanOutputData getLastOutput() {
            return last;
        }
    }
}
