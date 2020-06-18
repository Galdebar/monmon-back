package lt.galdebar.monmonapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String greeting(){
        return "Well, maybe we'll finally finish this";
    }
}
