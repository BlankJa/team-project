package placefinder.usecases.buildplan;

import placefinder.entities.*;
import placefinder.usecases.dataacessinterfaces.GeocodingDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.RouteDataAccessInterface;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactor for building plan from place and preferences.
 */
public class BuildPlanInteractor implements BuildPlanInputBoundary {

    private final PreferenceDataAccessInterface preferenceDataAccessInterface;
    private final GeocodingDataAccessInterface geocodingDataAccessInterface;
    private final RouteDataAccessInterface routeDataAccessInterface;
    private final BuildPlanOutputBoundary presenter;

    public BuildPlanInteractor(PreferenceDataAccessInterface preferenceDataAccessInterface,
                               GeocodingDataAccessInterface geocodingDataAccessInterface,
                               RouteDataAccessInterface routeDataAccessInterface,
                               BuildPlanOutputBoundary presenter) {
        this.preferenceDataAccessInterface = preferenceDataAccessInterface;
        this.geocodingDataAccessInterface = geocodingDataAccessInterface;
        this.routeDataAccessInterface = routeDataAccessInterface;
        this.presenter = presenter;
    }

    @Override
    public void execute(BuildPlanInputData inputData) {
        try {
            if (inputData.getSelectedPlaces() == null || inputData.getSelectedPlaces().isEmpty()) {
                presenter.present(new BuildPlanOutputData(null,
                        "Please select at least one place."));
                return;
            }
            GeocodeResult geo = geocodingDataAccessInterface.geocode(inputData.getLocationText());
            if (geo == null) {
                presenter.present(new BuildPlanOutputData(null,
                        "Could not find that location."));
                return;
            }
            PreferenceProfile profile = preferenceDataAccessInterface.loadForUser(inputData.getUserId());
            LocalDate date = LocalDate.parse(inputData.getDate());
            LocalTime start = LocalTime.parse(inputData.getStartTime());

            Route route = routeDataAccessInterface.computeRoute(geo, start, inputData.getSelectedPlaces());
            if (route == null) {
                presenter.present(new BuildPlanOutputData(null,
                        "Could not find route between locations."));
                return;
            }
            Plan plan = new Plan(
                    inputData.getExistingPlanId(),
                    inputData.getUserId(),
                    "", // name set in SavePlan
                    date,
                    start,
                    geo.getFormattedAddress(),
                    route,
                    profile.getRadiusKm(),
                    profile.getSelectedCategories()
            );
            presenter.present(new BuildPlanOutputData(plan,null));
        } catch (Exception e) {
            presenter.present(new BuildPlanOutputData(null, e.getMessage()));
        }
    }
}
