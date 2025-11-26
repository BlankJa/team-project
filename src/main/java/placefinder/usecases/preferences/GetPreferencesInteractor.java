package placefinder.usecases.preferences;

import placefinder.entities.PreferenceProfile;
import placefinder.usecases.ports.PreferenceGateway;

/**
 * Interactor for retrieving user preferences from the database.
 */
public class GetPreferencesInteractor implements GetPreferencesInputBoundary {

    private final PreferenceGateway preferenceGateway;
    private final GetPreferencesOutputBoundary presenter;

    public GetPreferencesInteractor(PreferenceGateway preferenceGateway,
                                    GetPreferencesOutputBoundary presenter) {
        this.preferenceGateway = preferenceGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(GetPreferencesInputData inputData) {
        try {
            PreferenceProfile profile = preferenceGateway.loadForUser(inputData.getUserId());
            var favorites = preferenceGateway.listFavorites(inputData.getUserId());
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

