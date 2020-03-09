package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserRegistrationTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.LoginAttemptDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.persistence.repositories.UserRegistrationTokenRepo;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyExists;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyValidated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Service
public class UserRegistrationService {
    private static final int EXPIRATION_IN_HOURS = 24;

    @Autowired
    private UserRegistrationTokenRepo tokenRepo;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private UserService userService;

    @Transactional
    public void registerNewUser(LoginAttemptDTO registrationAttempt) throws UserAlreadyExists {
        UserDAO newUser = registerUser(registrationAttempt);
        String token = UUID.randomUUID().toString();
        UserRegistrationTokenDAO tokenDAO = createRegistrationToken(newUser, token);
        emailSenderService.sendConfirmationEmail(
                newUser.getUserEmail(),
                token
        );
    }

    @Transactional
    public boolean confirmRegistration(String token) throws TokenNotFound, UserAlreadyValidated, TokenExpired {
        UserRegistrationTokenDAO registrationToken = checkToken(token);
        return userService.validateUser(registrationToken.getUser());
    }

    @Transactional
    public void extendTokenDuration(String token) throws UserAlreadyValidated, TokenNotFound {
        UserRegistrationTokenDAO registrationToken = tokenRepo.findByToken(token);
        if (registrationToken.getId() == null) {
            throw new TokenNotFound();
        }
        if (registrationToken.getUser().isValidated()) {
            throw new UserAlreadyValidated();
        }
        if (registrationToken.getExpiryDate().getTime() - Calendar.getInstance().getTime().getTime() <= 0) {
            registrationToken.setToken(UUID.randomUUID().toString());
            registrationToken.setExpiryDate(calculateTokenExpiryDate());
            emailSenderService.sendConfirmationEmail(
                    registrationToken.getUser().getUserEmail(),
                    registrationToken.getToken()
            );
        }

    }

    private UserRegistrationTokenDAO checkToken(String token) throws UserAlreadyValidated, TokenNotFound, TokenExpired {
        UserRegistrationTokenDAO registrationToken = tokenRepo.findByToken(token);
        if (registrationToken.getId() == null) {
            throw new TokenNotFound();
        }
        if (registrationToken.getExpiryDate().getTime() - Calendar.getInstance().getTime().getTime() <= 0) {
            throw new TokenExpired();
        }
        if (registrationToken.getUser().isValidated()) {
            throw new UserAlreadyValidated();
        }
        return registrationToken;
    }

    private UserRegistrationTokenDAO createRegistrationToken(UserDAO newUser, String token) {
        UserRegistrationTokenDAO registrationToken = new UserRegistrationTokenDAO();
        registrationToken.setUser(newUser);
        registrationToken.setToken(token);
        registrationToken.setExpiryDate(calculateTokenExpiryDate());
        return tokenRepo.save(registrationToken);
    }

    private UserDAO registerUser(LoginAttemptDTO loginAttemptDTO) throws UserAlreadyExists {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserEmail(loginAttemptDTO.getUserEmail());
        userDTO.setUserPassword(loginAttemptDTO.getUserPassword());
        return userService.registerUser(userDTO);
    }

    private Date calculateTokenExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.HOUR, EXPIRATION_IN_HOURS);
        return new Date(calendar.getTime().getTime());
    }
}
