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

    @CrossOrigin
    @GetMapping("/deleteuser")
    ResponseEntity markUserForDeletion(@RequestHeader(name = "Authorization") String token){
        String attemptMessage = String.format(
                "Attempting to mark user for deletion. User: %s",
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        log.info(attemptMessage);

        userService.markUserForDeletion(token);
        String successMessage = String.format(
                "User successfully marked for deletion. The profile will be deleted in %d hours",
                userService.getUSER_DELETION_GRACE_PERIOD()
        );

        log.info(successMessage);
        return ResponseEntity.ok().body(successMessage);
    }

    @CrossOrigin
    @GetMapping("/getlinkedusers")
    ResponseEntity getLinkedUsers() {
        String attemptMessage = "Attempting to get linked users";
        log.info(attemptMessage);

        List<String> linkedUsers = userService.getLinkedUsers();
        log.info("Returning: " + linkedUsers.toString());
        return ResponseEntity.ok(linkedUsers);
    }

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

    private ResponseEntity logAndSendBadRequest(String messageStart, String message){
        log.warn(messageStart + message);
        return ResponseEntity.badRequest().body(message);
    }

    private <T extends Throwable & CanSendResponse> ResponseEntity logAndSendBadRequest(String messageStart, T exception){
        log.warn(messageStart + exception.getMessage());
        return ResponseEntity.badRequest().body(exception.getResponseMessage());
    }

}
