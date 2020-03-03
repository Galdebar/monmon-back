package lt.galdebar.monmonmvc.api;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    ResponseEntity home(){
        String sayHello = "Hello";
        return ResponseEntity.ok(sayHello);
    }

    @GetMapping("/login")
    ResponseEntity login(){
        String login= "login";
        return ResponseEntity.ok(login);
    }
}
