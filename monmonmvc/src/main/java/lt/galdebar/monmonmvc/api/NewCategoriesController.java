package lt.galdebar.monmonmvc.api;

import lt.galdebar.monmonmvc.persistence.dao.Category;
import lt.galdebar.monmonmvc.service.HibernateSearchService;
import lt.galdebar.monmonmvc.service.NewCategoriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/categorysearch")
public class NewCategoriesController {
    @Autowired
    private NewCategoriesService service;

    @Autowired
    HibernateSearchService hibernateSearchService;



    @CrossOrigin
    @GetMapping
    ResponseEntity search(@RequestParam(value = "search", required = false) String searchString) {
        if (searchString != null && searchString != "") {
            List<String> results = new ArrayList<>();
            try{
                results = hibernateSearchService.searchKeywordAutocomplete(searchString);
            } catch (Exception e){
                e.printStackTrace();
            }

            return ResponseEntity.ok(results);
        }
        return ResponseEntity.badRequest().build();
    }

    @CrossOrigin
    @GetMapping("cat")
    ResponseEntity searchCategory(@RequestParam(value = "searchcategory", required = false) String searchString) {
        System.out.println(searchString);
        if (searchString != null && searchString != "") {
            Category result = null;
            try{
                result = hibernateSearchService.findCategoryByKeyword(searchString);
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
        return ResponseEntity.ok(hibernateSearchService.getAllCategories());
    }
}
