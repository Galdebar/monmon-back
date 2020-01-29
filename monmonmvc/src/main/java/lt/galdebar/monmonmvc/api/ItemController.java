package lt.galdebar.monmonmvc.api;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.model.Item;
import lt.galdebar.monmonmvc.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private final ItemService itemService;

    @GetMapping("getByName")
    ResponseEntity getItem(@RequestParam(value = "name", required = false) String getString) {
        if (getString != null) {
            return ResponseEntity.ok("Item retrieved: " + itemService.getItemByName(getString).itemName);
        }
        return ResponseEntity.ok().build();
    }


    @CrossOrigin
    @PostMapping
    ResponseEntity addItem(@RequestBody Item item) {
        System.out.println("Adding item: " + item);
        System.out.println(item.itemName);
        if (item != null) {
            itemService.addItem(item);
            return ResponseEntity.ok( item + "Added");
        }
        System.out.println("Item NULL");
        return ResponseEntity.badRequest().build();
    }

}
