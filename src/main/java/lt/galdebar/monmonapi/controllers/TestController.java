package lt.galdebar.monmonapi.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestController {

//    @GetMapping("/")
//    public ModelAndView greeting(){
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.setViewName("index");
//        return modelAndView;
//    }

    @GetMapping
    public String index(){
        return "index.html";
    }
}
