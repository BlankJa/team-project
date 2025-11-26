package placefinder.usecases.verify;

import placefinder.entities.User;
import placefinder.usecases.ports.UserGateway;

/**
 * Use case interactor for verifying a user's email with a code.
 */
public class VerifyEmailInteractor implements VerifyEmailInputBoundary {

    private final UserGateway userGateway;
    private final VerifyEmailOutputBoundary presenter;

    public VerifyEmailInteractor(UserGateway userGateway,
                                 VerifyEmailOutputBoundary presenter) {
        this.userGateway = userGateway;
        this.presenter = presenter;
    }

    @Override
    public void execute(VerifyEmailInputData inputData) {
        String email = inputData.getEmail() == null ? "" : inputData.getEmail().trim();
        String code = inputData.getCode() == null ? "" : inputData.getCode().trim();

        System.out.println("[VERIFY] email='" + email + "', code='" + code + "'");

        if (email.isEmpty() || code.isEmpty()) {
            presenter.present(new VerifyEmailOutputData(false, "Email and code are required."));
            return;
        }

        try {
            User user = userGateway.findByEmail(email);
            System.out.println("[VERIFY] findByEmail returned: "
                    + (user == null ? "null" : "user id=" + user.getId()));

            if (user == null) {
                presenter.present(new VerifyEmailOutputData(false, "No account found for that email."));
                return;
            }

            if (user.isVerified()) {
                presenter.present(new VerifyEmailOutputData(true, "This email is already verified."));
                return;
            }

            String storedCode = user.getVerificationCode();
            if (storedCode == null || !storedCode.equals(code)) {
                presenter.present(new VerifyEmailOutputData(false, "Invalid verification code."));
                return;
            }

            // Success: mark verified and clear code
            user.setVerified(true);
            user.setVerificationCode(null);
            userGateway.save(user);

            presenter.present(new VerifyEmailOutputData(true,
                    "Email verified successfully. You can now log in."));

        } catch (Exception e) {
            presenter.present(new VerifyEmailOutputData(false,
                    "Verification failed: " + e.getMessage()));
        }
    }
}