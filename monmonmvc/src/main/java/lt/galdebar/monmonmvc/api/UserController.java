package lt.galdebar.monmonmvc.api;

import lt.galdebar.monmonmvc.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonmvc.persistence.domain.dto.AuthTokenDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.LoginAttemptDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.service.UserRegistrationService;
import lt.galdebar.monmonmvc.service.UserService;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotValidated;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyExists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    private UserRegistrationService registrationService;

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
            throw new BadCredentialsException("Invalid username/password supplied");
        } catch (UserNotValidated userNotValidated) {
            userNotValidated.printStackTrace();
            return ResponseEntity.badRequest().body("User Not Validated. Confirm registration");
        } catch (UserNotFound userNotFound) {
            userNotFound.printStackTrace();
            return ResponseEntity.badRequest().body("User Not Found");
        }
    }

    @PostMapping("/signup")
    ResponseEntity signUp(@RequestBody LoginAttemptDTO loginAttempt) {
        if (loginAttempt != null && isEmailValid(loginAttempt.getUserEmail())) {
            try {
                registrationService.registerNewUser(loginAttempt);
                return ResponseEntity.ok().body("User Created Successfully. Confirmation Email Sent");
            } catch (UserAlreadyExists userAlreadyExists) {
                userAlreadyExists.printStackTrace();
                return ResponseEntity.badRequest().body("User Already Exists");
            }
        } else return ResponseEntity.badRequest().body("Invalid or empty email");
    }

    @GetMapping("/getconnectedusers")
    ResponseEntity getConnectedUsers() {
        return ResponseEntity.ok(userService.getConnectedUsers());
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
