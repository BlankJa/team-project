package placefinder.usecases.getroutedetails;

import placefinder.entities.Leg;
import placefinder.entities.Plan;
import placefinder.entities.Route;
import placefinder.frameworks_drivers.dataaccess.SqlitePlanDataAccess;
import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;

import java.util.List;

/**
 * Interactor for retrieving route details (legs and steps) for a specific plan.
 *
 * <p>The interactor coordinates the data access layer to fetch a plan and
 * extract its route.  It then passes the legs to the output boundary for
 * presentation.  If the plan does not exist or has no route, an
 * appropriate error message is returned.  This interactor does not
 * perform any UI logic or persistence beyond reading the plan.</p>
 */
public class GetRouteDetailsInteractor implements GetRouteDetailsInputBoundary {

    private final PlanDataAccessInterface planDAO;
    private final GetRouteDetailsOutputBoundary presenter;

    /**
     * Create a new interactor with the required dependencies.
     *
     * @param planDAO   gateway to load plans from persistence
     * @param presenter the output boundary to receive results
     */
    public GetRouteDetailsInteractor(PlanDataAccessInterface planDAO,
                                     GetRouteDetailsOutputBoundary presenter) {
        this.planDAO = planDAO;
        this.presenter = presenter;
    }

    @Override
    public void execute(GetRouteDetailsInputData inputData) {
        try {
            // 1. Retrieve the plan with its stops (and route if persisted)
            Plan plan = planDAO.findPlanWithStops(inputData.getPlanId());
            if (plan == null) {
                presenter.present(new GetRouteDetailsOutputData(null, "Plan not found."));
                return;
            }

            Route route = plan.getRoute();
            // 2. If there is no route or it contains no legs, report the absence
            if (route == null || route.getLegs() == null || route.getLegs().isEmpty()) {
                presenter.present(new GetRouteDetailsOutputData(null, "No route details available for this plan."));
                return;
            }

            // 3. Provide the legs to the presenter
            List<Leg> legs = route.getLegs();
            presenter.present(new GetRouteDetailsOutputData(legs, null));
        } catch (Exception e) {
            presenter.present(new GetRouteDetailsOutputData(null, e.getMessage()));
        }
    }
}