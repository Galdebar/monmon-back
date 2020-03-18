package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonmvc.persistence.domain.dao.token.LinkUsersTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.token.UserEmailChangeTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.token.UserRegistrationTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.*;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.exceptions.connectusers.ConnectUsersTokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.connectusers.ConnectUsersTokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyExists;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotValidated;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyValidated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
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
        UserDAO foundUser = userRepo.findByUserEmail(loginAttemptDTO.getUserEmail());
        if (foundUser == null) {
            throw new UserNotFound();
        }
        if (!foundUser.isValidated()) {
            throw new UserNotValidated();
        }
        if(!passwordEncoder.matches(loginAttemptDTO.getUserPassword(),foundUser.getUserPassword())){
            throw new BadCredentialsException("Invalid password");
        }
        return foundUser;
    }

    public UserDAO findByUserEmail(String userEmail) throws UserNotFound {
        UserDAO foundUser = userRepo.findByUserEmail(userEmail);
        if(foundUser == null){
            throw new UserNotFound();
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
        emailSenderService.sendConfirmationEmail(
                newUser.getUserEmail(),
                token
        );
    }

    @Transactional
    public boolean confirmRegistration(String token) throws TokenNotFound, UserAlreadyValidated, TokenExpired {
        UserRegistrationTokenDAO registrationToken = tokenService.checkRegistrationToken(token);
        UserDAO userToValidate = registrationToken.getUser();
        userToValidate.setValidated(true);
        return userRepo.save(userToValidate) != null;
    }

    @Transactional
    public void changeUserEmail(EmailChangeRequest emailChangeRequest) throws UserAlreadyExists {
        if(checkIfUserExists(emailChangeRequest.getNewEmail())){
            throw new UserAlreadyExists();
        }

        UserDAO currentUser = getCurrentUserDAO();
        String token = UUID.randomUUID().toString();
        UserEmailChangeTokenDAO tokenDAO = tokenService.createEmailChangeToken(currentUser,emailChangeRequest.getNewEmail(),token);
        emailSenderService.sendEmailChangeConfirmationEmail(
                emailChangeRequest.getNewEmail(),
                token
        );
    }

    @Transactional
    public boolean confirmUserEmailChange(String token) throws TokenNotFound, TokenExpired {
        UserEmailChangeTokenDAO tokenDAO = tokenService.checkEmailChangeToken(token);
        UserDAO userToUpdate = tokenDAO.getUser();
        userToUpdate.setUserEmail(tokenDAO.getNewEmail());
        return userRepo.save(userToUpdate) != null;

    }

    @Transactional
    public void renewRegistrationToken(String token) throws UserAlreadyValidated, TokenExpired, TokenNotFound {
        UserRegistrationTokenDAO registrationToken = tokenService.renewRegistrationToken(token);
        emailSenderService.sendConfirmationEmail(
                registrationToken.getUser().getUserEmail(),
                registrationToken.getToken()
        );
    }

    @Transactional
    public void changePassword(PasswordChangeRequest passwordChangeRequest) throws BadCredentialsException {
        UserDAO currentUser = getCurrentUserDAO();
        if(!currentUser.getUserEmail().equals(passwordChangeRequest.getUserEmail())){
            throw new BadCredentialsException("Invalid user email");
        }

        if (passwordEncoder.matches(passwordChangeRequest.getOldPassword(), currentUser.getUserPassword())) {
            currentUser.setUserPassword(
                    passwordEncoder.encode(
                            passwordChangeRequest.getNewPassword()
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
    public void linkUserWithCurrent(UserDTO userToConnect) throws UserNotFound {
        UserDAO currentUserDAO = getCurrentUserDAO();
        UserDAO userToConnectDAO = findByUserEmail(userToConnect.getUserEmail());
        String token = UUID.randomUUID().toString();
        LinkUsersTokenDAO connectionTokenDAO = tokenService.createLinkUsersToken(currentUserDAO, userToConnectDAO);

        if (connectionTokenDAO != null) {
            emailSenderService.sendUserConnectConfirmationEmail(
                    currentUserDAO.getUserEmail(),
                    token
            );
        }
    }

    @Transactional
    public void confirmLinkUsers(String token) throws ConnectUsersTokenNotFound, ConnectUsersTokenExpired {
        LinkUsersTokenDAO linkUsersTokenDAO = tokenService.checkLinkUsersToken(token);
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

    public void renewLinkUsersToken(String token) throws ConnectUsersTokenExpired, ConnectUsersTokenNotFound {
        LinkUsersTokenDAO connectionTokenDAO = tokenService.renewLinkUsersToken(token);
        if (connectionTokenDAO != null) {
            emailSenderService.sendUserConnectConfirmationEmail(
                    connectionTokenDAO.getUserB().getUserEmail(),
                    token
            );
        }
    }

    public UserDAO getCurrentUserDAO() {
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
            throw new UserAlreadyExists();
        }
        UserDAO newUser = new UserDAO();
        newUser.setUserEmail(userEmail);
        newUser.setUserPassword(passwordEncoder.encode(password));
        newUser.setValidated(false);
        UserDAO addedUserDAO = userRepo.insert(newUser);
        return addedUserDAO;
    }
}
