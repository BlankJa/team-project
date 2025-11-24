package placefinder.usecases.register;

import placefinder.entities.PasswordUtil;
import placefinder.entities.User;
import placefinder.usecases.ports.UserGateway;

/**
 * Interactor for the registration use case.
 * Handles new user registration with validation and password hashing.
 */
public class RegisterInteractor implements RegisterInputBoundary {

    private final UserGateway userGateway;
    private final RegisterOutputBoundary presenter;

    public RegisterInteractor(UserGateway userGateway, RegisterOutputBoundary presenter) {
        this.userGateway = userGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(RegisterInputData inputData) {
        try {
            String name = safeTrim(inputData.getName());
            String email = safeTrim(inputData.getEmail());
            String password = safeTrim(inputData.getPassword());
            String homeCity = safeTrim(inputData.getHomeCity());

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                presenter.present(new RegisterOutputData(
                        false,
                        "Name, email, and password are required to register."
                ));
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                presenter.present(new RegisterOutputData(
                        false,
                        "Please enter a valid email address."
                ));
                return;
            }

            if (password.length() < 6) {
                presenter.present(new RegisterOutputData(
                        false,
                        "Password must be at least 6 characters long."
                ));
                return;
            }

            User existing = userGateway.findByEmail(email);
            if (existing != null) {
                presenter.present(new RegisterOutputData(false, "Email already in use."));
                return;
            }

            String hash = PasswordUtil.hashPassword(password);
            String homeCityToStore = homeCity.isEmpty() ? null : homeCity;

            User user = new User(
                    null,
                    name,
                    email,
                    hash,
                    homeCityToStore
            );
            userGateway.save(user);

            presenter.present(new RegisterOutputData(
                    true,
                    "Registration successful. You can now log in."
            ));
        } catch (Exception e) {
            presenter.present(new RegisterOutputData(false, e.getMessage()));
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
