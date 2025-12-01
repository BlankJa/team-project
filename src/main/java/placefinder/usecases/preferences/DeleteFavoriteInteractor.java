package placefinder.usecases.preferences;

import placefinder.usecases.ports.PreferenceGateway;

public class DeleteFavoriteInteractor implements DeleteFavoriteInputBoundary {

    private final PreferenceGateway preferenceGateway;
    private final DeleteFavoriteOutputBoundary presenter;

    public DeleteFavoriteInteractor(PreferenceGateway preferenceGateway,
                                    DeleteFavoriteOutputBoundary presenter) {
        this.preferenceGateway = preferenceGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(DeleteFavoriteInputData inputData) {
        try {
            preferenceGateway.deleteFavorite(inputData.getFavoriteId(), inputData.getUserId());
            presenter.present(new DeleteFavoriteOutputData(true, "Favorite deleted."));
        } catch (Exception e) {
            presenter.present(new DeleteFavoriteOutputData(false, e.getMessage()));
        }
    }
}
