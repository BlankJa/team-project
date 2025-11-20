package interface_adapters.views;

import entities.User;

public interface RegistrationView {
    void showLoading();
    void hideLoading();
    void showError(String message);
    void showSuccess(String message);
    void navigateToLogin(User user);
    void clearForm();
}