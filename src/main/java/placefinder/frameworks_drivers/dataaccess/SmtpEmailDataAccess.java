package placefinder.frameworks_drivers.dataaccess;

import placefinder.usecases.dataacessinterfaces.EmailDataAccessInterface;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SmtpEmailDataAccess implements EmailDataAccessInterface {

    private final String username;
    private final String password;

    public SmtpEmailDataAccess(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void sendVerificationEmail(String to, String code) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Important for newer Java versions + Gmail
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(username));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject("Your TravelScheduler Verification Code");
        msg.setText("Your verification code is: " + code + "\n\n" +
                "Enter this code in the app to finish creating your account.");

        Transport.send(msg);
    }
}