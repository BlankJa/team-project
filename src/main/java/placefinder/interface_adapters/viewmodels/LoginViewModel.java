package placefinder.interface_adapters.viewmodels;

import placefinder.entities.User;

/**
 * View model for the login screen.
 * Holds the state of the logged-in user and any error messages.
 */
public class LoginViewModel {
    private User loggedInUser;
    private String errorMessage;

    public User getLoggedInUser() { return loggedInUser; }
    public void setLoggedInUser(User loggedInUser) { this.loggedInUser = loggedInUser; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
