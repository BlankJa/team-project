package placefinder.usecases.dataacessinterfaces;

import placefinder.entities.Plan;
import java.util.List;

/**
 * Gateway interface for accessing user's plans.
 */
public interface PlanDataAccessInterface {
    void savePlan(Plan plan) throws Exception;
    List<Plan> findPlansByUser(int userId) throws Exception;
    Plan findPlanWithStops(int planId) throws Exception;
    void deletePlan(int planId, int userId) throws Exception;
}
