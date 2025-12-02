package placefinder.usecases.verify;

public class VerifyEmailInputData {
    private final String email;
    private final String code;

    public VerifyEmailInputData(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }
}
