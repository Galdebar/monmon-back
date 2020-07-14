package lt.galdebar.monmonapi.api;

import lt.galdebar.monmonapi.persistence.domain.shoppingitems.ShoppingItemDTO;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ShoppingItemsController {

    @GetMapping("/getall")
    public List<ShoppingItemDTO> getAllItems(){
        return new ArrayList<>();
    }

    @PostMapping("/additem")
    public ShoppingItemDTO addItem(@RequestBody ShoppingItemDTO shoppingItemDTO){
        return shoppingItemDTO;
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
