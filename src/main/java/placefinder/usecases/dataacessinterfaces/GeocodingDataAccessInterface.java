package placefinder.usecases.dataacessinterfaces;

import placefinder.entities.GeocodeResult;

public interface GeocodingDataAccessInterface {
    GeocodeResult geocode(String query) throws Exception;
}
