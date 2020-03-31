package lt.galdebar.monmonmvc.api;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingCategoryDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import lt.galdebar.monmonmvc.service.ShoppingItemCategoryService;
import lt.galdebar.monmonmvc.service.exceptions.CanSendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Log4j2
@RequestMapping("/categorysearch")
public class ShoppingItemCategoryController {

    @Autowired
    private
    ShoppingItemCategoryService shoppingItemCategoryService;


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

    @CrossOrigin
    @GetMapping
    ResponseEntity searchCategory(@RequestBody ShoppingKeywordDTO shoppingKeywordDTO) {
        String failMessageStart = "Search item category failed! ";
        log.info(String.format(
                "Attempting to get items by category. User: %s | Requested details: %s ",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                shoppingKeywordDTO
        ));

        if(shoppingKeywordDTO == null || shoppingKeywordDTO.getKeyword().trim().isEmpty()){
            return logAndSendBadRequest(failMessageStart, "Invalid request");
        }

        ShoppingCategoryDTO result = shoppingItemCategoryService.findCategoryByKeyword(shoppingKeywordDTO);
        log.info("Results: " + result.toString());
        return ResponseEntity.ok(result);
    }

    @CrossOrigin
    @GetMapping("getall")
    ResponseEntity getAllCategories(){
        log.info(String.format(
                "Attempting to get all categories. User: %s",
                SecurityContextHolder.getContext().getAuthentication().getName()
        ));

        return ResponseEntity.ok(shoppingItemCategoryService.getAllCategories());
    }

    private ResponseEntity logAndSendBadRequest(String messageStart, String message){
        log.warn(messageStart + message);
        return ResponseEntity.badRequest().body(message);
    }
}
