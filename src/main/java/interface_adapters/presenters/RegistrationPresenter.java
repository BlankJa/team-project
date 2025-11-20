package interface_adapters.presenters;

import entities.User;
import interface_adapters.views.RegistrationView;
import usecases.common.Result;
import usecases.create_user.CreateUserUseCase;

public class RegistrationPresenter {
    private final RegistrationView view;
    private final CreateUserUseCase createUserUseCase;

    public RegistrationPresenter(RegistrationView view, CreateUserUseCase createUserUseCase) {
        this.view = view;
        this.createUserUseCase = createUserUseCase;
    }

    public void onRegisterClicked(String username, String email, String password, String confirmPassword) {
        // Validation
        if (username == null || username.trim().isEmpty()) {
            view.showError("Please enter a username");
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            view.showError("Please enter an email address");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            view.showError("Please enter a password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            view.showError("Passwords do not match");
            return;
        }

        view.showLoading();

        new Thread(() -> {
            try {
                Result result = createUserUseCase.execute(username, email, password);

                javax.swing.SwingUtilities.invokeLater(() -> {
                    view.hideLoading();

                    if (result.isSuccess()) {
                        view.showSuccess("Registration successful! Please login with your new account.");
                        view.navigateToLogin((User)result.getData());
                    } else {
                        view.showError(result.getError());
                    }
                });
            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    view.hideLoading();
                    view.showError("Registration failed: " + e.getMessage());
                });
            }
        }).start();
    }

    public void onBackToLoginClicked() {
        view.navigateToLogin(null);
    }
}