package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.PreferencesViewModel;
import placefinder.usecases.favouritelocation.AddFavoriteInputBoundary;
import placefinder.usecases.favouritelocation.AddFavoriteInputData;
import placefinder.usecases.favouritelocation.DeleteFavoriteInputBoundary;
import placefinder.usecases.favouritelocation.DeleteFavoriteInputData;
import placefinder.usecases.preferences.*;

import java.util.List;
import java.util.Map;

public class PreferencesController {

    private final GetPreferencesInputBoundary getPreferencesInteractor;
    private final UpdatePreferencesInputBoundary updatePreferencesInteractor;
    private final AddFavoriteInputBoundary addFavoriteInteractor;
    private final DeleteFavoriteInputBoundary deleteFavoriteInteractor;

    private final PreferencesViewModel viewModel;

    public PreferencesController(GetPreferencesInputBoundary getPreferencesInteractor,
                                 UpdatePreferencesInputBoundary updatePreferencesInteractor,
                                 AddFavoriteInputBoundary addFavoriteInteractor,
                                 DeleteFavoriteInputBoundary deleteFavoriteInteractor,
                                 PreferencesViewModel viewModel) {
        this.getPreferencesInteractor = getPreferencesInteractor;
        this.updatePreferencesInteractor = updatePreferencesInteractor;
        this.addFavoriteInteractor = addFavoriteInteractor;
        this.deleteFavoriteInteractor = deleteFavoriteInteractor;
        this.viewModel = viewModel;
    }

    public void loadPreferences(int userId) {
        viewModel.setErrorMessage(null);
        viewModel.setMessage(null);
        getPreferencesInteractor.execute(new GetPreferencesInputData(userId));
    }

    public void savePreferences(int userId, double radiusKm,
                                Map<String, List<String>> selectedCategories) {
        viewModel.setErrorMessage(null);
        viewModel.setMessage(null);
        updatePreferencesInteractor.execute(new UpdatePreferencesInputData(userId, radiusKm, selectedCategories));
    }

    public void addFavorite(int userId, String name, String address) {
        viewModel.setErrorMessage(null);
        addFavoriteInteractor.execute(new AddFavoriteInputData(userId, name, address));
    }

    public void deleteFavorite(int userId, int favoriteId) {
        viewModel.setErrorMessage(null);
        deleteFavoriteInteractor.execute(new DeleteFavoriteInputData(userId, favoriteId));
    }









    public PreferencesViewModel getViewModel() { return viewModel; }
}
