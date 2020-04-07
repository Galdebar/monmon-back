package lt.galdebar.monmonmvc.api;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import lt.galdebar.monmonmvc.service.ShoppingItemCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Shopping Item Category Controller.<br>
 * Handles getting all categories, category search and shopping item search autocomplete
 */
@RestController
@Log4j2
@RequestMapping("/categorysearch")
public class ShoppingItemCategoryController {

    @Autowired
    private
    ShoppingItemCategoryService shoppingItemCategoryService;


    /**
     * Search autocomplete.<br>
     * <strong>POST request</strong><br>
     *
     * <strong>Requires valid Authorization Token in request header</strong><br>
     * Header format:<br>
     * {"Authorization":"Bearer [token]"}<br>
     *
     *
     * @param shoppingKeywordDTO contains the keyword. Category field can be empty.<br>
     * Full Shopping Keyword <strong>JSON</strong> example:<br>
     *
     *   {<br>
     *     "shoppingItemCategory": "Beverages",<br>
     *     "keyword": "Beer"<br>
     *   }<br>
     * @return Array of potential keywords matched with DB. Max of 10 (Number defined in ShoppingCategoriesService)
     * Returns empty array if keyword is empty or blank.<br>
     * Returns <strong>HTTP 403</strong> if Auth token
     * <ul>
     *     <li>Is empty</li>
     *     <li>Is invalid</li>
     *     <li>Has expired yet</li>
     * </ul><br>
     * Returns <strong>HTTP 400</strong> if request is empty.<br>
     * Returns <strong>HTTP 403</strong> if Authorization token is
     * <ul>
     *      <li>Empty</li>
     *      <li>Invalid</li>
     *      <li>Expired</li>
     * </ul>
     *
     */
    @CrossOrigin
    @PostMapping
    ResponseEntity searchAutocomplete(@RequestBody ShoppingKeywordDTO shoppingKeywordDTO){
        String failMessageStart = "Category search autocomplete failed! ";
        log.info(String.format(
                "Attempting to get items by category. User: %s | Requested details: %s ",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                shoppingKeywordDTO
        ));

        if(shoppingKeywordDTO == null){
            String message = "Invalid request";
            log.warn(failMessageStart + message);
            return ResponseEntity.badRequest().body(message);
        }

        if(shoppingKeywordDTO.getKeyword().trim().isEmpty()){
            log.warn("Returning empty array. ");
            return  ResponseEntity.ok(new ArrayList<>());
        }

        List<ShoppingKeywordDTO> results = shoppingItemCategoryService.searchKeywordAutocomplete(shoppingKeywordDTO);
        log.info("Results: " + results.toString());
        return ResponseEntity.ok(results);
    }

    /**
     * Get all Shopping Categories.<br>
     * <strong>POST request</strong><br>
     *
     * <strong>Requires valid Authorization Token in request header</strong><br>
     * Header format:<br>
     * {"Authorization":"Bearer [token]"}<br>
     *
     * @return Array of ShoppingItemCategory objects.<br>
     * Returns <strong>HTTP 403</strong> if Authorization token is
     * <ul>
     *      <li>Empty</li>
     *      <li>Invalid</li>
     *      <li>Expired</li>
     * </ul>
     */
    @CrossOrigin
    @GetMapping("getall")
    ResponseEntity getAllCategories(){
        log.info(String.format(
                "Attempting to get all categories. User: %s",
                SecurityContextHolder.getContext().getAuthentication().getName()
        ));

        return ResponseEntity.ok(shoppingItemCategoryService.getAllCategories());
    }

}
