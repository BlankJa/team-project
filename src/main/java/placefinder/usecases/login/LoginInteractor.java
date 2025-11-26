package placefinder.usecases.login;

import placefinder.entities.PasswordUtil;
import placefinder.entities.User;
import placefinder.usecases.ports.UserGateway;

/**
 * Interactor for the login use case.
 * Handles user authentication by verifying email and password against the database,
 * and ensures the account's email is verified before allowing login.
 */
public class LoginInteractor implements LoginInputBoundary {

    private final UserGateway userGateway;
    private final LoginOutputBoundary presenter;

    public LoginInteractor(UserGateway userGateway, LoginOutputBoundary presenter) {
        this.userGateway = userGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(LoginInputData inputData) {
        try {
            User user = userGateway.findByEmail(inputData.getEmail());
            if (user == null) {
                presenter.present(new LoginOutputData(false, "Invalid email or password.", null));
                return;
            }
            String hash = PasswordUtil.hashPassword(inputData.getPassword());
            if (!hash.equals(user.getPasswordHash())) {
                presenter.present(new LoginOutputData(false, "Invalid email or password.", null));
                return;
            }

            // 🔴 NEW: block unverified users
            if (!user.isVerified()) {
                presenter.present(new LoginOutputData(
                        false,
                        "Please verify your email before signing in.",
                        null
                ));
                return;
            }

            presenter.present(new LoginOutputData(true, null, user));
        } catch (Exception e) {
            presenter.present(new LoginOutputData(false, e.getMessage(), null));
        }
    }
}