package lt.galdebar.monmonmvc.api;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonmvc.service.ShoppingItemService;
import lt.galdebar.monmonmvc.service.exceptions.shoppingitem.ShoppingItemNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/shoppingitems")
public class ShoppingItemController {

    @Autowired
    private final ShoppingItemService shoppingItemService;


    @GetMapping("getByCategory")
    ResponseEntity getItemsByCategory(@RequestParam(value = "shoppingItemCategory", required = false) String requestedCategory) {
        if (requestedCategory != null && !requestedCategory.equalsIgnoreCase("")) {
            return ResponseEntity.ok(shoppingItemService.getItemsByCategory(requestedCategory));
        } else if (!requestedCategory.equalsIgnoreCase("")) {
            return ResponseEntity.ok().build();
        } else return ResponseEntity.notFound().build();
    }

    @CrossOrigin
    @GetMapping("getAll")
    ResponseEntity getAllItems() {
        return ResponseEntity.ok(shoppingItemService.getAll());
    }


    @CrossOrigin
    @PostMapping("/additem")
    ResponseEntity<ShoppingItemDTO> addItem(@RequestBody(required = true) ShoppingItemDTO shoppingItemDTO) {
        if (!isShoppingItemDTOValid(shoppingItemDTO)) {
            return ResponseEntity.badRequest().build();
        }
        System.out.println(shoppingItemDTO.toString());
        ShoppingItemDTO returnedItem = shoppingItemService.addItem(shoppingItemDTO);
        return ResponseEntity.ok(returnedItem);
    }

    @CrossOrigin
    @PutMapping("updateitem")
    ResponseEntity updateItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        if (!isShoppingItemDTOValid(shoppingItemDTO)) {
            return ResponseEntity.badRequest().build();
        }
        ShoppingItemDTO returnedItem;

        try {
            returnedItem = shoppingItemService.updateItem(shoppingItemDTO);
            return ResponseEntity.ok(returnedItem);
        } catch (ShoppingItemNotFound shoppingItemNotFound) {
            return ResponseEntity.badRequest().body("Item not found");
        }
    }

    @CrossOrigin
    @PutMapping("/updateitems")
    ResponseEntity updateItems(@RequestBody List<ShoppingItemDTO> shoppingItemDTOS) {
        if (shoppingItemDTOS == null) {
            return ResponseEntity.badRequest().build();
        }
        for(ShoppingItemDTO item:shoppingItemDTOS){
            if(!isShoppingItemDTOValid(item)){
                return ResponseEntity.badRequest().build();
            }
        }

        List<ShoppingItemDTO> results;

        try {
            results = shoppingItemService.updateItems(shoppingItemDTOS);
            return ResponseEntity.ok(results);
        } catch (ShoppingItemNotFound shoppingItemNotFound) {
            return ResponseEntity.badRequest().body("Item not found");
        }
    }

    @CrossOrigin
    @DeleteMapping("deleteitem")
    ResponseEntity deleteById(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        if (!isShoppingItemDTOValid(shoppingItemDTO)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            shoppingItemService.deleteItem(shoppingItemDTO);
            return ResponseEntity.ok().build();
        } catch (ShoppingItemNotFound shoppingItemNotFound) {
            return ResponseEntity.badRequest().body("Item not found");
        }
    }

    @CrossOrigin
    @DeleteMapping("/deleteitems")
    ResponseEntity deleteItems(@RequestBody List<ShoppingItemDTO> shoppingItemDTOS) {
        if (shoppingItemDTOS == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            shoppingItemService.deleteItems(shoppingItemDTOS);
            return ResponseEntity.ok().build();
        } catch (ShoppingItemNotFound shoppingItemNotFound) {
            return ResponseEntity.badRequest().body("Item not found");
        }
    }


    private boolean isShoppingItemDTOValid(ShoppingItemDTO shoppingItemDTO) {
        if(shoppingItemDTO == null){
            return false;
        }
        if(!isStringValid(shoppingItemDTO.getItemName())){
            return false;
        }

        return true;
    }

    private boolean isStringValid(String itemName) {
        if(itemName == null){
            return false;
        }
        if(itemName.isEmpty()){
            return false;
        }
        if(itemName.trim().isEmpty()){
            return false;
        }

        return true;
    }
}
