package placefinder.interface_adapters.presenters;

import placefinder.entities.Place;
import placefinder.interface_adapters.viewmodels.PlanCreationViewModel;
import placefinder.usecases.buildplan.BuildPlanOutputBoundary;
import placefinder.usecases.buildplan.BuildPlanOutputData;
import placefinder.usecases.saveplan.SavePlanOutputBoundary;
import placefinder.usecases.saveplan.SavePlanOutputData;
import placefinder.usecases.searchplaces.SearchPlacesOutputBoundary;
import placefinder.usecases.searchplaces.SearchPlacesOutputData;

import java.util.List;

public class PlanCreationPresenter implements SearchPlacesOutputBoundary,
        BuildPlanOutputBoundary,
        SavePlanOutputBoundary{

    private final PlanCreationViewModel viewModel;

public PlanCreationPresenter(PlanCreationViewModel viewModel) {
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

        // Build a short advice string based on weather + indoor/outdoor balance
        String adviceText;

        if (!outputData.isWeatherUsed()) {
            // Weather API failed or not used
            adviceText = "Weather data unavailable. Results are not weather-optimized.";
            viewModel.setInfoMessage(adviceText);
        } else {
            // Weather was used; infer bias from recommended places
            int indoor = 0;
            int outdoor = 0;

            for (Place p : outputData.getPlaces()) {
                if (p.getIndoorOutdoorType() == null) continue;
                switch (p.getIndoorOutdoorType()) {
                    case INDOOR -> indoor++;
                    case OUTDOOR -> outdoor++;
                    default -> { /* MIXED or others – ignore for bias */ }
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

@Override
    public void present(BuildPlanOutputData outputData) {
        if (outputData.getErrorMessage() != null) {
            viewModel.setPlanPreview(null);
            viewModel.setPlanTruncated(false);
            viewModel.setErrorMessage(outputData.getErrorMessage());
            return;
        }
        viewModel.setPlanPreview(outputData.getPlan());
        viewModel.setPlanTruncated(outputData.isTruncated());
        if (outputData.isTruncated()) {
            viewModel.setInfoMessage("Plan exceeds available time; some places were not included.");
        } else {
            viewModel.setInfoMessage(null);
        }
    }

@Override
    public void present(SavePlanOutputData outputData) {
        if (outputData.isSuccess()) {
            viewModel.setLastSavedPlan(outputData.getPlan());
            viewModel.setInfoMessage(outputData.getMessage());
            viewModel.setErrorMessage(null);
        } else {
            viewModel.setErrorMessage(outputData.getMessage());
        }
    }
}
