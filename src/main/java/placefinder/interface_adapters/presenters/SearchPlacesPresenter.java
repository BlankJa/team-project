package placefinder.interface_adapters.presenters;

import placefinder.entities.Place;
import placefinder.interface_adapters.viewmodels.PlanCreationViewModel;
import placefinder.usecases.searchplaces.SearchPlacesOutputBoundary;
import placefinder.usecases.searchplaces.SearchPlacesOutputData;

import java.util.List;

public class SearchPlacesPresenter implements SearchPlacesOutputBoundary {

    private final PlanCreationViewModel viewModel;

    public SearchPlacesPresenter(PlanCreationViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void present(SearchPlacesOutputData outputData) {
        if (outputData.getErrorMessage() != null) {
            viewModel.setRecommendedPlaces(List.of());
            viewModel.setOriginAddress(null);
            viewModel.setWeatherUsed(false);
            viewModel.setWeatherAdvice(null);
            viewModel.setErrorMessage(outputData.getErrorMessage());
            viewModel.setInfoMessage(null);
            return;
        }

        // Basic data
        viewModel.setRecommendedPlaces(outputData.getPlaces());
        viewModel.setOriginAddress(outputData.getOriginAddress());
        viewModel.setWeatherUsed(outputData.isWeatherUsed());
        viewModel.setErrorMessage(null);

        // Short advice based on weather + indoor/outdoor balance
        String adviceText;

        if (!outputData.isWeatherUsed()) {
            adviceText = "Weather data unavailable. Results are not weather-optimized.";
            viewModel.setInfoMessage(adviceText);
        } else {
            int indoor = 0;
            int outdoor = 0;

            for (Place p : outputData.getPlaces()) {
                if (p.getIndoorOutdoorType() == null) continue;
                switch (p.getIndoorOutdoorType()) {
                    case INDOOR -> indoor++;
                    case OUTDOOR -> outdoor++;
                    default -> {
                        // MIXED/OTHER – ignore for bias
                    }
                }
            }

            String bias;
            if (indoor > outdoor) {
                bias = "We are favouring indoor locations based on the forecast.";
            } else if (outdoor > indoor) {
                bias = "We are favouring outdoor locations based on the forecast.";
            } else {
                bias = "Mix of indoor and outdoor locations based on the forecast.";
            }

            adviceText = bias + " For detailed temperature and UV advice, use the Weather Advice page on the dashboard.";
            viewModel.setInfoMessage(null);
        }

        viewModel.setWeatherAdvice(adviceText);
    }
}
