package frameworks_and_drivers.data.mappers;

import entities.User;
import frameworks_and_drivers.data.models.UserEntity;

public class UserMapper {
    public User toDomain(UserEntity entity) {
        if (entity == null) return null;

        User user = new User();
        user.setUserName(entity.getUsername());
        user.setEmail(entity.getEmail());
        user.setPassword(entity.getPassword());
        return user;
    }

    public UserEntity toData(User domain) {
        if (domain == null) return null;

        UserEntity entity = new UserEntity();
        entity.setUsername(domain.getUserName());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        return entity;
    }
}