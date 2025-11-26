package placefinder.usecases.preferences;

import placefinder.entities.PreferenceProfile;
import placefinder.usecases.ports.PreferenceGateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interactor for updating user preferences with validation.
 */
public class UpdatePreferencesInteractor implements UpdatePreferencesInputBoundary {

    private final PreferenceGateway preferenceGateway;
    private final UpdatePreferencesOutputBoundary presenter;

    public UpdatePreferencesInteractor(PreferenceGateway preferenceGateway,
                                       UpdatePreferencesOutputBoundary presenter) {
        this.preferenceGateway = preferenceGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(UpdatePreferencesInputData inputData) {
        try {
            if (inputData.getRadiusKm() < 0 || inputData.getRadiusKm() > 5) {
                presenter.present(new UpdatePreferencesOutputData(false,
                        "Radius must be between 0 and 5 km."));
                return;
            }
            
            Map<String, List<String>> selectedCategories = inputData.getSelectedCategories();
            int totalSubCategories = 0;
            if (selectedCategories != null) {
                for (List<String> subCategories : selectedCategories.values()) {
                    if (subCategories != null) {
                        totalSubCategories += subCategories.size();
                    }
                }
            }
            
            if (totalSubCategories < 3) {
                presenter.present(new UpdatePreferencesOutputData(false,
                        "Please select at least 3 sub-categories."));
                return;
            }
            
            PreferenceProfile profile = preferenceGateway.loadForUser(inputData.getUserId());
            profile.setRadiusKm(inputData.getRadiusKm());
            if (selectedCategories != null) {
                profile.setSelectedCategories(new HashMap<>(selectedCategories));
            } else {
                profile.setSelectedCategories(new HashMap<>());
            }
            preferenceGateway.saveForUser(profile);
            presenter.present(new UpdatePreferencesOutputData(true, "Preferences saved."));
        } catch (Exception e) {
            presenter.present(new UpdatePreferencesOutputData(false, e.getMessage()));
        }
    }
}

