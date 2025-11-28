package placefinder.interface_adapters.controllers;

import placefinder.entities.Place;
import placefinder.interface_adapters.viewmodels.PlanCreationViewModel;
import placefinder.usecases.buildplan.*;
import placefinder.usecases.saveplan.*;
import placefinder.usecases.searchplaces.*;

import java.util.List;

/**
 * Controller for managing plan creation.
 */
public class PlanCreationController implements
        SearchPlacesOutputBoundary,
        BuildPlanOutputBoundary,
        SavePlanOutputBoundary {

    private final SearchPlacesInputBoundary searchPlacesInteractor;
    private final BuildPlanInputBoundary buildPlanInteractor;
    private final SavePlanInputBoundary savePlanInteractor;
    private final PlanCreationViewModel viewModel;

    public PlanCreationController(SearchPlacesInputBoundary searchPlacesInteractor,
                                  BuildPlanInputBoundary buildPlanInteractor,
                                  SavePlanInputBoundary savePlanInteractor,
                                  PlanCreationViewModel viewModel) {
        this.searchPlacesInteractor = searchPlacesInteractor;
        this.buildPlanInteractor = buildPlanInteractor;
        this.savePlanInteractor = savePlanInteractor;
        this.viewModel = viewModel;
    }

    public void searchPlaces(int userId, String locationText, String date) {
        viewModel.setErrorMessage(null);
        viewModel.setInfoMessage(null);
        searchPlacesInteractor.execute(new SearchPlacesInputData(userId, locationText, date));
    }

    public void buildPlan(int userId,
                          String locationText,
                          String date,
                          String startTime,
                          List<Place> selectedPlaces,
                          Integer existingPlanId) {
        viewModel.setErrorMessage(null);
        viewModel.setInfoMessage(null);
        buildPlanInteractor.execute(new BuildPlanInputData(
                userId, locationText, date, startTime, selectedPlaces, existingPlanId
        ));
    }

    public void saveCurrentPlan(String name) {
        viewModel.setErrorMessage(null);
        viewModel.setInfoMessage(null);
        if (viewModel.getPlanPreview() == null) {
            viewModel.setErrorMessage("No plan to save. Please generate a plan first.");
            return;
        }
        savePlanInteractor.execute(new SavePlanInputData(viewModel.getPlanPreview(), name));
    }

    @Override
    public void present(SearchPlacesOutputData outputData) {
        if (outputData.getErrorMessage() != null) {
            viewModel.setRecommendedPlaces(List.of());
            viewModel.setOriginAddress(null);
            viewModel.setWeatherUsed(false);
            viewModel.setErrorMessage(outputData.getErrorMessage());
            return;
        }
        viewModel.setRecommendedPlaces(outputData.getPlaces());
        viewModel.setOriginAddress(outputData.getOriginAddress());
        viewModel.setWeatherUsed(outputData.isWeatherUsed());
        viewModel.setErrorMessage(null);
        if (!outputData.isWeatherUsed()) {
            viewModel.setInfoMessage("Weather data unavailable. Results are not weather-optimized.");
        } else {
            viewModel.setInfoMessage(null);
        }
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

    public PlanCreationViewModel getViewModel() { return viewModel; }
}
