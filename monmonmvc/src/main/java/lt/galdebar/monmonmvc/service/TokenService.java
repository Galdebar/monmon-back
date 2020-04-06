package lt.galdebar.monmonmvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonmvc.persistence.domain.entities.token.LinkUsersTokenEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.UserEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.token.UserEmailChangeTokenEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.token.UserRegistrationTokenEntity;
import lt.galdebar.monmonmvc.persistence.repositories.LinkUsersTokenRepo;
import lt.galdebar.monmonmvc.persistence.repositories.UserEmailChangeTokenRepo;
import lt.galdebar.monmonmvc.persistence.repositories.UserRegistrationTokenRepo;
import lt.galdebar.monmonmvc.service.exceptions.linkusers.LinkUsersTokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.linkusers.LinkUsersTokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenNotExpired;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyValidated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Log4j2
@Service
class TokenService {

    private static final int EXPIRATION_IN_HOURS = 24;
    @Autowired
    private UserRegistrationTokenRepo registrationTokenRepo;
    @Autowired
    private UserEmailChangeTokenRepo emailChangeTokenRepo;
    @Autowired
    private LinkUsersTokenRepo linkUsersTokenRepo;

    UserRegistrationTokenEntity createRegistrationToken(UserEntity newUser, String token) {
        UserRegistrationTokenEntity registrationToken = new UserRegistrationTokenEntity();
        registrationToken.setUser(newUser);
        registrationToken.setToken(token);
        registrationToken.setExpiryDate(calculateTokenExpiryDate());
        UserRegistrationTokenEntity createdToken = registrationTokenRepo.save(registrationToken);

        log.info(String.format(
                "User registration token created. Token details: %s",
                createdToken.toString()
        ));

        return createdToken;
    }

    UserEmailChangeTokenEntity createEmailChangeToken(UserEntity currentUser, String newEmail, String token){
        UserEmailChangeTokenEntity emailChangeTokenDAO = new UserEmailChangeTokenEntity();
        emailChangeTokenDAO.setUser(currentUser);
        emailChangeTokenDAO.setNewEmail(newEmail);
        emailChangeTokenDAO.setToken(token);
        emailChangeTokenDAO.setExpiryDate(calculateTokenExpiryDate());

        UserEmailChangeTokenEntity createdToken = emailChangeTokenRepo.save(emailChangeTokenDAO);

        log.info(String.format(
                "User email change token created. Token details: %s",
                createdToken.toString()
        ));

        return createdToken;
    }

    LinkUsersTokenEntity createLinkUsersToken(UserEntity userA, UserEntity userB, String token){
        LinkUsersTokenEntity connectionTokenDAO = new LinkUsersTokenEntity();
        connectionTokenDAO.setToken(token);
        connectionTokenDAO.setUserA(userA);
        connectionTokenDAO.setUserB(userB);
        connectionTokenDAO.setExpiryDate(calculateTokenExpiryDate());

        LinkUsersTokenEntity createdToken = linkUsersTokenRepo.save(connectionTokenDAO);

        log.info(String.format(
                "Link users token created. Token details: %s",
                createdToken.toString()
        ));

        return createdToken;
    }

    @Transactional
    UserRegistrationTokenEntity renewRegistrationToken(String token) throws UserAlreadyValidated, TokenNotFound, TokenNotExpired {
        UserRegistrationTokenEntity registrationToken = registrationTokenRepo.findByToken(token);
        if (registrationToken == null) {
            throw new TokenNotFound(token);
        }
        if (registrationToken.getUser().isValidated()) {
            throw new UserAlreadyValidated(registrationToken.getUser().getUserEmail());
        }
        if (registrationToken.getExpiryDate().getTime() - Calendar.getInstance().getTime().getTime() > 0) {
            throw new TokenNotExpired(registrationToken.getUser().getUserEmail());
        }
        registrationToken.setToken(UUID.randomUUID().toString());
        registrationToken.setExpiryDate(calculateTokenExpiryDate());

        UserRegistrationTokenEntity updatedToken = registrationTokenRepo.save(registrationToken);

        log.info(String.format(
                "Register user token extended. Token details: %s",
                updatedToken.toString()
        ));

        return updatedToken;
    }

    LinkUsersTokenEntity renewLinkUsersToken(String token) throws LinkUsersTokenExpired, LinkUsersTokenNotFound {
        LinkUsersTokenEntity tokenDAO = checkLinkUsersToken(token);
        String newToken = UUID.randomUUID().toString();
        Date newExpirationDate = calculateTokenExpiryDate();

        tokenDAO.setToken(newToken);
        tokenDAO.setExpiryDate(newExpirationDate);

        LinkUsersTokenEntity updatedToken = linkUsersTokenRepo.save(tokenDAO);

        log.info(String.format(
                "Link users token extended. Token details: %s",
                updatedToken.toString()
        ));

        return updatedToken;
    }


    UserRegistrationTokenEntity checkRegistrationToken(String token) throws UserAlreadyValidated, TokenNotFound, TokenExpired {
        log.info("Checking registration token. ");
        UserRegistrationTokenEntity registrationToken = registrationTokenRepo.findByToken(token);
        if (registrationToken == null) {
            throw new TokenNotFound(token);
        }
        if (registrationToken.getExpiryDate().getTime() - Calendar.getInstance().getTime().getTime() <= 0) {
            throw new TokenExpired(registrationToken.getUser().getUserEmail(),token);
        }
        if (registrationToken.getUser().isValidated()) {
            throw new UserAlreadyValidated(registrationToken.getUser().getUserEmail());
        }
        return registrationToken;
    }

    LinkUsersTokenEntity checkLinkUsersToken(String token) throws LinkUsersTokenNotFound, LinkUsersTokenExpired {
        log.info("Checking link users token. ");

        LinkUsersTokenEntity foundToken = linkUsersTokenRepo.findByToken(token);
        if (foundToken == null) {
            throw new LinkUsersTokenNotFound(token);
        }
        if (foundToken.getExpiryDate().getTime() - Calendar.getInstance().getTime().getTime() <= 0) {
            throw new LinkUsersTokenExpired(
                    foundToken.getUserA().getUserEmail(),
                    foundToken.getUserB().getUserEmail(),
                    foundToken.getId()
            );
        }
        return foundToken;
    }

    UserEmailChangeTokenEntity checkEmailChangeToken(String token) throws TokenNotFound, TokenExpired {
        log.info("Checking email change token. ");

        UserEmailChangeTokenEntity tokenDAO = emailChangeTokenRepo.findByToken(token);
        if (tokenDAO == null) {
            throw new TokenNotFound(token);
        }
        if (tokenDAO.getExpiryDate().getTime() - Calendar.getInstance().getTime().getTime() <= 0) {
            throw new TokenExpired(tokenDAO.getUser().getUserEmail(), token);
        }
        return tokenDAO;
    }

    private Date calculateTokenExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.HOUR, EXPIRATION_IN_HOURS);
        return new Date(calendar.getTime().getTime());
    }
}
