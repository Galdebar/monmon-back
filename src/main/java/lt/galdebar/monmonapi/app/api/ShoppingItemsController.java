package lt.galdebar.monmonapi.app.api;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.app.services.shoppingitems.ShoppingItemService;
import lt.galdebar.monmonapi.app.services.shoppingitems.exceptions.InvalidShoppingItemRequest;
import lt.galdebar.monmonapi.app.services.shoppingitems.exceptions.ItemNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/items")
public class ShoppingItemsController implements GetUsernameFromSecurityContext {

    @Autowired
    private ShoppingItemService itemService;

    @CrossOrigin
    @GetMapping("/getall")
    public List<ShoppingItemDTO> getAllItems() {

        return itemService.getAll();
    }

    @CrossOrigin
    @PostMapping("/add")
    public ShoppingItemDTO addItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        try {
            log.info(
                    "Adding item " + shoppingItemDTO.toString() + " for list " + getUserName()
            );
            return itemService.addItem(shoppingItemDTO);
        } catch (InvalidShoppingItemRequest exception) {
            log.warn(
                    "Cannot add item" + shoppingItemDTO.toString() + " for list " + getUserName() + ". Error: " + exception.getMessage()
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @CrossOrigin
    @PostMapping("/update")
    public ShoppingItemDTO updateItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        try {
            log.info(
                    "Updating item " + shoppingItemDTO.toString() + " for list " + getUserName()
            );
            return itemService.updateItem(shoppingItemDTO);
        } catch (InvalidShoppingItemRequest invalidRequest) {
            log.warn(
                    "Cannot update item" + shoppingItemDTO.toString() + " for list " + getUserName() + ". Error: " + invalidRequest.getMessage()
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidRequest.getMessage());
        } catch (ItemNotFound notFound) {
            log.warn(
                    "Cannot update item" + shoppingItemDTO.toString() + " for list " + getUserName() + ". Error: " + notFound.getMessage()
            );
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, notFound.getMessage());
        }
    }

    @CrossOrigin
    @DeleteMapping("/delete")
    public boolean deleteItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        try {
            log.info(
                    "Deleting item " + shoppingItemDTO.toString() + " for list " + getUserName()
            );
            return itemService.deleteItem(shoppingItemDTO);
        } catch (InvalidShoppingItemRequest exception) {
            log.warn(
                    "Cannot update item" + shoppingItemDTO.toString() + " for list " + getUserName() + ". Error: " + exception.getMessage()
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @CrossOrigin
    @DeleteMapping("/delete/all")
    public boolean deleteAllItems() {
        log.info(
                "Deleting all items for list " + getUserName()
        );
        return itemService.deleteAllItems();
    }

    @CrossOrigin
    @DeleteMapping("/delete/incart")
    public boolean deletteAlllItemsInCart(){
        log.info(
                "Deleting all items in cart for list " + getUserName()
        );
        return itemService.deleteAllItemsInCart();
    }

    @CrossOrigin
    @GetMapping("/unmark/all")
    public List<ShoppingItemDTO> unmarkAllItems() {
        log.info(
                "Unmarking all items for list " + getUserName()
        );
        return itemService.unmarkAll();
    }

}
