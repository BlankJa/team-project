package placefinder.interface_adapters.controllers;

import placefinder.entities.Place;

import java.util.List;

/**
 * Facade controller used by the PlanBuilderPanel.
 * Internally delegates to:
 *  - SearchPlacesController
 *  - BuildPlanController
 *  - SavePlanController
 *
 * Each of those is a single-responsibility controller for its use case.
 */
public class PlanCreationController {

    private final SearchPlacesController searchPlacesController;
    private final BuildPlanController buildPlanController;
    private final SavePlanController savePlanController;

    public PlanCreationController(SearchPlacesController searchPlacesController,
                                  BuildPlanController buildPlanController,
                                  SavePlanController savePlanController) {
        this.searchPlacesController = searchPlacesController;
        this.buildPlanController = buildPlanController;
        this.savePlanController = savePlanController;
    }

    public void searchPlaces(int userId, String locationText, String date) {
        searchPlacesController.searchPlaces(userId, locationText, date);
    }

    public void buildPlan(int userId,
                          String locationText,
                          String date,
                          String startTime,
                          List<Place> selectedPlaces,
                          Integer existingPlanId) {
        buildPlanController.buildPlan(
                userId,
                locationText,
                date,
                startTime,
                selectedPlaces,
                existingPlanId
        );
    }

    public void saveCurrentPlan(String name) {
        savePlanController.saveCurrentPlan(name);
    }
}
