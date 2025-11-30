package placefinder.usecases.searchplaces;

import placefinder.entities.Place;

import java.util.List;

public class SearchPlacesOutputData {

    private final List<Place> places;
    private final String originAddress;
    private final boolean weatherUsed;
    private final String weatherAdvice;   // NEW
    private final String errorMessage;

    public SearchPlacesOutputData(List<Place> places,
                                  String originAddress,
                                  boolean weatherUsed,
                                  String weatherAdvice,
                                  String errorMessage) {
        this.places = places;
        this.originAddress = originAddress;
        this.weatherUsed = weatherUsed;
        this.weatherAdvice = weatherAdvice;
        this.errorMessage = errorMessage;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public String getOriginAddress() {
        return originAddress;
    }

    public boolean isWeatherUsed() {
        return weatherUsed;
    }

    public String getWeatherAdvice() {
        return weatherAdvice;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
