package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.domain.dao.UserConnectionTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.persistence.repositories.UserConnectionTokenRepo;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.exceptions.connectusers.ConnectUsersTokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.connectusers.ConnectUsersTokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenExpired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Service
public class ConnectUsersService {
    private static final int EXPIRATION_IN_HOURS = 24;

    @Autowired
    private UserConnectionTokenRepo connectionTokenRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private EmailSenderService emailService;

    @Autowired
    private UserService userService;

    public void connectUserWithCurrent(UserDTO userToConnect) throws UserNotFound {
        UserDAO currentUserDAO = userService.getCurrentUserDAO();
        UserDAO userToConnectDAO = checkIfUserExists(userToConnect);
        String token = UUID.randomUUID().toString();
        UserConnectionTokenDAO connectionTokenDAO = new UserConnectionTokenDAO();
        connectionTokenDAO.setToken(token);
        connectionTokenDAO.setUserA(currentUserDAO);
        connectionTokenDAO.setUserB(userToConnectDAO);
        connectionTokenDAO.setExpiryDate(calculateTokenExpiryDate());

        if (connectionTokenRepo.save(connectionTokenDAO) != null) {
            emailService.sendUserConnectConfirmationEmail(
                    currentUserDAO.getUserEmail(),
                    token
            );
        }
    }

    public void confirmUserConnect(String token) throws ConnectUsersTokenNotFound, ConnectUsersTokenExpired {
        UserConnectionTokenDAO userConnectionTokenDAO = checkToken(token);
        connectUsers(
                userConnectionTokenDAO.getUserA(),
                userConnectionTokenDAO.getUserB()
        );
        connectUsers(
                userConnectionTokenDAO.getUserB(),
                userConnectionTokenDAO.getUserA()
        );
    }

    private void connectUsers(UserDAO userA, UserDAO userB) {
        userA.getConnectedUsers().add(userB.getUserEmail());
        userRepo.save(userA);
    }

    public void renewToken(String token) throws ConnectUsersTokenExpired, ConnectUsersTokenNotFound {
        UserConnectionTokenDAO tokenDAO = checkToken(token);
        String newToken = UUID.randomUUID().toString();
        Date newExpirationDate = calculateTokenExpiryDate();

        tokenDAO.setToken(newToken);
        tokenDAO.setExpiryDate(newExpirationDate);

        connectionTokenRepo.save(tokenDAO);
    }

    private UserConnectionTokenDAO checkToken(String token) throws ConnectUsersTokenNotFound, ConnectUsersTokenExpired {
        UserConnectionTokenDAO foundToken = connectionTokenRepo.findByToken(token);
        if (foundToken == null) {
            throw new ConnectUsersTokenNotFound();
        }
        if (foundToken.getExpiryDate().getTime() - Calendar.getInstance().getTime().getTime() <= 0) {
            throw new ConnectUsersTokenExpired();
        }
        return foundToken;
    }

    private Date calculateTokenExpiryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.HOUR, EXPIRATION_IN_HOURS);
        return new Date(calendar.getTime().getTime());
    }

    private UserDAO checkIfUserExists(UserDTO userDTO) throws UserNotFound {
        UserDAO foundUser = userRepo.findByUserEmail(userDTO.getUserEmail());
        if (foundUser == null) {
            throw new UserNotFound();
        }
        return foundUser;
    }
}
