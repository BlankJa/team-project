package placefinder.interface_adapters.controllers;

import placefinder.entities.Place;
import placefinder.interface_adapters.viewmodels.PlanCreationViewModel;
import placefinder.usecases.buildplan.BuildPlanInputBoundary;
import placefinder.usecases.buildplan.BuildPlanInputData;

import java.util.List;

public class BuildPlanController {

    private final BuildPlanInputBoundary buildPlanInteractor;
    private final PlanCreationViewModel viewModel;

    public BuildPlanController(BuildPlanInputBoundary buildPlanInteractor,
                               PlanCreationViewModel viewModel) {
        this.buildPlanInteractor = buildPlanInteractor;
        this.viewModel = viewModel;
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
                userId,
                locationText,
                date,
                startTime,
                selectedPlaces,
                existingPlanId
        ));
    }
}
