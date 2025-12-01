package placefinder.usecases.preferences;

import placefinder.entities.PreferenceProfile;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;

/**
 * Interactor for retrieving user preferences from the database.
 */
public class GetPreferencesInteractor implements GetPreferencesInputBoundary {

    private final PreferenceDataAccessInterface preferenceDataAccessInterface;
    private final GetPreferencesOutputBoundary presenter;

    public GetPreferencesInteractor(PreferenceDataAccessInterface preferenceDataAccessInterface,
                                    GetPreferencesOutputBoundary presenter) {
        this.preferenceDataAccessInterface = preferenceDataAccessInterface;
        this.presenter = presenter;
    }

    @Override
    public void execute(GetPreferencesInputData inputData) {
        try {
            PreferenceProfile profile = preferenceDataAccessInterface.loadForUser(inputData.getUserId());
            var favorites = preferenceDataAccessInterface.listFavorites(inputData.getUserId());
            presenter.present(new GetPreferencesOutputData(
                    profile.getRadiusKm(),
                    favorites,
                    profile.getSelectedCategories(),
                    null
            ));
        } catch (Exception e) {
            presenter.present(new GetPreferencesOutputData(
                    2.0,
                    java.util.List.of(),
                    null,
                    e.getMessage()
            ));
        }
    }
}

