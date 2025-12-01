package placefinder.usecases.preferences;

public class DeleteFavoriteInputData {
    private final int userId;
    private final int favoriteId;

    public DeleteFavoriteInputData(int userId, int favoriteId) {
        this.userId = userId;
        this.favoriteId = favoriteId;
    }

    public int getUserId() { return userId; }
    public int getFavoriteId() { return favoriteId; }
}
