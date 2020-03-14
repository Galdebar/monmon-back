package lt.galdebar.monmonmvc.api;

import lt.galdebar.monmonmvc.persistence.domain.dto.EmailChangeRequest;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.service.UserRegistrationService;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyExists;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyValidated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@Controller
@RequestMapping("/register")
public class UserRegistrationController {
    @Autowired
    private UserRegistrationService registrationService;


    @CrossOrigin
    @GetMapping(value = "/confirm/{token}")
    ResponseEntity validateUser(@PathVariable String token) {
        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body("No Token");
        }

        boolean isRegistrationSuccessful;

        try {
            isRegistrationSuccessful = registrationService.confirmRegistration(token);
            if (isRegistrationSuccessful) {
                return ResponseEntity.ok("Registration successful");
            } else return ResponseEntity.badRequest().body("Something went wrong");
        } catch (TokenNotFound tokenNotFound) {
//            tokenNotFound.printStackTrace();
            return ResponseEntity.badRequest().body("Incorrect token");
        } catch (UserAlreadyValidated userAlreadyValidated) {
//            userAlreadyValidated.printStackTrace();
            return ResponseEntity.badRequest().body("User already validated");
        } catch (TokenExpired tokenExpired) {
//            tokenExpired.printStackTrace();
            return ResponseEntity.badRequest().body("Token expired");
        }
    }

    @CrossOrigin
    @GetMapping(path = "/requestnewtoken/{token}")
    ResponseEntity requestNewToken(@PathVariable String token) {
        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body("No Token");
        }

        try {
            registrationService.extendTokenDuration(token);
            return ResponseEntity.ok("Success");

        } catch (UserAlreadyValidated userAlreadyValidated) {
//            userAlreadyValidated.printStackTrace();
            return ResponseEntity.badRequest().body("User already validated");
        } catch (TokenNotFound tokenNotFound) {
//            tokenNotFound.printStackTrace();
            return ResponseEntity.badRequest().body("Incorrect token");
        }
    }

    @CrossOrigin
    @PostMapping("/changeemail")
    ResponseEntity changeEmail(@RequestBody EmailChangeRequest emailChangeRequest) {
        if (emailChangeRequest == null || !isEmailValid(emailChangeRequest.getNewEmail())) {
            return ResponseEntity.badRequest().body("Invalid Email");
        }

        try {
            registrationService.changeUserEmail(emailChangeRequest);
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
            registrationService.confirmUserEmailChange(token);
            return ResponseEntity.ok().body("Success");
        } catch (TokenNotFound tokenNotFound) {

            return ResponseEntity.badRequest().body("Token not found");
        } catch (TokenExpired tokenExpired) {
            return ResponseEntity.badRequest().body("Token Expired");
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
