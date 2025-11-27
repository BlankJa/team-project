package placefinder.usecases.register;

import placefinder.entities.PasswordUtil;
import placefinder.entities.User;
import placefinder.usecases.ports.UserGateway;
import placefinder.usecases.ports.EmailGateway;

import java.security.SecureRandom;

/**
 * Interactor for the registration use case.
 * Creates an unverified user, generates a verification code,
 * sends it by email, and requires verification before login.
 */
public class RegisterInteractor implements RegisterInputBoundary {

    private final UserGateway userGateway;
    private final RegisterOutputBoundary presenter;
    private final EmailGateway emailGateway;
    private final SecureRandom random = new SecureRandom();

    public RegisterInteractor(UserGateway userGateway,
                              RegisterOutputBoundary presenter,
                              EmailGateway emailGateway) {
        this.userGateway = userGateway;
        this.presenter = presenter;
        this.emailGateway = emailGateway;
    }

    @Override
    public void execute(RegisterInputData inputData) {
        try {
            String name = safeTrim(inputData.getName());
            String email = safeTrim(inputData.getEmail());
            String password = safeTrim(inputData.getPassword());
            String homeCity = safeTrim(inputData.getHomeCity());

            // ===== Basic validation =====
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

            // ===== Existing user check =====
            User existing = userGateway.findByEmail(email);
            if (existing != null) {
                if (existing.isVerified()) {
                    // Already registered & verified -> hard stop
                    presenter.present(new RegisterOutputData(false, "Email already in use."));
                } else {
                    // Registered but NOT verified -> resend a fresh code
                    String newCode = generateVerificationCode();
                    existing.setVerificationCode(newCode);
                    userGateway.save(existing);
                    emailGateway.sendVerificationEmail(email, newCode);

                    presenter.present(new RegisterOutputData(
                            false,
                            "An account with this email already exists but is not verified.\n" +
                                    "We’ve sent a new verification code. Please verify to complete sign up."
                    ));
                }
                return;
            }

            // ===== Create new unverified user =====
            String hash = PasswordUtil.hashPassword(password);
            String homeCityToStore = homeCity.isEmpty() ? null : homeCity;

            String verificationCode = generateVerificationCode();

            // Constructor sets basic fields; we set verification flags explicitly
            User user = new User(
                    null,
                    name,
                    email,
                    hash,
                    homeCityToStore
            );
            user.setVerified(false);
            user.setVerificationCode(verificationCode);

            userGateway.save(user);

            // Send verification email
            emailGateway.sendVerificationEmail(email, verificationCode);

            presenter.present(new RegisterOutputData(
                    true,
                    "We’ve emailed you a 6-digit verification code.\n" +
                            "Enter it to finish creating your account."
            ));

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("UNIQUE constraint failed") && msg.contains("users.email")) {
                msg = "Email already in use.";
            } else if (msg == null || msg.isBlank()) {
                msg = "Registration failed due to an unexpected error.";
            } else {
                msg = "Registration failed: " + msg;
            }

            presenter.present(new RegisterOutputData(false, msg));
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String generateVerificationCode() {
        int value = 100000 + random.nextInt(900000); // 100000–999999
        return String.valueOf(value);
    }
}