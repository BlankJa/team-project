package placefinder.interface_adapters.viewmodels;

import placefinder.entities.Place;
import placefinder.entities.Plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlanCreationViewModel {

    private List<Place> recommendedPlaces = new ArrayList<>();
    private String originAddress;
    private boolean weatherUsed;

    private String weatherAdvice;

    private Plan planPreview;
    private boolean planTruncated;
    private Plan lastSavedPlan;

    private String infoMessage;
    private String errorMessage;
    private boolean isLoading = false;

    public List<Place> getRecommendedPlaces() {
        return Collections.unmodifiableList(recommendedPlaces);
    }

    public void setRecommendedPlaces(List<Place> places) {
        this.recommendedPlaces = places != null ? new ArrayList<>(places) : new ArrayList<>();
    }

    public String getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(String originAddress) {
        this.originAddress = originAddress;
    }

    public boolean isWeatherUsed() {
        return weatherUsed;
    }

    public void setWeatherUsed(boolean weatherUsed) {
        this.weatherUsed = weatherUsed;
    }

    public Plan getPlanPreview() {
        return planPreview;
    }

    public void setPlanPreview(Plan planPreview) {
        this.planPreview = planPreview;
    }

    public boolean isPlanTruncated() {
        return planTruncated;
    }

    public void setPlanTruncated(boolean planTruncated) {
        this.planTruncated = planTruncated;
    }

    public Plan getLastSavedPlan() {
        return lastSavedPlan;
    }

    public void setLastSavedPlan(Plan lastSavedPlan) {
        this.lastSavedPlan = lastSavedPlan;
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getWeatherAdvice() {
        return weatherAdvice;
    }

    public void setWeatherAdvice(String weatherAdvice) {
        this.weatherAdvice = weatherAdvice;
    }

    public boolean isLoading() { return isLoading; }
    public void setLoading(boolean loading) { this.isLoading = loading; }
}
