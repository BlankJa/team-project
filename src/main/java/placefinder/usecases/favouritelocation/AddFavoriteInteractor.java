package placefinder.usecases.favouritelocation;

import placefinder.entities.FavoriteLocation;
import placefinder.entities.GeocodeResult;
import placefinder.usecases.dataacessinterfaces.GeocodingDataAccessInterface;
import placefinder.usecases.dataacessinterfaces.PreferenceDataAccessInterface;

public class AddFavoriteInteractor implements AddFavoriteInputBoundary {

    private final PreferenceDataAccessInterface preferenceDataAccessInterface;
    private final GeocodingDataAccessInterface geocodingDataAccessInterface;
    private final AddFavoriteOutputBoundary presenter;

    public AddFavoriteInteractor(PreferenceDataAccessInterface preferenceDataAccessInterface,
                                 GeocodingDataAccessInterface geocodingDataAccessInterface,
                                 AddFavoriteOutputBoundary presenter) {
        this.preferenceDataAccessInterface = preferenceDataAccessInterface;
        this.geocodingDataAccessInterface = geocodingDataAccessInterface;
        this.presenter = presenter;
    }

    @Override
    public void execute(AddFavoriteInputData inputData) {
        try {
            GeocodeResult geo = geocodingDataAccessInterface.geocode(inputData.getAddress());
            if (geo == null) {
                presenter.present(new AddFavoriteOutputData(null, "Could not find that location."));
                return;
            }
            FavoriteLocation fav = preferenceDataAccessInterface.addFavorite(
                    inputData.getUserId(),
                    inputData.getName(),
                    geo.getFormattedAddress(),
                    geo.getLat(),
                    geo.getLon()
            );
            presenter.present(new AddFavoriteOutputData(fav, null));
        } catch (Exception e) {
            presenter.present(new AddFavoriteOutputData(null, e.getMessage()));
        }
    }
}
