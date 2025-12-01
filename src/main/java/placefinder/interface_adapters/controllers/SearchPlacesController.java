package placefinder.interface_adapters.controllers;

import placefinder.interface_adapters.viewmodels.PlanCreationViewModel;
import placefinder.usecases.searchplaces.SearchPlacesInputBoundary;
import placefinder.usecases.searchplaces.SearchPlacesInputData;

public class SearchPlacesController {

    private final SearchPlacesInputBoundary searchPlacesInteractor;
    private final PlanCreationViewModel viewModel;

    public SearchPlacesController(SearchPlacesInputBoundary searchPlacesInteractor,
                                  PlanCreationViewModel viewModel) {
        this.searchPlacesInteractor = searchPlacesInteractor;
        this.viewModel = viewModel;
    }

    public void searchPlaces(int userId, String locationText, String date) {
        viewModel.setErrorMessage(null);
        viewModel.setInfoMessage(null);
        viewModel.setLoading(true);

        searchPlacesInteractor.execute(new SearchPlacesInputData(userId, locationText, date));

        viewModel.setLoading(false);
    }
}
