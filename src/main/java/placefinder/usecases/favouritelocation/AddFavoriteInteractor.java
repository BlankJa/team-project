package placefinder.usecases.favouritelocation;

import placefinder.entities.FavoriteLocation;
import placefinder.entities.GeocodeResult;
import placefinder.usecases.ports.GeocodingGateway;
import placefinder.usecases.ports.PreferenceGateway;

public class AddFavoriteInteractor implements AddFavoriteInputBoundary {

    private final PreferenceGateway preferenceGateway;
    private final GeocodingGateway geocodingGateway;
    private final AddFavoriteOutputBoundary presenter;

    public AddFavoriteInteractor(PreferenceGateway preferenceGateway,
                                 GeocodingGateway geocodingGateway,
                                 AddFavoriteOutputBoundary presenter) {
        this.preferenceGateway = preferenceGateway;
        this.geocodingGateway = geocodingGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(AddFavoriteInputData inputData) {
        try {
            GeocodeResult geo = geocodingGateway.geocode(inputData.getAddress());
            if (geo == null) {
                presenter.present(new AddFavoriteOutputData(null, "Could not find that location."));
                return;
            }
            FavoriteLocation fav = preferenceGateway.addFavorite(
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
