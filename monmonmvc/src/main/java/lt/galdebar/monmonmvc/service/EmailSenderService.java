package lt.galdebar.monmonmvc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    private static final String REGISTER_CONFIRM = "localhost:8080/register/confirm";
    private static final String CONNECTUSER_CONFIRM = "localhost:8080/user/connectuser/confirm";

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendConfirmationEmail(String recepient, String token){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recepient);
        message.setSubject("MonMon Registration Confirmation");
        message.setText(generateRegistrationLink(token));

        javaMailSender.send(message);
    }

    public void sendUserConnectConfirmationEmail(String recepient,String token){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recepient);
        message.setSubject("MonMon User Sync Confirmation");
        message.setText(generateRegistrationLink(token));

        javaMailSender.send(message);
    }

    private String generateRegistrationLink(String token) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(REGISTER_CONFIRM)
                .append("/")
                .append(token);
        return stringBuilder.toString();
    }

    private String generateUserConnectConfirmationLink(String token){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CONNECTUSER_CONFIRM)
                .append("/")
                .append(token);
        return stringBuilder.toString();
    }

}
