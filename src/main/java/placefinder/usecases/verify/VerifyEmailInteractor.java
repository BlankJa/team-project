package placefinder.usecases.verify;

import placefinder.entities.User;
import placefinder.usecases.dataacessinterfaces.UserDataAccessInterface;

/**
 * Use case interactor for verifying a user's email with a code.
 */
public class VerifyEmailInteractor implements VerifyEmailInputBoundary {

    private final UserDataAccessInterface userDataAccessInterface;
    private final VerifyEmailOutputBoundary presenter;

    public VerifyEmailInteractor(UserDataAccessInterface userDataAccessInterface,
                                 VerifyEmailOutputBoundary presenter) {
        this.userDataAccessInterface = userDataAccessInterface;
        this.presenter = presenter;
    }

    @Override
    public void execute(VerifyEmailInputData inputData) {
        final String email = inputData.getEmail() == null ? "" : inputData.getEmail().trim();
        final String code = inputData.getCode() == null ? "" : inputData.getCode().trim();

        System.out.println("[VERIFY] email='" + email + "', code='" + code + "'");

        if (email.isEmpty() || code.isEmpty()) {
            presenter.present(new VerifyEmailOutputData(false, "Email and code are required."));
            return;
        }

        try {
            final User user = userDataAccessInterface.findByEmail(email);
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

            final String storedCode = user.getVerificationCode();
            if (storedCode == null || !storedCode.equals(code)) {
                presenter.present(new VerifyEmailOutputData(false, "Invalid verification code."));
                return;
            }

            // Success: mark verified and clear code
            user.setVerified(true);
            user.setVerificationCode(null);
            userDataAccessInterface.save(user);

            presenter.present(new VerifyEmailOutputData(true,
                    "Email verified successfully. You can now log in."));

        } catch (Exception e) {
            presenter.present(new VerifyEmailOutputData(false,
                    "Verification failed: " + e.getMessage()));
        }
    }
}
