package lt.galdebar.monmonmvc.api;

import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.service.ConnectUsersService;
import lt.galdebar.monmonmvc.service.exceptions.connectusers.ConnectUsersTokenExpired;
import lt.galdebar.monmonmvc.service.exceptions.connectusers.ConnectUsersTokenNotFound;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@Controller
@RequestMapping("/connectusers")
public class ConnectUsersController {

    @Autowired
    private ConnectUsersService connectUsersService;

    @CrossOrigin
    @PostMapping("/connect")
    ResponseEntity connectUser(@RequestBody UserDTO userDTO) {
        if (userDTO == null || !isEmailValid(userDTO.getUserEmail())) {
            return ResponseEntity.badRequest().body("Invalid User Email");
        }
        try {
            connectUsersService.connectUserWithCurrent(userDTO);
            return ResponseEntity.ok("Connection request sent");
        } catch (UserNotFound userNotFound) {
            userNotFound.printStackTrace();
            return ResponseEntity.badRequest().body("Such User doesn't exist");
        }
    }

    @CrossOrigin
    @GetMapping("confirm/{token}")
    ResponseEntity confirmConnectUsers(@PathVariable String token) {
        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body("No Token");
        }

        try {
            connectUsersService.confirmUserConnect(token);
            return ResponseEntity.ok("Success");
        } catch (ConnectUsersTokenNotFound connectUsersTokenNotFound) {
            connectUsersTokenNotFound.printStackTrace();
            return ResponseEntity.badRequest().body("Token Not Found");
        } catch (ConnectUsersTokenExpired connectUsersTokenExpired) {
            connectUsersTokenExpired.printStackTrace();
            return ResponseEntity.badRequest().body("Token Expired, Send request again");
        }
    }

    @CrossOrigin
    @GetMapping("renew/{token}")
    ResponseEntity renewConnectRequestToken(@PathVariable String token) {
        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body("No Token");
        }

        try {
            connectUsersService.renewToken(token);
            return ResponseEntity.ok("Success");
        } catch (ConnectUsersTokenExpired connectUsersTokenExpired) {
            connectUsersTokenExpired.printStackTrace();
            return ResponseEntity.badRequest().body("Token Expired, Send request again");
        } catch (ConnectUsersTokenNotFound connectUsersTokenNotFound) {
            connectUsersTokenNotFound.printStackTrace();
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
