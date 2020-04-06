package lt.galdebar.monmonmvc.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class EmailSenderService {

    private static final String REGISTER_CONFIRM = "localhost:8080/user/register/confirm";
    private static final String CONNECTUSER_CONFIRM = "localhost:8080/user/link/confirm";
    private static final String CHANGE_EMAIL = "localhost:8080/user/changeemail/confirm";

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendRegistrationConformationEmail(String recepient, String token) {
        log.info(String.format(
                "Sending registration email to: ",
                recepient
        ));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recepient);
        message.setSubject("MonMon Registration Confirmation");
        message.setText(generateRegistrationLink(token));

        javaMailSender.send(message);
    }

    public void sendLinkUsersConfirmationEmail(String recepient, String token) {
        log.info(String.format(
                "Sending link users email to: ",
                recepient
        ));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recepient);
        message.setSubject("MonMon User Sync Confirmation");
        message.setText(generateUserConnectConfirmationLink(token));

        javaMailSender.send(message);
    }

    public void sendEmailChangeConfirmationEmail(String recepient, String token) {
        log.info(String.format(
                "Sending email change email to: ",
                recepient
        ));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recepient);
        message.setSubject("MonMon Email Change Confirmation");
        message.setText(generateEmailChangeConfirmationLink(token));

        javaMailSender.send(message);
    }

    public void sendDeletionWarningEmail(String recepient, int period) {
        log.info(String.format(
                "Sending account deletion warning to $s",
                recepient
        ));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recepient);
        message.setSubject("MonMon Account Deletion Warning");
        message.setText(String.format(
                "Warning \n Account deletion requested. \n " +
                        "The user will be deleted in %d. \n" +
                        "If you log in at any time during that period, deletion will be cancelled",
                period
        ));

        javaMailSender.send(message);
    }

    public void sendDeletionConfirmationEmail(String recepient) {
        log.info(String.format(
                "Sending account deletion confirmation to $s",
                recepient
        ));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recepient);
        message.setSubject("MonMon Account Deleted");
        message.setText(String.format(
                "Your account has been deleted. You will no longer be able to log into MonMon"
        ));

        javaMailSender.send(message);
    }

    private String generateRegistrationLink(String token) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(REGISTER_CONFIRM)
                .append("/")
                .append(token);
        return stringBuilder.toString();
    }

    private String generateUserConnectConfirmationLink(String token) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CONNECTUSER_CONFIRM)
                .append("/")
                .append(token);
        return stringBuilder.toString();
    }

    private String generateEmailChangeConfirmationLink(String token) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CHANGE_EMAIL)
                .append("/")
                .append(token);
        return stringBuilder.toString();
    }
}
