package lt.galdebar.monmonmvc.api;

import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingCategoryDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import lt.galdebar.monmonmvc.service.ShoppingItemCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/categorysearch")
public class ShoppingItemCategoryController {

    @Autowired
    ShoppingItemCategoryService shoppingItemCategoryService;


    @CrossOrigin
    @PostMapping
    ResponseEntity<List<ShoppingKeywordDTO>> searchAutocomplete(@RequestBody ShoppingKeywordDTO shoppingKeywordDTO){
        if(shoppingKeywordDTO != null){
            List<ShoppingKeywordDTO> results = new ArrayList<>();
            try{
                results = shoppingItemCategoryService.searchKeywordAutocomplete(shoppingKeywordDTO);
            }catch (Exception e){
                e.printStackTrace();
            }
            return ResponseEntity.ok(results);
        } else return ResponseEntity.badRequest().build();
    }

    @CrossOrigin
    @GetMapping("cat")
    ResponseEntity searchCategory(@RequestParam(value = "searchcategory", required = false) ShoppingKeywordDTO keyword) {
        System.out.println(keyword);
        if (keyword != null && keyword.getKeyword() != "") {
            ShoppingCategoryDTO result = null;
            try{
                result = shoppingItemCategoryService.findCategoryByKeyword(keyword);
            } catch (Exception e){
                e.printStackTrace();
            }

            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().build();
    }

    @CrossOrigin
    @GetMapping("getall")
    ResponseEntity getAllCategories(){
        return ResponseEntity.ok(shoppingItemCategoryService.getAllCategories());
    }
}
