package usecases.authenticate_user;

import entities.User;
import usecases.common.Result;
import usecases.dataaccess.UserRepository;

public class AuthenticateUserUseCase {
    private final UserRepository userRepository;

    public AuthenticateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Result<User> execute(String email, String password) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return Result.error("Email cannot be empty");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("Password cannot be empty");
            }

            User user = userRepository.authenticate(email, password);
            if (user == null) {
                return Result.error("Invalid email or password");
            }

            return Result.success(user);
        } catch (Exception e) {
            return Result.error("Authentication failed: " + e.getMessage());
        }
    }
}