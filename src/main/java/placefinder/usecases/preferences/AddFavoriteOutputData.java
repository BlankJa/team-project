package placefinder.usecases.preferences;

import placefinder.entities.FavoriteLocation;

public class AddFavoriteOutputData {
    private final FavoriteLocation favorite;
    private final String errorMessage;

    public AddFavoriteOutputData(FavoriteLocation favorite, String errorMessage) {
        this.favorite = favorite;
        this.errorMessage = errorMessage;
    }

    public FavoriteLocation getFavorite() { return favorite; }
    public String getErrorMessage() { return errorMessage; }
}
