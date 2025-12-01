package placefinder.usecases.buildplan;

import placefinder.entities.*;
import placefinder.usecases.dataacessinterfaces.GeocodingDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;

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
    private final BuildPlanOutputBoundary presenter;

    public BuildPlanInteractor(PreferenceDataAccessInterface preferenceDataAccessInterface,
                               GeocodingDataAccessInterface geocodingDataAccessInterface,
                               BuildPlanOutputBoundary presenter) {
        this.preferenceDataAccessInterface = preferenceDataAccessInterface;
        this.geocodingDataAccessInterface = geocodingDataAccessInterface;
        this.presenter = presenter;
    }

    @Override
    public void execute(BuildPlanInputData inputData) {
        try {
            if (inputData.getSelectedPlaces() == null || inputData.getSelectedPlaces().isEmpty()) {
                presenter.present(new BuildPlanOutputData(null, false,
                        "Please select at least one place."));
                return;
            }
            GeocodeResult geo = geocodingDataAccessInterface.geocode(inputData.getLocationText());
            if (geo == null) {
                presenter.present(new BuildPlanOutputData(null, false,
                        "Could not find that location."));
                return;
            }
            PreferenceProfile profile = preferenceDataAccessInterface.loadForUser(inputData.getUserId());
            LocalDate date = LocalDate.parse(inputData.getDate());
            LocalTime start = LocalTime.parse(inputData.getStartTime());
            LocalTime current = start;
            LocalTime dayEnd = LocalTime.of(23, 59);

            List<PlanStop> stops = new ArrayList<>();
            int seq = 1;
            boolean truncated = false;
            for (Place p : inputData.getSelectedPlaces()) {
                LocalTime end = current.plusHours(1);
                if (end.isAfter(dayEnd)) {
                    truncated = true;
                    break;
                }
                stops.add(new PlanStop(seq, p, current, end));
                seq++;
                current = end;
            }

            RouteOld route = new RouteOld(stops);
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
            presenter.present(new BuildPlanOutputData(plan, truncated, null));
        } catch (Exception e) {
            presenter.present(new BuildPlanOutputData(null, false, e.getMessage()));
        }
    }
}
