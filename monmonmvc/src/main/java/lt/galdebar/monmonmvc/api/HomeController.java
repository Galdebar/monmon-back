package lt.galdebar.monmonmvc.api;

import lt.galdebar.monmonmvc.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonmvc.persistence.domain.dto.LoginAttemptDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/")
public class HomeController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping
    ResponseEntity home(){
        String sayHello = "Hello";
        return ResponseEntity.ok(sayHello);
    }

    @CrossOrigin
    @PostMapping("/login")
    ResponseEntity login(@RequestBody LoginAttemptDTO loginAttemptDTO){
        if(!isEmailValid(loginAttemptDTO.getUserEmail())){
            return ResponseEntity.badRequest().body("Invalid Email");
        }
        try{
            String userName = loginAttemptDTO.getUserEmail();
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userName,
                            loginAttemptDTO.getUserPassword()
                    )
            );
            UserDTO foundUser = userService.findByUserEmail(userName);
            String token = jwtTokenProvider.createToken(
                    userName,
                    Collections.singletonList(foundUser.toString())
            );
            Map<Object, Object> model = new HashMap<>();
            model.put("userEmail",userName);
            model.put("token",token);
            return ResponseEntity.ok(model);
        } catch (AuthenticationException e){
            throw new BadCredentialsException("Invalid username/password supplied");
        }
    }

    @PostMapping("/signup")
    ResponseEntity signUp(@RequestBody UserDTO userDTO){
        if(userDTO != null && isEmailValid(userDTO.getUserEmail())){
            UserDTO addedUser = userService.addUser(userDTO);
            return ResponseEntity.ok(addedUser);
        } else return ResponseEntity.badRequest().build();
    }

    private boolean isEmailValid(String userEmail) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (userEmail == null)
            return false;
        return pat.matcher(userEmail).matches();
    }
}
