package lt.galdebar.monmonapi.api;

import lt.galdebar.monmonapi.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.services.shoppingitems.ShoppingItemService;
import lt.galdebar.monmonapi.services.shoppingitems.exceptions.InvalidShoppingItemRequest;
import lt.galdebar.monmonapi.services.shoppingitems.exceptions.ItemNotFound;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ShoppingItemsController {

    @Autowired
    private ShoppingItemService itemService;

    @GetMapping("/getall")
    public List<ShoppingItemDTO> getAllItems() {

        return itemService.getAll();
    }

    @PostMapping("/add")
    public ShoppingItemDTO addItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        try {
            return itemService.addItem(shoppingItemDTO);
        } catch (InvalidShoppingItemRequest exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @PostMapping("/update")
    public ShoppingItemDTO updateItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        try {
            return itemService.updateItem(shoppingItemDTO);
        } catch (InvalidShoppingItemRequest invalidRequest) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidRequest.getMessage());
        } catch (ItemNotFound notFound) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, notFound.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public boolean deleteItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        try {
            return itemService.deleteItem(shoppingItemDTO);
        } catch (InvalidShoppingItemRequest exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @DeleteMapping("/delete/all")
    public boolean deleteAllItems() {
        return itemService.deleteAllItems();
    }

    @GetMapping("/unmark/all")
    public List<ShoppingItemDTO> unmarkAllItems() {
        return itemService.unmarkAll();
    }

}
