package placefinder.usecases.ports;

public interface EmailGateway {
    void sendVerificationEmail(String to, String code) throws Exception;
}