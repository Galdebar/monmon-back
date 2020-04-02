package lt.galdebar.monmonmvc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonmvc.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonmvc.persistence.domain.entities.token.LinkUsersTokenEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.UserEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.token.UserEmailChangeTokenEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.token.UserRegistrationTokenEntity;
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
    @Autowired
    private UserRepo userRepo;
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
        UserEntity foundUser = loginUserCheck(loginAttemptDTO);
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

    public UserEntity findByUserEmail(String userEmail) throws UserNotFound {
        UserEntity foundUser = userRepo.findByUserEmail(userEmail);
        if (foundUser == null) {
            throw new UserNotFound(userEmail);
        }
        return foundUser;
    }

    @Transactional
    public void registerNewUser(LoginAttemptDTO registrationAttempt) throws UserAlreadyExists {
        UserEntity newUser = createNewUser(
                registrationAttempt.getUserEmail(),
                registrationAttempt.getUserPassword()
        );
        String token = UUID.randomUUID().toString();
        UserRegistrationTokenEntity tokenDAO = tokenService.createRegistrationToken(newUser, token);
        emailSenderService.sendRegistrationConformationEmail(
                newUser.getUserEmail(),
                token
        );
    }

    @Transactional
    public boolean confirmRegistration(String token) throws TokenNotFound, UserAlreadyValidated, TokenExpired {
        log.info("Validating user. ");
        UserRegistrationTokenEntity registrationToken = tokenService.checkRegistrationToken(token);
        UserEntity userToValidate = registrationToken.getUser();
        userToValidate.setValidated(true);
        return userRepo.save(userToValidate) != null;
    }

    @Transactional
    public void changeUserEmail(EmailChangeRequestDTO emailChangeRequestDTO) throws UserAlreadyExists {
        if (checkIfUserExists(emailChangeRequestDTO.getNewEmail())) {
            throw new UserAlreadyExists(emailChangeRequestDTO.getNewEmail());
        }

        UserEntity currentUser = getCurrentUserDAO();
        String token = UUID.randomUUID().toString();

        UserEmailChangeTokenEntity tokenDAO = tokenService.createEmailChangeToken(currentUser, emailChangeRequestDTO.getNewEmail(), token);
        emailSenderService.sendEmailChangeConfirmationEmail(
                emailChangeRequestDTO.getNewEmail(),
                token
        );
    }

    @Transactional
    public boolean confirmUserEmailChange(String token) throws TokenNotFound, TokenExpired {
        UserEmailChangeTokenEntity tokenDAO = tokenService.checkEmailChangeToken(token);
        UserEntity userToUpdate = tokenDAO.getUser();

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
        UserRegistrationTokenEntity registrationToken = tokenService.renewRegistrationToken(token);
        emailSenderService.sendRegistrationConformationEmail(
                registrationToken.getUser().getUserEmail(),
                registrationToken.getToken()
        );
    }

    @Transactional
    public void changePassword(PasswordChangeRequestDTO passwordChangeRequestDTO) throws BadCredentialsException {
        UserEntity currentUser = getCurrentUserDAO();
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
        UserEntity currentUser = getCurrentUserDAO();
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
        UserEntity currentUserEntity = getCurrentUserDAO();
        UserEntity userToConnectDAO = findByUserEmail(userToConnect.getUserEmail());
        if (!userToConnectDAO.isValidated()) {
            throw new UserNotValidated(userToConnect.getUserEmail());
        }
        if (currentUserEntity.getId().equals(userToConnectDAO.getId())) {
            throw new LinkUsersMatch(currentUserEntity.getUserEmail(), userToConnect.getUserEmail());
        }
        String token = UUID.randomUUID().toString();
        LinkUsersTokenEntity connectionTokenDAO = tokenService.createLinkUsersToken(currentUserEntity, userToConnectDAO, token);

        if (connectionTokenDAO != null) {
            emailSenderService.sendLinkUsersConfirmationEmail(
                    currentUserEntity.getUserEmail(),
                    token
            );
        }
    }

    @Transactional
    public void confirmLinkUsers(String token) throws LinkUsersTokenNotFound, LinkUsersTokenExpired {
        LinkUsersTokenEntity linkUsersTokenEntity = tokenService.checkLinkUsersToken(token);
        log.info(String.format(
                "Linking users. User A: %s| User B: %s",
                linkUsersTokenEntity.getUserA().getUserEmail(),
                linkUsersTokenEntity.getUserB().getUserEmail()
        ));
        linkUsers(
                linkUsersTokenEntity.getUserA(),
                linkUsersTokenEntity.getUserB()
        );
        linkUsers(
                linkUsersTokenEntity.getUserB(),
                linkUsersTokenEntity.getUserA()
        );
    }

    private UserEntity loginUserCheck(LoginAttemptDTO loginAttemptDTO) throws UserNotFound, UserNotValidated {
        log.info("Checking user login credentials. ");
        UserEntity foundUser = userRepo.findByUserEmail(loginAttemptDTO.getUserEmail());
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

    private void linkUsers(UserEntity userA, UserEntity userB) {
        userA.getLinkedUsers().add(userB.getUserEmail());
        userRepo.save(userA);
    }

    public void renewLinkUsersToken(String token) throws LinkUsersTokenExpired, LinkUsersTokenNotFound {
        LinkUsersTokenEntity connectionTokenDAO = tokenService.renewLinkUsersToken(token);
        if (connectionTokenDAO != null) {
            emailSenderService.sendLinkUsersConfirmationEmail(
                    connectionTokenDAO.getUserB().getUserEmail(),
                    token
            );
        }
    }

    @Transactional
    private UserEntity createNewUser(String userEmail, String password) throws UserAlreadyExists {
        if (checkIfUserExists(userEmail)) {
            throw new UserAlreadyExists(userEmail);
        }
        UserEntity newUser = new UserEntity();
        newUser.setUserEmail(userEmail);
        newUser.setUserPassword(passwordEncoder.encode(password));
        newUser.setValidated(false);
        UserEntity addedUserEntity = userRepo.insert(newUser);
        log.info(String.format(
                "New user added to DB. User details: %s",
                addedUserEntity.toString()
        ));
        return addedUserEntity;
    }

    private UserEntity getCurrentUserDAO() {
        return userRepo.findByUserEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    private boolean checkIfUserExists(String userEmail) {
        return userRepo.findByUserEmail(userEmail) != null;
    }
}
