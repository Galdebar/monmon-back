package lt.galdebar.monmonmvc.api;

import lt.galdebar.monmonmvc.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonmvc.persistence.domain.dto.*;
import lt.galdebar.monmonmvc.service.UserService;
import lt.galdebar.monmonmvc.service.exceptions.connectusers.ConnectUsersTokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.connectusers.ConnectUsersTokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotValidated;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyExists;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyValidated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;


    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    @GetMapping("/me")
    public ResponseEntity currentUser(@AuthenticationPrincipal UserDetails userDetails) {
        Map<Object, Object> model = new HashMap<>();
        model.put("username", userDetails.getUsername());
        model.put("roles", userDetails.getAuthorities()
                .stream()
                .map(item -> ((GrantedAuthority) item).getAuthority())
                .collect(toList())
        );
        return ResponseEntity.ok(model);
    }

    @CrossOrigin
    @PostMapping("/login")
    ResponseEntity login(@RequestBody LoginAttemptDTO loginAttemptDTO) {
        if (!isEmailValid(loginAttemptDTO.getUserEmail())) {
            return ResponseEntity.badRequest().body("Invalid Email");
        }
        try {
            AuthTokenDTO receivedToken = userService.login(loginAttemptDTO);
            Map<Object, Object> model = new HashMap<>();
            model.put("userEmail", receivedToken.getUserEmail());
            model.put("token", receivedToken.getToken());
            return ResponseEntity.ok(model);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UserNotValidated userNotValidated) {
            return ResponseEntity.badRequest().body("User Not Validated. Confirm registration");
        } catch (UserNotFound userNotFound) {
            return ResponseEntity.badRequest().body("User Not Found");
        }
    }

    @CrossOrigin
    @PostMapping("/register")
    ResponseEntity signUp(@RequestBody LoginAttemptDTO loginAttempt) {
        if (loginAttempt != null && isEmailValid(loginAttempt.getUserEmail())) {
            try {
                userService.registerNewUser(loginAttempt);
                return ResponseEntity.ok().body("Success");
            } catch (UserAlreadyExists userAlreadyExists) {
                return ResponseEntity.badRequest().body("User Already Exists");
            }
        } else return ResponseEntity.badRequest().body("Invalid Email");
    }

    @CrossOrigin
    @GetMapping(value = "register/confirm/{token}")
    ResponseEntity validateUser(@PathVariable String token) {
        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body("No Token");
        }

        boolean isRegistrationSuccessful;

        try {
            isRegistrationSuccessful = userService.confirmRegistration(token);
            if (isRegistrationSuccessful) {
                return ResponseEntity.ok("Registration successful");
            } else return ResponseEntity.badRequest().body("Something went wrong");
        } catch (TokenNotFound tokenNotFound) {
            return ResponseEntity.badRequest().body("Incorrect token");
        } catch (UserAlreadyValidated userAlreadyValidated) {
            return ResponseEntity.badRequest().body("User already validated");
        } catch (TokenExpired tokenExpired) {
            return ResponseEntity.badRequest().body("Token expired");
        }
    }

    @CrossOrigin
    @GetMapping(path = "/register/renew/{token}")
    ResponseEntity requestNewToken(@PathVariable String token) {
        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body("No Token");
        }

        try {
            userService.renewRegistrationToken(token);
            return ResponseEntity.ok("Success");

        } catch (UserAlreadyValidated userAlreadyValidated) {
            return ResponseEntity.badRequest().body("User already validated");
        } catch (TokenNotFound tokenNotFound) {
            return ResponseEntity.badRequest().body("Incorrect token");
        } catch (TokenExpired tokenExpired) {
            return ResponseEntity.badRequest().body("Token expired");
        }
    }

    @CrossOrigin
    @PostMapping("/changeemail")
    ResponseEntity changeEmail(@RequestBody EmailChangeRequest emailChangeRequest) {
        if (emailChangeRequest == null || !isEmailValid(emailChangeRequest.getNewEmail())) {
            return ResponseEntity.badRequest().body("Invalid Email");
        }

        try {
            userService.changeUserEmail(emailChangeRequest);
            return ResponseEntity.ok().body("Success");
        } catch (UserAlreadyExists userAlreadyExists) {
            return ResponseEntity.badRequest().body("Email is already taken");
        }
    }

    @CrossOrigin
    @GetMapping("/changeemail/confirm/{token}")
    ResponseEntity confirmEmailChange(@PathVariable String token){
        if(token.isEmpty()){
            return ResponseEntity.badRequest().body("Invalid token");
        }

        try {
            userService.confirmUserEmailChange(token);
            return ResponseEntity.ok().body("Success");
        } catch (TokenNotFound tokenNotFound) {

            return ResponseEntity.badRequest().body("Token not found");
        } catch (TokenExpired tokenExpired) {
            return ResponseEntity.badRequest().body("Token Expired");
        }
    }


    @CrossOrigin
    @PostMapping("/changepassword")
    ResponseEntity changePassword(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        if (passwordChangeRequest == null) {
            return ResponseEntity.badRequest().body("Invalid Request");
        }
        try {
            userService.changePassword(passwordChangeRequest);
            return ResponseEntity.ok().body("Success");
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping("/getlinkedusers")
    ResponseEntity getLinkedUsers() {
        return ResponseEntity.ok(userService.getLinkedUsers());
    }

    @CrossOrigin
    @PostMapping("/link")
    ResponseEntity linkUsers(@RequestBody UserDTO userDTO) {
        if (userDTO == null || !isEmailValid(userDTO.getUserEmail())) {
            return ResponseEntity.badRequest().body("Invalid User Email");
        }
        try {
            userService.linkUserWithCurrent(userDTO);
            return ResponseEntity.ok("Connection request sent");
        } catch (UserNotFound userNotFound) {
            return ResponseEntity.badRequest().body("Such User doesn't exist");
        }
    }

    @CrossOrigin
    @GetMapping("link/confirm/{token}")
    ResponseEntity confirmLinkUsers(@PathVariable String token) {
        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body("No Token");
        }

        try {
            userService.confirmLinkUsers(token);
            return ResponseEntity.ok("Success");
        } catch (ConnectUsersTokenNotFound connectUsersTokenNotFound) {
            return ResponseEntity.badRequest().body("Token Not Found");
        } catch (ConnectUsersTokenExpired connectUsersTokenExpired) {
            return ResponseEntity.badRequest().body("Token Expired, Send request again");
        }
    }

    @CrossOrigin
    @GetMapping("link/renew/{token}")
    ResponseEntity renewLinkUsersToken(@PathVariable String token) {
        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body("No Token");
        }

        try {
            userService.renewLinkUsersToken(token);
            return ResponseEntity.ok("Success");
        } catch (ConnectUsersTokenExpired connectUsersTokenExpired) {
            return ResponseEntity.badRequest().body("Token Expired, Send request again");
        } catch (ConnectUsersTokenNotFound connectUsersTokenNotFound) {
            return ResponseEntity.badRequest().body("Token Not Found");
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

}
