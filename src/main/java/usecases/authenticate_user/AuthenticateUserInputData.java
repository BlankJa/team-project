package usecases.authenticate_user;

public class AuthenticateUserInputData {
    private final String email;
    private final String password;

    public AuthenticateUserInputData(String email, String password) {
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
