package lt.galdebar.monmonapi.api;

import lt.galdebar.monmonapi.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.services.shoppingitems.ShoppingItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        return itemService.addItem(shoppingItemDTO);
    }

    @PostMapping("/update")
    public ShoppingItemDTO updateItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        return itemService.updateItem(shoppingItemDTO);
    }

    @DeleteMapping("/delete")
    public boolean deleteItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        return itemService.deleteItem(shoppingItemDTO);
    }

}
