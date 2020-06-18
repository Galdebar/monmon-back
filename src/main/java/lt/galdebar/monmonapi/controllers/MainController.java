package lt.galdebar.monmonapi.controllers;

import lt.galdebar.shoppingitems.TestResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {

    @Autowired
    private TestResource testResource;

    @GetMapping
    public String index(){
        return "index.html";
    }

    @GetMapping("/test")
    public String test(){
        return testResource.getMessage();
    }
}
