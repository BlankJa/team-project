package interface_adapters.views;

import entities.User;

public interface LoginView {
    void showLoading();
    void hideLoading();
    void showError(String message);
    void showSuccess(String message);
    void navigateToMainDashboard(User user);
    void navigateToRegistration();
    void clearForm();
}