package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.domain.dao.token.LinkUsersTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.token.UserEmailChangeTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.token.UserRegistrationTokenDAO;
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

@Service
class TokenService {

    private static final int EXPIRATION_IN_HOURS = 24;

    @Autowired
    private UserRegistrationTokenRepo registrationTokenRepo;

    @Autowired
    private UserEmailChangeTokenRepo emailChangeTokenRepo;

    @Autowired
    private LinkUsersTokenRepo linkUsersTokenRepo;

    UserRegistrationTokenDAO createRegistrationToken(UserDAO newUser, String token) {
        UserRegistrationTokenDAO registrationToken = new UserRegistrationTokenDAO();
        registrationToken.setUser(newUser);
        registrationToken.setToken(token);
        registrationToken.setExpiryDate(calculateTokenExpiryDate());
        return registrationTokenRepo.save(registrationToken);
    }

    UserEmailChangeTokenDAO createEmailChangeToken(UserDAO currentUser, String newEmail, String token){
        UserEmailChangeTokenDAO emailChangeTokenDAO = new UserEmailChangeTokenDAO();
        emailChangeTokenDAO.setUser(currentUser);
        emailChangeTokenDAO.setNewEmail(newEmail);
        emailChangeTokenDAO.setToken(token);
        emailChangeTokenDAO.setExpiryDate(calculateTokenExpiryDate());
        return emailChangeTokenRepo.save(emailChangeTokenDAO);
    }

    LinkUsersTokenDAO createLinkUsersToken(UserDAO userA, UserDAO userB, String token){
        LinkUsersTokenDAO connectionTokenDAO = new LinkUsersTokenDAO();
        connectionTokenDAO.setToken(token);
        connectionTokenDAO.setUserA(userA);
        connectionTokenDAO.setUserB(userB);
        connectionTokenDAO.setExpiryDate(calculateTokenExpiryDate());
        return linkUsersTokenRepo.save(connectionTokenDAO);
    }

    @Transactional
    UserRegistrationTokenDAO renewRegistrationToken(String token) throws UserAlreadyValidated, TokenNotFound, TokenNotExpired {
        UserRegistrationTokenDAO registrationToken = registrationTokenRepo.findByToken(token);
        if (registrationToken == null) {
            throw new TokenNotFound();
        }
        if (registrationToken.getUser().isValidated()) {
            throw new UserAlreadyValidated();
        }
        if (registrationToken.getExpiryDate().getTime() - Calendar.getInstance().getTime().getTime() > 0) {
            throw new TokenNotExpired();
        }
        registrationToken.setToken(UUID.randomUUID().toString());
        registrationToken.setExpiryDate(calculateTokenExpiryDate());
        return registrationToken;
    }

    LinkUsersTokenDAO renewLinkUsersToken(String token) throws LinkUsersTokenExpired, LinkUsersTokenNotFound {
        LinkUsersTokenDAO tokenDAO = checkLinkUsersToken(token);
        String newToken = UUID.randomUUID().toString();
        Date newExpirationDate = calculateTokenExpiryDate();

        tokenDAO.setToken(newToken);
        tokenDAO.setExpiryDate(newExpirationDate);

        return linkUsersTokenRepo.save(tokenDAO);
    }

    UserRegistrationTokenDAO checkRegistrationToken(String token) throws UserAlreadyValidated, TokenNotFound, TokenExpired {
        UserRegistrationTokenDAO registrationToken = registrationTokenRepo.findByToken(token);
        if (registrationToken == null) {
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

    LinkUsersTokenDAO checkLinkUsersToken(String token) throws LinkUsersTokenNotFound, LinkUsersTokenExpired {
        LinkUsersTokenDAO foundToken = linkUsersTokenRepo.findByToken(token);
        if (foundToken == null) {
            throw new LinkUsersTokenNotFound();
        }
        if (foundToken.getExpiryDate().getTime() - Calendar.getInstance().getTime().getTime() <= 0) {
            throw new LinkUsersTokenExpired();
        }
        return foundToken;
    }

    UserEmailChangeTokenDAO checkEmailChangeToken(String token) throws TokenNotFound, TokenExpired {
        UserEmailChangeTokenDAO tokenDAO = emailChangeTokenRepo.findByToken(token);
        if (tokenDAO == null) {
            throw new TokenNotFound();
        }
        if (tokenDAO.getExpiryDate().getTime() - Calendar.getInstance().getTime().getTime() <= 0) {
            throw new TokenExpired();
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
