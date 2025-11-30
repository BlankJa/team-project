package placefinder.entities;

public class GeocodeResult {
    private final double lat;
    private final double lon;
    private final String formattedAddress;

    public GeocodeResult(double lat, double lon, String formattedAddress) {
        this.lat = lat;
        this.lon = lon;
        this.formattedAddress = formattedAddress;
    }

    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public String getFormattedAddress() { return formattedAddress; }
}
