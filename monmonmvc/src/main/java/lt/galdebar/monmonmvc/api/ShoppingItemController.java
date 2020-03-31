package lt.galdebar.monmonmvc.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonmvc.service.ShoppingItemService;
import lt.galdebar.monmonmvc.service.exceptions.CanSendResponse;
import lt.galdebar.monmonmvc.service.exceptions.shoppingitem.ShoppingItemNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Log4j2
@RequestMapping("/shoppingitems")
public class ShoppingItemController {

    @Autowired
    private ShoppingItemService shoppingItemService;


    @GetMapping("getByCategory")
    ResponseEntity getItemsByCategory(@RequestParam(value = "shoppingItemCategory", required = false) String requestedCategory) {
        String failMessageStart = "Get item category failed! ";
        log.info(String.format(
                "Attempting to get items by category. User: %s | Requested category: %s ",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                requestedCategory
        ));

        if (requestedCategory == null || requestedCategory.trim().isEmpty()) {
            return logAndSendBadRequest(failMessageStart, "Invalid request");
        }
        List<ShoppingItemDTO> result = shoppingItemService.getItemsByCategory(requestedCategory);
        log.info("Results: " + result.toString());
        return ResponseEntity.ok(result);
    }

    @CrossOrigin
    @GetMapping("getAll")
    ResponseEntity getAllItems() {
        log.info(String.format(
                "Attempting to get all items. User: %s",
                SecurityContextHolder.getContext().getAuthentication().getName()
        ));

        List<ShoppingItemDTO> result = shoppingItemService.getAll();
        log.info(String.format(
                "Result: %s ",
                result.toString()
        ));
        return ResponseEntity.ok(result);
    }


    @CrossOrigin
    @PostMapping("/additem")
    ResponseEntity addItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        log.info(String.format(
                "Attempting to add item. User: %s | Request details: %s ",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                shoppingItemDTO.toString()
        ));

        if (!isShoppingItemDTOValid(shoppingItemDTO)) {
            return logAndSendBadRequest("Add item failed! ", "Invalid request");
        }

        ShoppingItemDTO result = shoppingItemService.addItem(shoppingItemDTO);
        log.info("Item added successfully. Item details: " + result.toString());
        return ResponseEntity.ok(result);
    }

    @CrossOrigin
    @PutMapping("updateitem")
    ResponseEntity updateItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        String failMessageStart = "Update item failed! ";
        log.info(String.format(
                "Attempting to update item. User: %s | Request details: %s ",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                shoppingItemDTO.toString()
        ));

        if (!isShoppingItemDTOValid(shoppingItemDTO)) {
            return logAndSendBadRequest(failMessageStart, "Invalid request");
        }
        ShoppingItemDTO returnedItem;

        try {
            returnedItem = shoppingItemService.updateItem(shoppingItemDTO);
            log.info("Result: " + returnedItem.toString());
            return ResponseEntity.ok(returnedItem);
        } catch (ShoppingItemNotFound shoppingItemNotFound) {
            return logAndSendBadRequest(failMessageStart, shoppingItemNotFound);
        }
    }

    @CrossOrigin
    @PutMapping("/updateitems")
    ResponseEntity updateItems(@RequestBody List<ShoppingItemDTO> shoppingItemDTOS) {
        String failMessageStart = "Update multiple items failed! ";
        log.info(String.format(
                "Attempting to update multiple items. User: %s | Request details: %s ",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                shoppingItemDTOS.toString()
        ));

        for (ShoppingItemDTO item : shoppingItemDTOS) {
            if (!isShoppingItemDTOValid(item)) {
                return logAndSendBadRequest(failMessageStart, "Invalid request");
            }
        }

        try {
            List<ShoppingItemDTO> results = shoppingItemService.updateItems(shoppingItemDTOS);
            log.info("Results: " + results.toString());
            return ResponseEntity.ok(results);
        } catch (ShoppingItemNotFound shoppingItemNotFound) {
            return logAndSendBadRequest(failMessageStart, shoppingItemNotFound);
        }
    }

    @CrossOrigin
    @DeleteMapping("deleteitem")
    ResponseEntity deleteById(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        String failMessageStart = "Delete item failed! ";
        log.info(String.format(
                "Attempting to delete item. User: %s | Request details: %s ",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                shoppingItemDTO.toString()
        ));

        if (!isShoppingItemDTOValid(shoppingItemDTO)) {
            return logAndSendBadRequest(failMessageStart, "Invalid request");
        }
        try {
            shoppingItemService.deleteItem(shoppingItemDTO);
            String message = "Item deleted successfully";
            log.info(message);
            return ResponseEntity.ok().body(message);
        } catch (ShoppingItemNotFound shoppingItemNotFound) {
            return logAndSendBadRequest(failMessageStart, shoppingItemNotFound);
        }
    }

    @CrossOrigin
    @DeleteMapping("/deleteitems")
    ResponseEntity deleteItems(@RequestBody List<ShoppingItemDTO> shoppingItemDTOS) {
        String failMessageStart = "Delete multiple items failed! ";
        log.info(String.format(
                "Attempting to delete multiple items. User: %s | Request details: %s ",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                shoppingItemDTOS.toString()
        ));
        try {
            shoppingItemService.deleteItems(shoppingItemDTOS);
            String message = "Items deleted successfully";
            log.info(message);
            return ResponseEntity.ok().body(message);
        } catch (ShoppingItemNotFound shoppingItemNotFound) {
            return logAndSendBadRequest(failMessageStart, shoppingItemNotFound);
        }
    }


    private boolean isShoppingItemDTOValid(ShoppingItemDTO shoppingItemDTO) {
        if (shoppingItemDTO == null) {
            return false;
        }
        if (!isStringValid(shoppingItemDTO.getItemName())) {
            return false;
        }

        return true;
    }

    private boolean isStringValid(String itemName) {
        if (itemName == null) {
            return false;
        }
        if (itemName.isEmpty()) {
            return false;
        }
        if (itemName.trim().isEmpty()) {
            return false;
        }

        return true;
    }

    private ResponseEntity logAndSendBadRequest(String messageStart, String message){
        log.warn(messageStart + message);
        return ResponseEntity.badRequest().body(message);
    }

    private <T extends Throwable & CanSendResponse> ResponseEntity logAndSendBadRequest(String messageStart, T exception){
        log.warn(messageStart + exception.getMessage());
        return ResponseEntity.badRequest().body(exception.getResponseMessage());
    }
}
