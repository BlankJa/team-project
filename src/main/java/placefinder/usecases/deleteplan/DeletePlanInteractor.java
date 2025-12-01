package placefinder.usecases.deleteplan;

import placefinder.usecases.dataacessinterfaces.PlanDataAccessInterface;

/**
 * Interactor responsible for deleting an existing plan.
 *
 * Coordinates between the plan data source (PlanGateway) and the output presenter.
 * Implements application logic only — does not perform UI work or persistence itself.
 */
public class DeletePlanInteractor implements DeletePlanInputBoundary {

    /** Gateway interface for deleting plans in the data layer */
    private final PlanDataAccessInterface planDataAccessInterface;

    /** Output boundary responsible for formatting result data for presentation */
    private final DeletePlanOutputBoundary presenter;

    /**
     * Constructs a DeletePlanInteractor with the required data gateway and output presenter.
     *
     * @param planDataAccessInterface  abstraction for deleting plans from storage
     * @param presenter    presenter that receives the result of the operation
     */
    public DeletePlanInteractor(PlanDataAccessInterface planDataAccessInterface,
                                DeletePlanOutputBoundary presenter) {
        this.planDataAccessInterface = planDataAccessInterface;
        this.presenter = presenter;
    }

    /**
     * Executes the delete use case.
     *
     * Flow:
     * 1. Calls the gateway to delete a plan for the given user.
     * 2. On success → presenter receives success result.
     * 3. On failure → presenter receives failure and error message.
     *
     * No UI or database logic appears here—this class handles business rules only.
     */
    @Override
    public void execute(DeletePlanInputData inputData) {
        try {
            planDataAccessInterface.deletePlan(inputData.getPlanId(), inputData.getUserId());
            presenter.present(new DeletePlanOutputData(true, "Plan deleted."));
        } catch (Exception e) {
            presenter.present(new DeletePlanOutputData(false, e.getMessage()));
        }
    }
}
