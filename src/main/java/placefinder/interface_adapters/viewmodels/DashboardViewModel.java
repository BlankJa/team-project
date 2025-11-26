package placefinder.interface_adapters.viewmodels;

import placefinder.entities.Plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardViewModel {
    private List<Plan> plans = new ArrayList<>();
    private String message;
    private String errorMessage;

    public List<Plan> getPlans() {
        return Collections.unmodifiableList(plans);
    }

    public void setPlans(List<Plan> plans) {
        this.plans = plans != null ? new ArrayList<>(plans) : new ArrayList<>();
    }

    public void removePlanById(int id) {
        this.plans.removeIf(p -> p.getId() != null && p.getId() == id);
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
