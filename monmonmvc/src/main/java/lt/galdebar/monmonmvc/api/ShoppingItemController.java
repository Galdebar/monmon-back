package lt.galdebar.monmonmvc.api;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonmvc.service.ShoppingItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/shoppingitems")
public class ShoppingItemController {

    @Autowired
    private final ShoppingItemService shoppingItemService;


    @GetMapping("getByCategory")
    ResponseEntity getItemsByCategory(@RequestParam(value = "shoppingItemCategory", required = false) String requestedCategory){
        if(requestedCategory != null && !requestedCategory.equalsIgnoreCase("")){
            return ResponseEntity.ok(shoppingItemService.getItemsByCategory(requestedCategory));
        }else if(!requestedCategory.equalsIgnoreCase("")){
            return ResponseEntity.ok().build();
        }else return ResponseEntity.notFound().build();
    }

    @CrossOrigin
    @GetMapping("getAll")
    ResponseEntity getAllItems(){
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return ResponseEntity.ok(shoppingItemService.getAll());
    }




    @CrossOrigin
    @PostMapping
    ResponseEntity<ShoppingItemDTO> addItem(@RequestBody ShoppingItemDTO shoppingItemDTO) {
        if (shoppingItemDTO != null) {
            ShoppingItemDTO returnedItem = shoppingItemService.addItem(shoppingItemDTO);
            return ResponseEntity.ok( returnedItem );
        }
        return ResponseEntity.badRequest().build();
    }

    @CrossOrigin
    @PutMapping(value = "{id}")
    ResponseEntity<ShoppingItemDTO> updateItem(@PathVariable("id")String id, @RequestBody ShoppingItemDTO shoppingItemDTO){
        if(shoppingItemDTO != null && shoppingItemService.getItemById(id) != null){// is this check neccessary, or is it enough to just update item?
            ShoppingItemDTO returnedItem = shoppingItemService.updateItem(shoppingItemDTO);
            return ResponseEntity.ok(returnedItem);
        }
        return ResponseEntity.badRequest().build();
    }

    @CrossOrigin
    @PutMapping("/updateItems")
    ResponseEntity<List<ShoppingItemDTO>> updateItems(@RequestBody List<ShoppingItemDTO> shoppingItemDTOS){
        if(shoppingItemDTOS != null){
            List<ShoppingItemDTO> results = shoppingItemService.updateItems(shoppingItemDTOS);
            return ResponseEntity.ok(results);
        }
        return ResponseEntity.badRequest().build();
    }

    @CrossOrigin
    @DeleteMapping(value = "{id}")
    ResponseEntity deleteById(@PathVariable("id")String id, @RequestBody ShoppingItemDTO shoppingItemDTO){
        if(shoppingItemDTO != null && shoppingItemService.getItemById(id) != null){// is this check neccessary, or is it enough to just update item?
            shoppingItemService.deleteItem(shoppingItemDTO);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @CrossOrigin
    @DeleteMapping("/deleteItems")
    ResponseEntity deleteItems(@RequestBody List<ShoppingItemDTO> shoppingItemDTOS){
        if(shoppingItemDTOS != null){
            shoppingItemService.deleteItems(shoppingItemDTOS);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

}
