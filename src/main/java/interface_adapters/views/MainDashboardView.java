package interface_adapters.views;

public interface MainDashboardView {
    void showWelcomeMessage(String username);
    void showError(String message);
    void navigateToLogin();
    void openPreferenceFrame();
}