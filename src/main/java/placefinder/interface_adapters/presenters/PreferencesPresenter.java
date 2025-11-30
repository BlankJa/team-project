package placefinder.interface_adapters.presenters;

import placefinder.entities.FavoriteLocation;
import placefinder.interface_adapters.viewmodels.PreferencesViewModel;
import placefinder.usecases.preferences.GetPreferencesOutputBoundary;
import placefinder.usecases.preferences.GetPreferencesOutputData;
import placefinder.usecases.preferences.UpdatePreferencesOutputBoundary;
import placefinder.usecases.preferences.UpdatePreferencesOutputData;

public class PreferencesPresenter implements GetPreferencesOutputBoundary,
        UpdatePreferencesOutputBoundary,
        AddFavoriteOutputBoundary,
        DeleteFavoriteOutputBoundary{

    private final PreferencesViewModel viewModel;

public PreferencesPresenter(PreferencesViewModel viewModel) {
    this.viewModel = viewModel;
}

@Override
    public void present(GetPreferencesOutputData outputData) {
        if (outputData.getErrorMessage() != null) {
            viewModel.setErrorMessage(outputData.getErrorMessage());
            return;
        }
        viewModel.setRadiusKm(outputData.getRadiusKm());
        viewModel.setFavorites(outputData.getFavorites());
        viewModel.setSelectedCategories(outputData.getSelectedCategories());
    }

@Override
    public void present(UpdatePreferencesOutputData outputData) {
        if (outputData.isSuccess()) {
            viewModel.setMessage(outputData.getMessage());
            viewModel.setErrorMessage(null);
        } else {
            viewModel.setErrorMessage(outputData.getMessage());
        }
    }

@Override
    public void present(AddFavoriteOutputData outputData) {
        if (outputData.getErrorMessage() != null) {
            viewModel.setErrorMessage(outputData.getErrorMessage());
            return;
        }
        FavoriteLocation fav = outputData.getFavorite();
        if (fav != null) {
            viewModel.addFavorite(fav);
        }
    }

@Override
    public void present(DeleteFavoriteOutputData outputData) {
        if (outputData.isSuccess()) {
            viewModel.setMessage(outputData.getMessage());
            viewModel.setErrorMessage(null);
        } else {
            viewModel.setErrorMessage(outputData.getMessage());
        }
    }
}
