package placefinder.usecases.ports;

import placefinder.entities.FavoriteLocation;
import placefinder.entities.PreferenceProfile;

import java.util.List;

/**
 * Gateway interface for accessing and persisting user preferences and favorite locations.
 */
public interface PreferenceGateway {
    PreferenceProfile loadForUser(int userId) throws Exception;
    void saveForUser(PreferenceProfile profile) throws Exception;

    List<FavoriteLocation> listFavorites(int userId) throws Exception;
    FavoriteLocation addFavorite(int userId, String name, String address, double lat, double lon) throws Exception;
    void deleteFavorite(int favoriteId, int userId) throws Exception;
}

