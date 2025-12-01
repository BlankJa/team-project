package placefinder.usecases.preferences;

public class AddFavoriteInputData {
    private final int userId;
    private final String name;
    private final String address;

    public AddFavoriteInputData(int userId, String name, String address) {
        this.userId = userId;
        this.name = name;
        this.address = address;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
}
