package placefinder.usecases.login;

/**
 * Data transfer object containing login input information.
 * Holds email and password for authentication.
 */
public class LoginInputData {

    private final String email;
    private final String password;

    public LoginInputData(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
