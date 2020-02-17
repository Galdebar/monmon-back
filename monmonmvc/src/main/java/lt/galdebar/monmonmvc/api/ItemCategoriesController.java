package lt.galdebar.monmonmvc.api;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.service.ItemCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class ItemCategoriesController {

    @Autowired
    private final ItemCategoryService itemCategoryService;


    @CrossOrigin
    @GetMapping("getShoppingItemCategories")
    ResponseEntity getShoppingItemCategories(){
        return ResponseEntity.ok(itemCategoryService.getItemCategories());
    }
}
