package lt.galdebar.monmonmvc.api;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonmvc.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonmvc.persistence.domain.dto.*;
import lt.galdebar.monmonmvc.service.UserService;
import lt.galdebar.monmonmvc.service.exceptions.CanSendResponse;
import lt.galdebar.monmonmvc.service.exceptions.linkusers.LinkUsersMatch;
import lt.galdebar.monmonmvc.service.exceptions.linkusers.LinkUsersTokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.linkusers.LinkUsersTokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotValidated;
import lt.galdebar.monmonmvc.service.exceptions.registration.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;


/**
 * User Controller
 * Used for user registration and login.
 * Also for handling user email and password change as well as linking two user accounts together.
 */
@RestController
@RequestMapping("/user")
@Log4j2
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;


    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    /**
     * Get current user details.<br>
     * <strong>GET request</strong><br>
     *
     * <strong>Requires valid Authorization Token in request header</strong><br>
     * Header format:<br>
     * {"Authorization":"Bearer [token]"}<br>
     *
     * @param userDetails UserDetails after Authorization token has been validated. Taken from the header.
     * @return current user name and roles.<br>
     * Returns <strong>HTTP 403</strong> if Authorization token invalid.
     */
    @GetMapping("/me")
    public ResponseEntity currentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Attempting to get current user. ");

        Map<Object, Object> model = new HashMap<>();
        model.put("username", userDetails.getUsername());
        model.put("roles", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(toList())
        );
        log.info(String.format("Returned user: %s.", model.toString()));
        return ResponseEntity.ok(model);
    }

    /**
     * Login.<br>
     * <strong>POST request</strong><br>
     *
     *
     * @param loginAttemptDTO containing <strong>User Email</strong> and <strong>User Password</strong><br>
     * Login attempt request <strong>JSON</strong> example:<br>
     *
     *   {<br>
     *     "userEmail": "some@email.com",<br>
     *     "userPassword": "This15APassw0rd",<br>
     *   }<br>
     * @return User Email and a valid Authorization Token.<br>
     * Returns <strong>HTTP 400</strong> if
     * <ul>
     *     <li>Email format is empty or invalid format</li>
     *     <li>User email not found.</li>
     *     <li>Invalid password</li>
     * </ul>
     */
    @CrossOrigin
    @PostMapping("/login")
    ResponseEntity login(@RequestBody LoginAttemptDTO loginAttemptDTO) {
        String failMessageStart = "Login attempt failed! ";
        log.info("Login attempt. User: " + loginAttemptDTO.getUserEmail());

        if (!isEmailValid(loginAttemptDTO.getUserEmail())) {
            log.warn(failMessageStart + String.format("Invalid email: %s", loginAttemptDTO.getUserEmail()));
            return ResponseEntity.badRequest().body("Invalid Email");
        }
        try {
            AuthTokenDTO receivedToken = userService.login(loginAttemptDTO);
            Map<Object, Object> responseObj = new HashMap<>();
            responseObj.put("userEmail", receivedToken.getUserEmail());
            responseObj.put("token", receivedToken.getToken());
            log.info("Login successful.");
            return ResponseEntity.ok(responseObj);
        } catch (AuthenticationException e) {
            log.warn(failMessageStart + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UserNotValidated | UserNotFound exception) {
            return logAndSendBadRequest(failMessageStart, exception);
        }
    }

    /**
     * Register new User.<br>
     * <strong>GET request</strong><br>
     *
     * Only begins the registration process.<br>
     * Creates the user entry in the Database and sends a registration confirmation email to the supplied address.<br>
     *
     *
     * @param loginAttempt containing <strong>User Email</strong> and <strong>User Password</strong><br>
     * Login attempt request <strong>JSON</strong> example:<br>
     *
     *   {<br>
     *     "userEmail": "some@email.com",<br>
     *     "userPassword": "This15APassw0rd",<br>
     *   }<br>
     * @return HTTP OK response if successful.<br>
     * Returns <strong>HTTP 400</strong> if
     * <ul>
     *     <li>Email format is empty or invalid format</li>
     *     <li>User email already taken</li>
     *     <li>Empty password</li>
     * </ul>
     */
    @CrossOrigin
    @PostMapping("/register")
    ResponseEntity signUp(@RequestBody LoginAttemptDTO loginAttempt) {
        String failMessageStart = "User registration failed! ";
        log.info("User registration attempt: " + loginAttempt.getUserEmail());

        if (!isEmailValid(loginAttempt.getUserEmail())) {
            return logAndSendBadRequest(failMessageStart, "Invalid email. ");
        }
        if (loginAttempt.getUserPassword().trim().isEmpty()) {
            return logAndSendBadRequest(failMessageStart, "Password empty. ");
        }
        try {
            String message = "Registration successful. Confirmation email sent.";
            userService.registerNewUser(loginAttempt);
            log.info(message);
            return ResponseEntity.ok().body(message);
        } catch (UserAlreadyExists userAlreadyExists) {
            return logAndSendBadRequest(failMessageStart, userAlreadyExists);
        }
    }

    /**
     * Confirm user registration.<br>
     * <strong>GET request</strong><br>
     *
     * Finalizes the registration process. User is allowed to login after this step.<br>
     *
     * @param token the registration confirmation token.
     * @return HTTP OK response if successful.<br>
     * Returns <strong>HTTP 400</strong> if
     * <ul>
     *     <li>Token is empty</li>
     *     <li>Token invalid</li>
     *     <li>Token expired</li>
     * </ul>
     */
    @CrossOrigin
    @GetMapping(value = "register/confirm/{token}")
    ResponseEntity validateUser(@PathVariable String token) {
        String failMessageStart = "User registration confirm failed! ";
        log.info("Attempting to confirm user registration. Token: " + token);

        if (token.trim().isEmpty()) {
            return logAndSendBadRequest(failMessageStart, "No token. ");
        }

        try {
            boolean isRegistrationSuccessful = userService.confirmRegistration(token);
            if (isRegistrationSuccessful) {
                String message = "Registration confirmed successfully";
                log.info(message);
                return ResponseEntity.ok(message);
            } else {
                String message = "Registration confirmation unsuccessful. ";
                log.error(message);
                return ResponseEntity.badRequest().body(message + "Internal error. ");
            }
        } catch (TokenNotFound | TokenExpired | UserAlreadyValidated exception) {
            return logAndSendBadRequest(failMessageStart, exception);
        }
    }

    /**
     * Renew user registration token.<br>
     * <strong>GET request</strong><br>
     *
     * @param token the token
     * @return HTTP OK response if successful.<br>
     * Returns <strong>HTTP 400</strong> if
     * <ul>
     *     <li>Token is empty</li>
     *     <li>Token invalid</li>
     *     <li>Token not expired yet</li>
     * </ul>
     */
    @CrossOrigin
    @GetMapping(path = "/register/renew/{token}")
    ResponseEntity requestNewToken(@PathVariable String token) {
        String failMessageStart = "Registration token renewal failed! ";
        log.info("Attempting registration token renewal. TokenID: " + token);

        if (token.isEmpty()) {
            return logAndSendBadRequest(failMessageStart, "No token. ");
        }

        try {
            String message = "Success. Registration token renewed. ";
            userService.renewRegistrationToken(token);
            log.info(message);
            return ResponseEntity.ok(message);
        } catch (UserAlreadyValidated | TokenNotExpired | TokenNotFound exception) {
            return logAndSendBadRequest(failMessageStart, exception);
        }
    }

    /**
     * Change current user Email.<br>
     * <strong>POST request</strong><br>
     *
     * User can still login with the old email, until email change is confirmed.<br>
     * <strong>Requires valid Authorization Token in request header</strong><br>
     * Header format:<br>
     * {"Authorization":"Bearer [token]"}<br>
     *
     *
     * @param emailChangeRequestDTO contains new Email Address.<br>
     * Email change request <strong>JSON</strong> example:<br>
     *
     *   {<br>
     *     "newEmail": "some@email.com",<br>
     *   }<br>
     * @return HTTP OK response if successful. Also sends confirmation email to the supplied new address.<br>
     * Returns <strong>HTTP 400</strong> if
     * <ul>
     *     <li>Email empty or invalid format</li>
     *     <li>Email already taken</li>
     *     <li>Token not expired yet</li>
     * </ul><br>
     * Returns <strong>HTTP 403</strong> if Authorization token is
     * <ul>
     *      <li>Empty</li>
     *      <li>Invalid</li>
     *      <li>Expired</li>
     * </ul>
     */
    @CrossOrigin
    @PostMapping("/changeemail")
    ResponseEntity changeEmail(@RequestBody EmailChangeRequestDTO emailChangeRequestDTO) {
        String failMessageStart = "Change email failed! ";
        log.info(String.format("Attempting email change. New email: %s. ", emailChangeRequestDTO.getNewEmail()));

        if (!isEmailValid(emailChangeRequestDTO.getNewEmail())) {
            return logAndSendBadRequest(failMessageStart, "Invalid email. ");
        }

        try {
            userService.changeUserEmail(emailChangeRequestDTO);
            String message = "Email change successful. ";
            log.info(message);
            return ResponseEntity.ok().body(message);
        } catch (UserAlreadyExists userAlreadyExists) {
            return logAndSendBadRequest(failMessageStart, userAlreadyExists);

        }
    }

    /**
     * Confirm user email change.<br>
     * <strong>GET request</strong><br>
     *
     * @param token the token
     * @return HTTP OK response if successful.<br>
     * Returns <strong>HTTP 400</strong> if
     * <ul>
     *     <li>Token is empty</li>
     *     <li>Token invalid</li>
     *     <li>Token not expired yet</li>
     * </ul>
     */
    @CrossOrigin
    @GetMapping("/changeemail/confirm/{token}")
    ResponseEntity confirmEmailChange(@PathVariable String token) {
        String failMessageStart = "Email change confirmation failed! ";
        log.info("Attempting email change confirmation. TokenID: " + token);

        if (token.isEmpty()) {
            return logAndSendBadRequest(failMessageStart, "Invalid token. ");
        }

        try {
            userService.confirmUserEmailChange(token);
            String message = "Email change confirmed success. ";
            log.info(message);
            return ResponseEntity.ok().body(message);
        } catch (TokenNotFound | TokenExpired exception) {
            return logAndSendBadRequest(failMessageStart, exception);
        }
    }


    /**
     * Change current user Password.<br>
     * <strong>POST request</strong><br>
     *
     * <strong>Requires valid Authorization Token in request header</strong><br>
     * Header format:<br>
     * {"Authorization":"Bearer [token]"}<br>
     *
     *
     * @param passwordChangeRequestDTO contains current user Email Address (for verification), old password and new password.<br>
     * Password change request <strong>JSON</strong> example:<br>
     *
     *   {<br>
     *     "userEmail": "some@email.com",<br>
     *     "oldPassword": "This15APassw0rd",<br>
     *     "newPassword": "5omeNewPassword",<br>
     *   }<br>
     * @return HTTP OK response if successful. Also sends confirmation email to the supplied address.<br>
     * Returns <strong>HTTP 400</strong> if
     * <ul>
     *     <li>Email empty or invalid format</li>
     *     <li>Email doesn't match current user</li>
     *     <li>Password empty</li>
     *     <li>Old and new passwords match</li>
     *     <li>Old password is incorrect</li>
     * </ul><br>
     * Returns <strong>HTTP 403</strong> if Authorization token is
     * <ul>
     *      <li>Empty</li>
     *      <li>Invalid</li>
     *      <li>Expired</li>
     * </ul>
     */
    @CrossOrigin
    @PostMapping("/changepassword")
    ResponseEntity changePassword(@RequestBody PasswordChangeRequestDTO passwordChangeRequestDTO) {
        String failMessageStart = "Change password failed! ";
        log.info(String.format(
                "Password change attempt. User: %s. Old: %s, New: %s",
                passwordChangeRequestDTO.getUserEmail(),
                passwordChangeRequestDTO.getOldPassword(),
                passwordChangeRequestDTO.getNewPassword()
        ));

        if (!isEmailValid(passwordChangeRequestDTO.getUserEmail())) {
            return logAndSendBadRequest(failMessageStart, "Invalid email. ");
        }
        if (passwordChangeRequestDTO.getNewPassword().trim().isEmpty()) {
            return logAndSendBadRequest(failMessageStart, "Invalid new password. ");
        }
        if (passwordChangeRequestDTO.getOldPassword().equals(passwordChangeRequestDTO.getNewPassword())) {
            String message = "Old and new passwords match. ";
            log.warn(failMessageStart + message);
            return ResponseEntity.badRequest().body(message);
        }
        try {
            userService.changePassword(passwordChangeRequestDTO);
            String message = "Password change successful";
            log.info(message);
            return ResponseEntity.ok().body(message);
        } catch (AuthenticationException e) {
            log.warn(failMessageStart + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Request delete current user.<br>
     * <strong>GET request</strong><br>
     *
     * Only begins the registration process.
     * Process finishes after time set in UserService.
     * If the user logs in within the grace period, deletion is cancelled.<br>
     *
     * <strong>Requires valid Authorization Token in request header</strong><br>
     * Header format:<br>
     * {Authorization:Bearer [token]}<br>
     *
     * @return HTTP OK response if successful. Also sends warning email to the current user email.<br>
     * Returns <strong>HTTP 403</strong> if Authorization token is
     * <ul>
     *      <li>Empty</li>
     *      <li>Invalid</li>
     *      <li>Expired</li>
     * </ul>
     */
    @CrossOrigin
    @DeleteMapping("/deleteuser")
    ResponseEntity markUserForDeletion() {
        String attemptMessage = String.format(
                "Attempting to mark user for deletion. User: %s",
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        log.info(attemptMessage);

        userService.markUserForDeletion();
        String successMessage = String.format(
                "User successfully marked for deletion. The profile will be deleted in %d hours",
                userService.getUSER_DELETION_GRACE_PERIOD()
        );

        log.info(successMessage);
        return ResponseEntity.ok().body(successMessage);
    }

    /**
     * Gets linked users.<br>
     * <strong>GET request</strong><br>
     *
     *
     * <strong>Requires valid Authorization Token in request header</strong><br>
     * Header format:<br>
     * {Authorization:Bearer [token]}<br>
     *
     * @return List (String) of users linked with the current user.
     */
    @CrossOrigin
    @GetMapping("/getlinkedusers")
    ResponseEntity getLinkedUsers() {
        String attemptMessage = "Attempting to get linked users";
        log.info(attemptMessage);

        List<String> linkedUsers = userService.getLinkedUsers();
        log.info("Returning: " + linkedUsers.toString());
        return ResponseEntity.ok(linkedUsers);
    }

    /**
     * Link users request.<br>
     * <strong>POST request</strong><br>
     *
     * <strong>Requires valid Authorization Token in request header</strong><br>
     * Header format:<br>
     * {"Authorization":"Bearer [token]"}<br>
     *
     *
     * @param userDTO containing email of the other user (other fields can be empty).<br>
     * Full User <strong>JSON</strong> example:<br>
     *
     *   {<br>
     *     "userEmail": "some@email.com",<br>
     *     "userPassword": "This15APassw0rd",<br>
     *     "linkedUsers": "["someother@mail.com", "another@mail.this"]",<br>
     *   }<br>
     * @return HTTP OK response if successful. Also sends confirmation link to the user in userDTO.<br>
     * Returns <strong>HTTP 400</strong> if other user email
     * <ul>
     *      <li>Is empty or invalid format</li>
     *      <li>isn't found in users DB</li>
     *      <li>matches the current user</li>
     * </ul>
     * Returns <strong>HTTP 403</strong> if Authorization token is
     * <ul>
     *      <li>Empty</li>
     *      <li>Invalid</li>
     *      <li>Expired</li>
     * </ul>
     */
    @CrossOrigin
    @PostMapping("/link")
    ResponseEntity linkUsers(@RequestBody UserDTO userDTO) {
        String failMessageStart = "Link users failed! ";
        log.info(String.format(
                "Attempting to link users: Current user: %s | User to link: %s ",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                userDTO.getUserEmail()
        ));

        if (!isEmailValid(userDTO.getUserEmail())) {
            return logAndSendBadRequest(failMessageStart, "Invalid user email. ");
        }
        try {
            userService.linkUserWithCurrent(userDTO);
            String message = "Success! Connection request sent. ";
            log.info(message);
            return ResponseEntity.ok(message);
        } catch (UserNotFound | UserNotValidated | LinkUsersMatch exception) {
            return logAndSendBadRequest(failMessageStart, exception);
        }
    }

    /**
     * Confirm link users response entity.<br>
     * <strong>GET request</strong><br>
     *
     *
     * @param token the token
     * @return HTTO Ok response if successful<br>
     * Returns <strong>HTTP 400</strong> if token is
     * <ul>
     *      <li>Empty</li>
     *      <li>Invalid</li>
     *      <li>Expired</li>
     * </ul>
     */
    @CrossOrigin
    @GetMapping("link/confirm/{token}")
    ResponseEntity confirmLinkUsers(@PathVariable String token) {
        String failMessageStart = "Link users confirmation failed! ";
        log.info("Attempting to confirm link users. Token ID: " + token);

        if (token.isEmpty()) {
            return logAndSendBadRequest(failMessageStart, "No token. ");
        }

        try {
            userService.confirmLinkUsers(token);
            String message = "User link confirmation successful. ";
            log.info(message);
            return ResponseEntity.ok(message);
        } catch (LinkUsersTokenNotFound | LinkUsersTokenExpired exception) {
            return logAndSendBadRequest(failMessageStart, exception);
        }
    }

    /**
     * Renew link users token.<br>
     * <strong>GET request</strong><br>
     *
     * @param token the token
     * @return HTTP Ok response<br>
     * Returns <strong>HTTP 400</strong> if
     * <ul>
     *     <li>Token is empty</li>
     *     <li>Token invalid</li>
     *     <li>Token not expired yet</li>
     * </ul>
     */
    @CrossOrigin
    @GetMapping("link/renew/{token}")
    ResponseEntity renewLinkUsersToken(@PathVariable String token) {
        String failMessageStart = "Link users token renew failed! ";
        log.info("Attempting to renew confirm link users token. Token ID: " + token);

        if (token.isEmpty()) {
            return logAndSendBadRequest(failMessageStart, "No token. ");
        }

        try {
            userService.renewLinkUsersToken(token);
            String message = "Success. New link users request sent. ";
            log.warn(message);
            return ResponseEntity.ok(message);
        } catch (LinkUsersTokenExpired | LinkUsersTokenNotFound exception) {
            return logAndSendBadRequest(failMessageStart, exception);
        }
    }

    private boolean isEmailValid(String userEmail) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (userEmail == null)
            return false;
        return pat.matcher(userEmail).matches();
    }

    private ResponseEntity logAndSendBadRequest(String messageStart, String message) {
        log.warn(messageStart + message);
        return ResponseEntity.badRequest().body(message);
    }

    private <T extends Throwable & CanSendResponse> ResponseEntity logAndSendBadRequest(String messageStart, T exception) {
        log.warn(messageStart + exception.getMessage());
        return ResponseEntity.badRequest().body(exception.getResponseMessage());
    }

}
