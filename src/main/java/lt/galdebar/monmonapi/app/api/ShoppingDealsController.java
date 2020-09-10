package lt.galdebar.monmonapi.app.api;

import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.services.ShoppingItemDealFinderService;
import lt.galdebar.monmonapi.webscraper.services.exceptions.BadDealRequest;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("deals")
public class ShoppingDealsController {

    @Autowired
    private ShoppingItemDealFinderService dealService;


    @GetMapping("/getall")
    public List<ShoppingItemDealDTO> getAllDeals() {
        return dealService.getAllDeals();
    }

    @GetMapping("getall/shop")
    public List<ShoppingItemDealDTO> getDealsByShop(@RequestParam String shop) {
        try {
            return dealService.getDealsByShop(shop);
        } catch (BadDealRequest exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }
}
