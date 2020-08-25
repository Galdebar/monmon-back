package lt.galdebar.monmonapi.app.api;

import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.app.services.shoppingitems.ShoppingItemService;
import lt.galdebar.monmonapi.app.services.shoppingitems.exceptions.InvalidShoppingItemRequest;
import lt.galdebar.monmonapi.app.services.shoppingitems.exceptions.ItemNotFound;
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

    @CrossOrigin
    @GetMapping("/getall")
    public List<ShoppingItemDTO> getAllItems() {

        return itemService.getAll();
    }

    @CrossOrigin
    @PostMapping("/add")
    public ShoppingItemDTO addItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        try {
            return itemService.addItem(shoppingItemDTO);
        } catch (InvalidShoppingItemRequest exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @CrossOrigin
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

    @CrossOrigin
    @DeleteMapping("/delete")
    public boolean deleteItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        try {
            return itemService.deleteItem(shoppingItemDTO);
        } catch (InvalidShoppingItemRequest exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @CrossOrigin
    @DeleteMapping("/delete/all")
    public boolean deleteAllItems() {
        return itemService.deleteAllItems();
    }

    @CrossOrigin
    @DeleteMapping("/delete/incart")
    public boolean deletteAlllItemsInCart(){
        return itemService.deleteAllItemsInCart();
    }

    @CrossOrigin
    @GetMapping("/unmark/all")
    public List<ShoppingItemDTO> unmarkAllItems() {
        return itemService.unmarkAll();
    }

}
