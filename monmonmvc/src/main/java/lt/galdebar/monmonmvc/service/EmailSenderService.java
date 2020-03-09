package lt.galdebar.monmonmvc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    private static final String CONFIRMATION_PATH = "localhost:8080/register/confirm";

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendConfirmationEmail(String recepient, String token){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recepient);
        message.setSubject("MonMon Registration Confirmation");
        message.setText(generateLink(token));

        javaMailSender.send(message);
    }

    private String generateLink(String token) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CONFIRMATION_PATH)
                .append("/")
                .append(token);
        return stringBuilder.toString();
    }
}
