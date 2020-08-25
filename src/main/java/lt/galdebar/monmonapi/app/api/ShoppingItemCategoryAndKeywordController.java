package lt.galdebar.monmonapi.app.api;

import lt.galdebar.monmonapi.app.services.categories.ShoppingItemCategoryAndKeywordService;
import lt.galdebar.monmonapi.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingKeywordDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class ShoppingItemCategoryAndKeywordController {

    @Autowired
    private ShoppingItemCategoryAndKeywordService service;

    @CrossOrigin
    @GetMapping("/getall")
    public List<CategoryDTO> getAllCategories(){
        return service.getAllCategories();
    }

    @CrossOrigin
    @PostMapping("/search")
    public List<ShoppingKeywordDTO> searchAutocomplete(@RequestBody ShoppingKeywordDTO shoppingKeywordDTO){
        return service.searchAutocomplete(shoppingKeywordDTO);
    }

}
