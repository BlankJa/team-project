package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.PreferencesViewModel;
import placefinder.usecases.preferences.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for managing user preferences, handling get and update operations.
 */
public class PreferencesController implements
        GetPreferencesOutputBoundary,
        UpdatePreferencesOutputBoundary {

    private final GetPreferencesInputBoundary getPreferencesInteractor;
    private final UpdatePreferencesInputBoundary updatePreferencesInteractor;

    private final PreferencesViewModel viewModel;

    public PreferencesController(GetPreferencesInputBoundary getPreferencesInteractor,
                                 UpdatePreferencesInputBoundary updatePreferencesInteractor,
                                 PreferencesViewModel viewModel) {
        this.getPreferencesInteractor = getPreferencesInteractor;
        this.updatePreferencesInteractor = updatePreferencesInteractor;
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

    public PreferencesViewModel getViewModel() { return viewModel; }
}

