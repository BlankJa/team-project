package usecases.create_user;

import entities.User;
import usecases.common.Result;
import usecases.dataaccess.UserRepository;

public class CreateUserUseCase {
    private final UserRepository userRepository;

    public CreateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Result<User> execute(String username, String email, String password) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return Result.error("Username cannot be empty");
            }
            if (email == null || email.trim().isEmpty()) {
                return Result.error("Email cannot be empty");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("Password cannot be empty");
            }

            if (userRepository.userExists(email)) {
                return Result.error("User with this email already exists");
            }

            User newUser = new User(username, email, password);
            boolean created = userRepository.createUser(newUser) != null;

            if (created) {
                return Result.success(newUser);
            } else {
                return Result.error("Failed to create user");
            }
        } catch (Exception e) {
            return Result.error("Registration failed: " + e.getMessage());
        }
    }
}