package lt.galdebar.monmonmvc.api;

import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.service.UserRegistrationService;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.registration.TokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyValidated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
            tokenNotFound.printStackTrace();
            return ResponseEntity.badRequest().body("Incorrect token");
        } catch (UserAlreadyValidated userAlreadyValidated) {
            userAlreadyValidated.printStackTrace();
            return ResponseEntity.badRequest().body("User already validated");
        } catch (TokenExpired tokenExpired) {
            tokenExpired.printStackTrace();
            return ResponseEntity.badRequest().body("Token expired");
        }
    }

    @CrossOrigin
    @GetMapping(path = "/requestnewtoken/{token}")
    ResponseEntity requestNewToken(@RequestParam("token") String token){
        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body("No Token");
        }

        try {
            registrationService.extendTokenDuration(token);
            return ResponseEntity.ok("Account Validated!");

        } catch (UserAlreadyValidated userAlreadyValidated) {
            userAlreadyValidated.printStackTrace();
            return ResponseEntity.badRequest().body("User already validated");
        } catch (TokenNotFound tokenNotFound) {
            tokenNotFound.printStackTrace();
            return ResponseEntity.badRequest().body("Incorrect token");
        }
    }
}
