package placefinder.usecases.getplandetails;

import placefinder.entities.Plan;
import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;

/**
 * Interactor responsible for retrieving detailed information about a specific plan.
 * Behaviour:
 * - Requests a full plan (including stops) from the data gateway.
 * - Passes the result to the output boundary for presentation.
 * - Handles both success and failure cases without UI or database logic.
 */
public class GetPlanDetailsInteractor implements GetPlanDetailsInputBoundary {

    /** Gateway abstraction used to load plans from the data layer. */
    private final PlanDataAccessInterface planDataAccessInterface;

    /** Output presenter that receives the formatted result. */
    private final GetPlanDetailsOutputBoundary presenter;

    /**
     * Constructs the interactor with the required dependencies.
     *
     * @param planDataAccessInterface  data access interface for fetching plans
     * @param presenter    receives formatted output for the view layer
     */
    public GetPlanDetailsInteractor(PlanDataAccessInterface planDataAccessInterface,
                                    GetPlanDetailsOutputBoundary presenter) {
        this.planDataAccessInterface = planDataAccessInterface;
        this.presenter = presenter;
    }

    /**
     * Executes the use case to load a plan with all stops.
     * Flow:
     * 1. Request the full plan from the gateway using its ID.
     * 2. If no plan exists → presenter receives null + an error message.
     * 3. If found → presenter receives the plan + no error message.
     * 4. Any exception results in a failure message passed to presenter.
     */
    @Override
    public void execute(GetPlanDetailsInputData inputData) {
        try {
            final Plan plan = planDataAccessInterface.findPlanWithStops(inputData.getPlanId());

            if (plan == null) {
                presenter.present(new GetPlanDetailsOutputData(null, "Plan not found."));
            } else {
                presenter.present(new GetPlanDetailsOutputData(plan, null));
            }

        } catch (Exception e) {
            presenter.present(new GetPlanDetailsOutputData(null, e.getMessage()));
        }
    }
}
