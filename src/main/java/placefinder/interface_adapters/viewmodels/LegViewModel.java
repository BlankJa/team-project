package placefinder.interface_adapters.viewmodels;

import java.util.List;

/**
 * Internal data structure representing a single leg's display state.
 */
public class LegViewModel {
    private final String startPlace;
    private final String endPlace;
    private final List<String> steps;
    private boolean expanded;

    public LegViewModel(String startPlace, String endPlace, List<String> steps) {
        this.startPlace = startPlace;
        this.endPlace = endPlace;
        this.steps = steps;
        this.expanded = false;
    }

    public String getStartPlace() {
        return startPlace;
    }

    public String getEndPlace()   {
        return endPlace;
    }

    public List<String> getSteps() {
        return steps;
    }

    public boolean isExpanded()   {
        return expanded;
    }

    public void toggleExpanded()  {
        this.expanded = !this.expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

}