package lt.galdebar.monmonmvc.service;

import lombok.Getter;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles initial login checks, communicates with user Repositories for user CRUD operations.
 * Communicates with Token Service for user registration, token renewal, user link and deletion.
 * Handles password encoding when creating user entry in DB by utilizing an Autowired password Encoder (uses Bcrypt)
 */
@Service
@Log4j2
public class UserService {
    @Getter
    private final int USER_DELETION_GRACE_PERIOD = 48;

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


    /**
     * User credential check and user token generation using jwtTokenProvider. Cancels user deletion if it is set.
     *
     * @param loginAttemptDTO Carries user email and password (non-encrypted)
     * @return Auth token
     * @throws UserNotValidated If user tries to login before confirming registration.
     * @throws UserNotFound     If user not found
     */
    @Transactional
    public AuthTokenDTO login(LoginAttemptDTO loginAttemptDTO) throws UserNotValidated, UserNotFound {
        UserEntity foundUser = loginUserCheck(loginAttemptDTO);
        cancelUserDeletion(foundUser);

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

    /**
     * Find User by email.
     *
     * @param userEmail Email
     * @return Found user. Is never null.
     * @throws UserNotFound If user not found.
     */
    public UserEntity findByUserEmail(String userEmail) throws UserNotFound {
        UserEntity foundUser = userRepo.findByUserEmailIgnoreCase(userEmail);
        if (foundUser == null) {
            throw new UserNotFound(userEmail);
        }
        return foundUser;
    }

    /**
     * Create user entity, save it to DB, request registration token creation and request registration mail send.
     * Encrypts password before sending to Repo.
     *
     * @param registrationAttempt Contains user email and password (non-encrypted)
     * @throws UserAlreadyExists If user already exists (whether fully validated or not)
     */
    @Transactional
    public void registerNewUser(LoginAttemptDTO registrationAttempt) throws UserAlreadyExists {
        UserEntity newUser = createNewUser(
                registrationAttempt.getUserEmail(),
                registrationAttempt.getUserPassword()
        );

        String token = UUID.randomUUID().toString();
        UserRegistrationTokenEntity tokenDAO = tokenService.createRegistrationToken(newUser);
        emailSenderService.sendRegistrationConformationEmail(
                newUser.getUserEmail(),
                tokenDAO.getToken()
        );
    }

    /**
     * Finalize registration. Allows user to log in.
     *
     * @param token the token
     * @return the boolean
     * @throws TokenNotFound        the token not found
     * @throws UserAlreadyValidated the user already validated
     * @throws TokenExpired         the token expired
     */
    @Transactional
    public boolean confirmRegistration(String token) throws TokenNotFound, UserAlreadyValidated, TokenExpired {
        log.info("Validating user. ");
        UserRegistrationTokenEntity registrationToken = tokenService.checkRegistrationToken(token);
        UserEntity userToValidate = registrationToken.getUser();
        userToValidate.setValidated(true);
        return userRepo.save(userToValidate) != null;
    }

    /**
     * Calls for user email change token creation. Calls for email change confirmation email to be sent.
     * Doesn't actually change the user Email untill confirmed.
     *
     * @param emailChangeRequestDTO contains new email
     * @throws UserAlreadyExists email address taken
     */
    @Transactional
    public void changeUserEmail(EmailChangeRequestDTO emailChangeRequestDTO) throws UserAlreadyExists {
        if (checkIfUserExists(emailChangeRequestDTO.getNewEmail())) {
            throw new UserAlreadyExists(emailChangeRequestDTO.getNewEmail());
        }

        UserEntity currentUser = getCurrentUserEntity();

        UserEmailChangeTokenEntity tokenDAO = tokenService.createEmailChangeToken(currentUser, emailChangeRequestDTO.getNewEmail());
        emailSenderService.sendEmailChangeConfirmationEmail(
                emailChangeRequestDTO.getNewEmail(),
                tokenDAO.getToken()
        );
    }

    /**
     * Finalize user email change and save change to Repo.
     *
     * @param token the token
     * @return the boolean
     * @throws TokenNotFound the token not found
     * @throws TokenExpired  the token expired
     */
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

    /**
     * Renew user registration token.
     *
     * @param token the token
     * @throws UserAlreadyValidated the user already validated
     * @throws TokenNotExpired      the token not expired
     * @throws TokenNotFound        the token not found
     */
    @Transactional
    public void renewRegistrationToken(String token) throws UserAlreadyValidated, TokenNotExpired, TokenNotFound {
        UserRegistrationTokenEntity registrationToken = tokenService.renewRegistrationToken(token);
        emailSenderService.sendRegistrationConformationEmail(
                registrationToken.getUser().getUserEmail(),
                registrationToken.getToken()
        );
    }

    /**
     * Change password.
     *
     * @param passwordChangeRequestDTO Contains user email (for additional validation), old and new password.
     * @throws BadCredentialsException if old password is incorrect
     */
    @Transactional
    public void changePassword(PasswordChangeRequestDTO passwordChangeRequestDTO) throws BadCredentialsException {
        UserEntity currentUser = getCurrentUserEntity();
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

    /**
     * Mark user for deletion, set grace period. Calls for email warning to be sent.
     *
     */
    public void markUserForDeletion() {
        UserEntity user = getCurrentUserEntity();
        if (user.isToBeDeleted()) {
            return;
        }
        user.setToBeDeleted(true);
        user.setDeletionDate(getUserDeletionDate());
        userRepo.save(user);
        emailSenderService.sendDeletionWarningEmail(user.getUserEmail(), USER_DELETION_GRACE_PERIOD);
    }

    /**
     * Gets all users pending deletion. Checks user's toBeDeleted = true and if deletion date is before the method call.
     * This method is called by Task scheduler.
     *
     * @return the users pending deletion
     */
    public List<UserDTO> getUsersPendingDeletion() {
        List<UserEntity> usersToBeDeleted = userRepo.findByToBeDeleted(true).stream()
                .filter(user -> user.getDeletionDate().before(new Date()))
                .collect(Collectors.toList());
        return entitiesToDTOs(usersToBeDeleted);
    }

    /**
     * Delete users. Final deletion from DB.
     *
     * @param userDTOS list of users to be deleted
     */
    @Transactional
    public void deleteUsers(List<UserDTO> userDTOS) {
        List<String> userEmails = new ArrayList<>();
        for (UserDTO userDTO : userDTOS) {
            userEmails.add(userDTO.getUserEmail());
        }
        List<UserEntity> usersToDelete = userRepo.findByUserEmailIn(userEmails);

        userRepo.deleteAll(usersToDelete);

        for(String email: userEmails){
            emailSenderService.sendDeletionConfirmationEmail(email);
        }
    }


    /**
     * Gets linked users of the currently logged in user.
     *
     * @return String list of found users. Can be empty, is never null.
     */
    public List<String> getLinkedUsers() {
        UserEntity currentUser = getCurrentUserEntity();
        List<String> connectedUserNames = new ArrayList<>();

        for (String userName : currentUser.getLinkedUsers()) {
            if (userName != null) {
                connectedUserNames.add(userName);
            }
        }

        return connectedUserNames;
    }

    /**
     * Link another user with current. Doesn't save any changes to DB (need confirmation for that).
     * Calls for email to be sent to the other user to confirm user link.
     *
     * @param userToConnect the user to connect
     * @throws UserNotFound     the user not found
     * @throws UserNotValidated the user not validated
     * @throws LinkUsersMatch   current user and user to connect are the same
     */
    @Transactional
    public void linkUserWithCurrent(UserDTO userToConnect) throws UserNotFound, UserNotValidated, LinkUsersMatch {
        UserEntity currentUserEntity = getCurrentUserEntity();
        UserEntity userToConnectDAO = findByUserEmail(userToConnect.getUserEmail());
        if (!userToConnectDAO.isValidated()) {
            throw new UserNotValidated(userToConnect.getUserEmail());
        }
        if (currentUserEntity.getId().equals(userToConnectDAO.getId())) {
            throw new LinkUsersMatch(currentUserEntity.getUserEmail(), userToConnect.getUserEmail());
        }
        LinkUsersTokenEntity connectionTokenDAO = tokenService.createLinkUsersToken(currentUserEntity, userToConnectDAO);

        if (connectionTokenDAO != null) {
            emailSenderService.sendLinkUsersConfirmationEmail(
                    currentUserEntity.getUserEmail(),
                    connectionTokenDAO.getToken()
            );
        }
    }

    /**
     * Finalize user link. Saves changes for both users to DB.
     *
     * @param token the token
     * @throws LinkUsersTokenNotFound the link users token not found
     * @throws LinkUsersTokenExpired  the link users token expired
     */
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

    /**
     * Renew link users token.
     *
     * @param token the token
     * @throws LinkUsersTokenExpired  the link users token expired
     * @throws LinkUsersTokenNotFound the link users token not found
     */
    public void renewLinkUsersToken(String token) throws LinkUsersTokenExpired, LinkUsersTokenNotFound {
        LinkUsersTokenEntity connectionTokenDAO = tokenService.renewLinkUsersToken(token);
        if (connectionTokenDAO != null) {
            emailSenderService.sendLinkUsersConfirmationEmail(
                    connectionTokenDAO.getUserB().getUserEmail(),
                    token
            );
        }
    }

    /**
     * Unlink users.
     *
     * @param userA the user a
     * @param userB the user b
     */
    @Transactional
    public void unlinkUsers(UserDTO userA, UserDTO userB) {
        UserEntity userAEntity = userRepo.findByUserEmailIgnoreCase(userA.getUserEmail());
        UserEntity userBEntity = userRepo.findByUserEmailIgnoreCase(userB.getUserEmail());

        unlinkUsers(userAEntity, userBEntity);
        unlinkUsers(userBEntity, userAEntity);
    }

    /**
     * Check if user is pending deletion.
     *
     * @param subject user email.
     * @return the boolean
     */
    public boolean checkIfUserIsPendingDeletion(String subject) {
        UserEntity user = userRepo.findByUserEmailIgnoreCase(subject);
        return user.isToBeDeleted();
    }

    private UserEntity loginUserCheck(LoginAttemptDTO loginAttemptDTO) throws UserNotFound, UserNotValidated {
        log.info("Checking user login credentials. ");
        UserEntity foundUser = userRepo.findByUserEmailIgnoreCase(loginAttemptDTO.getUserEmail());
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

    private void cancelUserDeletion(UserEntity user) {
        if (user.isToBeDeleted()) {
            user.setToBeDeleted(false);
            userRepo.save(user);
        }
    }

    private void linkUsers(UserEntity userA, UserEntity userB) {
        userA.getLinkedUsers().add(userB.getUserEmail());
        userRepo.save(userA);
    }

    private void unlinkUsers(UserEntity userA, UserEntity userB) {
        userA.getLinkedUsers().remove(userB.getUserEmail());
        userRepo.save(userA);
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
        newUser.setToBeDeleted(false); // Might want to change this later on to autodelete not confirmed users

        UserEntity addedUserEntity = userRepo.insert(newUser);
        log.info(String.format(
                "New user added to DB. User details: %s",
                addedUserEntity.toString()
        ));
        return addedUserEntity;
    }

    private UserEntity getCurrentUserEntity() {
        return userRepo.findByUserEmailIgnoreCase(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    private boolean checkIfUserExists(String userEmail) {
        return userRepo.findByUserEmailIgnoreCase(userEmail) != null;
    }

    private List<UserDTO> entitiesToDTOs(List<UserEntity> userEntities) {
        List<UserDTO> userDTOS = new ArrayList<>();

        for (UserEntity entity : userEntities) {
            userDTOS.add(entityToDTO(entity));
        }

        return userDTOS;
    }

    private UserDTO entityToDTO(UserEntity entity) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserEmail(entity.getUserEmail());
        userDTO.setUserPassword(entity.getUserPassword());

        return userDTO;
    }

    private Date getUserDeletionDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Timestamp(calendar.getTime().getTime()));
        calendar.add(Calendar.HOUR, USER_DELETION_GRACE_PERIOD);
        return new Date(calendar.getTime().getTime());
    }
}
