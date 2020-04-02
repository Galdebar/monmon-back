package lt.galdebar.monmonmvc.service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonmvc.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonmvc.persistence.domain.dao.token.LinkUsersTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.token.UserEmailChangeTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.token.UserRegistrationTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.*;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.exceptions.linkusers.LinkUsersMatch;
import lt.galdebar.monmonmvc.service.exceptions.linkusers.LinkUsersTokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.linkusers.LinkUsersTokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.*;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotValidated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Log4j2
public class UserService {

    private static final int EXPIRATION_IN_HOURS = 24;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public AuthTokenDTO login(LoginAttemptDTO loginAttemptDTO) throws UserNotValidated, UserNotFound {
        UserDAO foundUser = loginUserCheck(loginAttemptDTO);
        String token = jwtTokenProvider.createToken(
                foundUser.getUserEmail(),
                Collections.singletonList(foundUser.toString())
        );

        AuthTokenDTO tokenDTO = new AuthTokenDTO(
                foundUser.getUserEmail(),
                token
        );
        return tokenDTO;
    }

    private UserDAO loginUserCheck(LoginAttemptDTO loginAttemptDTO) throws UserNotFound, UserNotValidated {
        log.info("Checking user login credentials. ");
        UserDAO foundUser = userRepo.findByUserEmail(loginAttemptDTO.getUserEmail());
        if (foundUser == null) {
            throw new UserNotFound(loginAttemptDTO.getUserEmail());
        }
        if (!foundUser.isValidated()) {
            throw new UserNotValidated(loginAttemptDTO.getUserEmail());
        }
        if (!passwordEncoder.matches(loginAttemptDTO.getUserPassword(), foundUser.getUserPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        return foundUser;
    }

    public UserDAO findByUserEmail(String userEmail) throws UserNotFound {
        UserDAO foundUser = userRepo.findByUserEmail(userEmail);
        if (foundUser == null) {
            throw new UserNotFound(userEmail);
        }
        return foundUser;
    }

    @Transactional
    public void registerNewUser(LoginAttemptDTO registrationAttempt) throws UserAlreadyExists {
        UserDAO newUser = createNewUser(
                registrationAttempt.getUserEmail(),
                registrationAttempt.getUserPassword()
        );
        String token = UUID.randomUUID().toString();
        UserRegistrationTokenDAO tokenDAO = tokenService.createRegistrationToken(newUser, token);
        emailSenderService.sendRegistrationConformationEmail(
                newUser.getUserEmail(),
                token
        );
    }

    @Transactional
    public boolean confirmRegistration(String token) throws TokenNotFound, UserAlreadyValidated, TokenExpired {
        log.info("Validating user. ");
        UserRegistrationTokenDAO registrationToken = tokenService.checkRegistrationToken(token);
        UserDAO userToValidate = registrationToken.getUser();
        userToValidate.setValidated(true);
        return userRepo.save(userToValidate) != null;
    }

    @Transactional
    public void changeUserEmail(EmailChangeRequestDTO emailChangeRequestDTO) throws UserAlreadyExists {
        if (checkIfUserExists(emailChangeRequestDTO.getNewEmail())) {
            throw new UserAlreadyExists(emailChangeRequestDTO.getNewEmail());
        }

        UserDAO currentUser = getCurrentUserDAO();
        String token = UUID.randomUUID().toString();

        UserEmailChangeTokenDAO tokenDAO = tokenService.createEmailChangeToken(currentUser, emailChangeRequestDTO.getNewEmail(), token);
        emailSenderService.sendEmailChangeConfirmationEmail(
                emailChangeRequestDTO.getNewEmail(),
                token
        );
    }

    @Transactional
    public boolean confirmUserEmailChange(String token) throws TokenNotFound, TokenExpired {
        UserEmailChangeTokenDAO tokenDAO = tokenService.checkEmailChangeToken(token);
        UserDAO userToUpdate = tokenDAO.getUser();

        log.info(String.format(
                "Updating user email. Old email: %s | New email: %s ",
                userToUpdate.getUserEmail(),
                tokenDAO.getNewEmail()
        ));

        userToUpdate.setUserEmail(tokenDAO.getNewEmail());
        return userRepo.save(userToUpdate) != null;

    }

    @Transactional
    public void renewRegistrationToken(String token) throws UserAlreadyValidated, TokenNotExpired, TokenNotFound {
        UserRegistrationTokenDAO registrationToken = tokenService.renewRegistrationToken(token);
        emailSenderService.sendRegistrationConformationEmail(
                registrationToken.getUser().getUserEmail(),
                registrationToken.getToken()
        );
    }

    @Transactional
    public void changePassword(PasswordChangeRequestDTO passwordChangeRequestDTO) throws BadCredentialsException {
        UserDAO currentUser = getCurrentUserDAO();
        if (!currentUser.getUserEmail().equals(passwordChangeRequestDTO.getUserEmail())) {
            throw new BadCredentialsException("Invalid email");
        }

        if (passwordEncoder.matches(passwordChangeRequestDTO.getOldPassword(), currentUser.getUserPassword())) {
            currentUser.setUserPassword(
                    passwordEncoder.encode(
                            passwordChangeRequestDTO.getNewPassword()
                    )
            );
            userRepo.save(currentUser);
        } else throw new BadCredentialsException("Invalid password supplied");
    }


    public List<String> getLinkedUsers() {
        UserDAO currentUser = getCurrentUserDAO();
        List<String> connectedUserNames = new ArrayList<>();

        for (String userName : currentUser.getLinkedUsers()) {
            if (userName != null) {
                connectedUserNames.add(userName);
            }
        }

        return connectedUserNames;
    }

    @Transactional
    public void linkUserWithCurrent(UserDTO userToConnect) throws UserNotFound, UserNotValidated, LinkUsersMatch {
        UserDAO currentUserDAO = getCurrentUserDAO();
        UserDAO userToConnectDAO = findByUserEmail(userToConnect.getUserEmail());
        if (!userToConnectDAO.isValidated()) {
            throw new UserNotValidated(userToConnect.getUserEmail());
        }
        if (currentUserDAO.getId().equals(userToConnectDAO.getId())) {
            throw new LinkUsersMatch(currentUserDAO.getUserEmail(), userToConnect.getUserEmail());
        }
        String token = UUID.randomUUID().toString();
        LinkUsersTokenDAO connectionTokenDAO = tokenService.createLinkUsersToken(currentUserDAO, userToConnectDAO, token);

        if (connectionTokenDAO != null) {
            emailSenderService.sendLinkUsersConfirmationEmail(
                    currentUserDAO.getUserEmail(),
                    token
            );
        }
    }

    @Transactional
    public void confirmLinkUsers(String token) throws LinkUsersTokenNotFound, LinkUsersTokenExpired {
        LinkUsersTokenDAO linkUsersTokenDAO = tokenService.checkLinkUsersToken(token);
        log.info(String.format(
                "Linking users. User A: %s| User B: %s",
                linkUsersTokenDAO.getUserA().getUserEmail(),
                linkUsersTokenDAO.getUserB().getUserEmail()
        ));
        linkUsers(
                linkUsersTokenDAO.getUserA(),
                linkUsersTokenDAO.getUserB()
        );
        linkUsers(
                linkUsersTokenDAO.getUserB(),
                linkUsersTokenDAO.getUserA()
        );
    }

    private void linkUsers(UserDAO userA, UserDAO userB) {
        userA.getLinkedUsers().add(userB.getUserEmail());
        userRepo.save(userA);
    }

    public void renewLinkUsersToken(String token) throws LinkUsersTokenExpired, LinkUsersTokenNotFound {
        LinkUsersTokenDAO connectionTokenDAO = tokenService.renewLinkUsersToken(token);
        if (connectionTokenDAO != null) {
            emailSenderService.sendLinkUsersConfirmationEmail(
                    connectionTokenDAO.getUserB().getUserEmail(),
                    token
            );
        }
    }

    private UserDAO getCurrentUserDAO() {
        return userRepo.findByUserEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    private UserDTO daoToDto(UserDAO userDAO) {
        if (userDAO == null) {
            return new UserDTO();
        }
        return new UserDTO(userDAO.getUserEmail(), userDAO.getUserPassword());
    }

    UserDAO dtoToDao(UserDTO userDTO) {
        UserDAO newUser = new UserDAO();
        newUser.setUserEmail(userDTO.getUserEmail());
        newUser.setUserPassword(userDTO.getUserPassword());
        return newUser;
    }

    private boolean checkIfUserExists(String userEmail) {
        return userRepo.findByUserEmail(userEmail) != null;
    }

    @Transactional
    private UserDAO createNewUser(String userEmail, String password) throws UserAlreadyExists {
        if (checkIfUserExists(userEmail)) {
            throw new UserAlreadyExists(userEmail);
        }
        UserDAO newUser = new UserDAO();
        newUser.setUserEmail(userEmail);
        newUser.setUserPassword(passwordEncoder.encode(password));
        newUser.setValidated(false);
        UserDAO addedUserDAO = userRepo.insert(newUser);
        log.info(String.format(
                "New user added to DB. User details: %s",
                addedUserDAO.toString()
        ));
        return addedUserDAO;
    }
}
