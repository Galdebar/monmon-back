package lt.galdebar.monmonmvc.api;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.persistence.dao.ShoppingItem;
import lt.galdebar.monmonmvc.persistence.dao.ShoppingItemCategory;
import lt.galdebar.monmonmvc.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/shoppingitems")
public class ItemController {

    @Autowired
    private final ItemService itemService;

    @GetMapping("getByName")
    ResponseEntity getItemByName(@RequestParam(value = "itemName", required = true) String requestedItemName) {
        if (requestedItemName != null) {
            return ResponseEntity.ok(itemService.getItemByName(requestedItemName));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("getByCategory")
    ResponseEntity getItemsByCategory(@RequestParam(value = "category", required = false) String requestedCategory){
        if(requestedCategory != null && !requestedCategory.equalsIgnoreCase("")){
            return ResponseEntity.ok(itemService.getItemsByCategory(requestedCategory));
        }else if(!requestedCategory.equalsIgnoreCase("")){
            return ResponseEntity.ok(itemService.getItemsByCategory(ShoppingItemCategory.UNCATEGORIZED.name()));
        }else return ResponseEntity.notFound().build();
    }

    @CrossOrigin
    @GetMapping("getAll")
    ResponseEntity getAllItems(){
        return ResponseEntity.ok(itemService.getAll());
    }

//    @CrossOrigin
//    @GetMapping("getUnmarked")
//    ResponseEntity getItemsNotInCart(){return ResponseEntity.ok(itemService.getAllNOTInCart());}
//
//    @GetMapping("getByFullBody")
//    ResponseEntity getItemByFullBody(@RequestBody ShoppingItem shoppingItem) {
//        if (shoppingItem != null) {
//            return ResponseEntity.ok("ShoppingItem retrieved: " + itemService.getItemByName(shoppingItem.itemName));
//        }
//        return ResponseEntity.notFound().build();
//    }


    @CrossOrigin
    @PostMapping
    ResponseEntity addItem(@RequestBody ShoppingItem shoppingItem) {
        if (shoppingItem != null) {
            ShoppingItem returnedItem = itemService.addItem(shoppingItem);
            return ResponseEntity.ok( returnedItem );
        }
        System.out.println("ShoppingItem NULL");
        return ResponseEntity.badRequest().build();
    }

    @CrossOrigin
    @PutMapping(value = "{id}")
    ResponseEntity<ShoppingItem> updateItem(@PathVariable("id")String id, @RequestBody ShoppingItem shoppingItem){
        if(shoppingItem != null && itemService.getItemById(id) != null){// is this check neccessary, or is it enough to just update item?
            ShoppingItem returnedItem = itemService.updateItem(shoppingItem);
            return ResponseEntity.ok(returnedItem);
        }
        return ResponseEntity.badRequest().build();
    }

    @CrossOrigin
    @PutMapping("/updateItems")
    ResponseEntity updateItems(@RequestBody ShoppingItem[] shoppingItems){
        if(shoppingItems != null){
            List<ShoppingItem> results = itemService.updateItems(shoppingItems);
            return ResponseEntity.ok(results);
        }
        return ResponseEntity.badRequest().build();
    }

    @CrossOrigin
    @DeleteMapping(value = "{id}")
    ResponseEntity deleteById(@PathVariable("id")String id, @RequestBody ShoppingItem shoppingItem){
        if(shoppingItem != null && itemService.getItemById(id) != null){// is this check neccessary, or is it enough to just update item?
            itemService.deleteItem(shoppingItem);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @CrossOrigin
    @DeleteMapping("/deleteItems")
    ResponseEntity deleteItems(@RequestBody ShoppingItem[] shoppingItems){
        if(shoppingItems != null){
            itemService.deleteItems(shoppingItems);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

}
