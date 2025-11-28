package placefinder.usecases.ports;

import placefinder.entities.GeocodeResult;

public interface GeocodingGateway {
    GeocodeResult geocode(String query) throws Exception;
}
