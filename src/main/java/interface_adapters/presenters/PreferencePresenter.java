package interface_adapters.presenters;

import entities.PreferenceProfile;
import interface_adapters.views.PreferenceView;
import usecases.common.Result;
import usecases.update_user_preferences.UpdateUserPreferencesUseCase;

public class PreferencePresenter {
    private final PreferenceView view;
    private final UpdateUserPreferencesUseCase updatePreferencesUseCase;

    public PreferencePresenter(PreferenceView view, UpdateUserPreferencesUseCase updatePreferencesUseCase) {
        this.view = view;
        this.updatePreferencesUseCase = updatePreferencesUseCase;
    }

    public void onSavePreferences(String email, PreferenceProfile profile) {
        view.showLoading();

        new Thread(() -> {
            try {
                Result<Boolean> result = updatePreferencesUseCase.execute(email, profile);

                javax.swing.SwingUtilities.invokeLater(() -> {
                    view.hideLoading();

                    if (result.isSuccess() && result.getData()) {
                        view.updateUserProfile(profile);
                        view.showSuccess("Preferences saved successfully!");
                    } else {
                        view.showError(result.getError());
                    }
                });
            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    view.hideLoading();
                    view.showError("Failed to save preferences: " + e.getMessage());
                });
            }
        }).start();
    }
}