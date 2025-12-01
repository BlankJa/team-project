package placefinder.usecases.listplans;

import placefinder.entities.Plan;
import placefinder.usecases.ports.PlanGateway;
import java.util.List;

/**
 * Interactor for retrieving all plans owned by a specific user.
 *
 * Responsibilities:
 * - Calls the PlanGateway to load plans from the data source.
 * - Passes results to the output presenter for UI formatting.
 * - Contains application logic only (no UI, DB, or framework code).
 */
public class ListPlansInteractor implements ListPlansInputBoundary {

    /** Provides access to stored plans (database or external source) */
    private final PlanGateway planGateway;

    /** Output boundary that receives the formatted result */
    private final ListPlansOutputBoundary presenter;

    /**
     * Constructs a ListPlansInteractor with dependency-injected gateway and presenter.
     *
     * @param planGateway  abstraction for retrieving plans belonging to a user
     * @param presenter    handles output formatting for the view layer
     */
    public ListPlansInteractor(PlanGateway planGateway,
                               ListPlansOutputBoundary presenter) {
        this.planGateway = planGateway;
        this.presenter = presenter;
    }

    /**
     * Executes the use case to fetch all plans for the given user ID.
     *
     * Flow:
     * 1. Request user plans from the PlanGateway.
     * 2. If successful → presenter receives list of plans + no error message.
     * 3. If failure → presenter receives empty list + error message.
     *
     * No UI logic and no persistence details are handled here.
     */
    @Override
    public void execute(ListPlansInputData inputData) {
        try {
            List<Plan> plans = planGateway.findPlansByUser(inputData.getUserId());
            presenter.present(new ListPlansOutputData(plans, null));
        } catch (Exception e) {
            presenter.present(new ListPlansOutputData(List.of(), e.getMessage()));
        }
    }
}