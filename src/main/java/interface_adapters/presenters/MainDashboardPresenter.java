package interface_adapters.presenters;

import interface_adapters.views.MainDashboardView;

import javax.swing.*;

public class MainDashboardPresenter {
    private final MainDashboardView view;

    public MainDashboardPresenter(MainDashboardView view) {
        this.view = view;
    }

    public void onLogoutClicked() {
        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            view.navigateToLogin();
        }
    }

    public void onPreferencesClicked() {
        view.openPreferenceFrame();
    }
}