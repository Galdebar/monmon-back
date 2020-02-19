package lt.galdebar.monmonmvc.api;

import lt.galdebar.monmonmvc.service.NewCategoriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/categorysearch")
public class NewCategoriesController {
    @Autowired
    private NewCategoriesService service;

    @GetMapping
    ResponseEntity search(@RequestBody String searchString) {
        if (searchString != null && searchString != "") {
            return ResponseEntity.ok(searchString);
        }
        return ResponseEntity.badRequest().build();
    }
}
