package placefinder.interface_adapters.viewmodels;

import placefinder.entities.Leg;
import placefinder.entities.Step;
import placefinder.entities.PlanStop;

import java.util.ArrayList;
import java.util.List;

/**
 * View model for managing navigation instructions displayed in the plan details view.
 * <p>
 * Each direction consists of a leg connecting two places.  A leg has a start place
 * and an end place, plus a list of human‑readable step instructions.  The view
 * model holds an ordered list of legs and tracks whether each leg is expanded
 * (showing its steps) or collapsed.  The UI should observe changes to this model
 * and rebuild its components when the data changes.
 */
public class DirectionsViewModel {

    private final List<LegViewModel> legs = new ArrayList<>();
    private String errorMessage;

    /**
     * Populates this view model with legs derived from the domain route.  All
     * existing legs are cleared, and each new leg is initially collapsed.  Any
     * HTML tags in step instructions are stripped.
     *
     * @param legEntities the list of domain Leg objects to convert
     */
    public void setFromRoute(List<Leg> legEntities) {
        this.legs.clear();
        if (legEntities == null) return;
        int legIndex = 0;
        for (Leg leg : legEntities) {
            String startName = "Start";
            String endName = "Next";
            PlanStop start = leg.getStartLocation();
            PlanStop end = leg.getEndLocation();

            if (legIndex == 0) {
                startName = "Origin";
                if (end != null && end.getPlace() != null) {
                    endName = end.getPlace().getName();
                }
            }
            else if (legIndex == legEntities.size() - 1) {
                if (start != null && start.getPlace() != null) {
                    startName = start.getPlace().getName();
                }
                endName = "Origin";
            }
            else {
                if (start != null && start.getPlace() != null) {
                    startName = start.getPlace().getName();
                }
                if (end != null && end.getPlace() != null) {
                    endName = end.getPlace().getName();
                }
            }
            List<String> stepList = new ArrayList<>();
            for (Step step : leg.getSteps()) {
                String instr = step.getNavInstruction().replaceAll("<[^>]*>", "");
                stepList.add(instr);
            }
            this.legs.add(new LegViewModel(startName, endName, stepList));
            legIndex++;
        }
    }

    /**
     * Clears all legs from this view model.
     */
    public void clear() {
        this.legs.clear();
    }

    public List<LegViewModel> getLegs() {
        return legs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}