package placefinder.usecases.favouritelocation;

import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;

public class DeleteFavoriteInteractor implements DeleteFavoriteInputBoundary {

    private final PreferenceDataAccessInterface preferenceDataAccessInterface;
    private final DeleteFavoriteOutputBoundary presenter;

    public DeleteFavoriteInteractor(PreferenceDataAccessInterface preferenceDataAccessInterface,
                                    DeleteFavoriteOutputBoundary presenter) {
        this.preferenceDataAccessInterface = preferenceDataAccessInterface;
        this.presenter = presenter;
    }

    @Override
    public void execute(DeleteFavoriteInputData inputData) {
        try {
            preferenceDataAccessInterface.deleteFavorite(inputData.getFavoriteId(), inputData.getUserId());
            presenter.present(new DeleteFavoriteOutputData(true, "Favorite deleted."));
        } catch (Exception e) {
            presenter.present(new DeleteFavoriteOutputData(false, e.getMessage()));
        }
    }
}
