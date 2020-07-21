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
    public List<ShoppingItemDTO> getAllItems(){

        return itemService.getAll();
    }

    @PostMapping("/additem")
    public ShoppingItemDTO addItem(@RequestBody ShoppingItemDTO shoppingItemDTO){
        return itemService.addItem(shoppingItemDTO);
    }

    @PostMapping("/updateitem")
    public ShoppingItemDTO updateItem(@RequestBody ShoppingItemDTO shoppingItemDTO){
        return shoppingItemDTO;
    }

    @DeleteMapping("/deleteitem")
    public boolean deleteItem(@RequestBody ShoppingItemDTO shoppingItemDTO){
        return true;
    }

}
