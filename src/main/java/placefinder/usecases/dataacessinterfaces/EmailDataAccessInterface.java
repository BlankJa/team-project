package placefinder.usecases.dataacessinterfaces;

public interface EmailDataAccessInterface {
    void sendVerificationEmail(String to, String code) throws Exception;
}