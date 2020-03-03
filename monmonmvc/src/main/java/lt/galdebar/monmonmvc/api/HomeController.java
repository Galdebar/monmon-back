package lt.galdebar.monmonmvc.api;

import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {
    @Autowired
    private UserService userService;

    @GetMapping
    ResponseEntity home(){
        String sayHello = "Hello";
        return ResponseEntity.ok(sayHello);
    }

    @PostMapping("/login")
    ResponseEntity login(@RequestBody UserDTO userDTO){
        String login= "login";
        return ResponseEntity.ok(login);
    }

    @PostMapping("/signup")
    ResponseEntity signUp(@RequestBody UserDTO userDTO){
        if(userDTO != null){
            UserDTO addedUser = userService.addUser(userDTO);
            return ResponseEntity.ok(addedUser);
        } else return ResponseEntity.badRequest().build();
    }
}
