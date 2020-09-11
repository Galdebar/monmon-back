package lt.galdebar.monmonapi.app.api;

import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.app.services.shoppingdeals.ShoppingDealsService;
import lt.galdebar.monmonapi.app.services.shoppingitems.exceptions.InvalidShoppingItemRequest;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.app.services.shoppingdeals.exceptions.BadDealRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("deals")
public class ShoppingDealsController {

    @Autowired
    private ShoppingDealsService dealService;


    @CrossOrigin
    @GetMapping("/getall")
    public List<ShoppingItemDealDTO> getAllDeals() {
        return dealService.getAllDeals();
    }

    @CrossOrigin
    @GetMapping("getall/shop")
    public List<ShoppingItemDealDTO> getDealsByShop(@RequestParam String shop) {
        try {
            return dealService.getDealsByShop(shop);
        } catch (BadDealRequest exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @CrossOrigin
    @PostMapping("find")
    public ShoppingItemDealDTO findDeal(@RequestBody ShoppingItemDTO shoppingItemDTO){
        try{
            return dealService.findDeal(shoppingItemDTO);
        }catch (InvalidShoppingItemRequest exception){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,exception.getMessage());
        }
    }

    @CrossOrigin
    @PostMapping("attach")
    public ShoppingItemDTO attachDeal(@RequestBody ShoppingItemDTO shoppingItemDTO){
        try{
            return dealService.attachDeal(shoppingItemDTO);
        }catch (InvalidShoppingItemRequest exception){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,exception.getMessage());
        }
    }

    @CrossOrigin
    @PostMapping("attach/all")
    public List<ShoppingItemDTO> findDeals(@RequestBody List<ShoppingItemDTO> shoppingItemDTOS){
        try{
            return dealService.attachDeals(shoppingItemDTOS);
        }catch (InvalidShoppingItemRequest exception){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,exception.getMessage());
        }
    }
}
