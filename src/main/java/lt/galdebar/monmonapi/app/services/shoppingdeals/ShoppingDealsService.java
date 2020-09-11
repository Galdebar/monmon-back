package lt.galdebar.monmonapi.app.services.shoppingdeals;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.app.services.shoppingdeals.exceptions.BadDealRequest;
import lt.galdebar.monmonapi.app.services.shoppingitems.exceptions.InvalidShoppingItemRequest;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.services.ShoppingItemDealFinderService;
import lt.galdebar.monmonapi.webscraper.services.scrapers.ShopNames;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingDealsService {

    private final ShoppingItemDealFinderService dealFinderService;

    public List<ShoppingItemDealDTO> getAllDeals(){
        return dealFinderService.getAllDeals();
    }

    public List<ShoppingItemDealDTO> getDealsByShop(String shop){
        for(ShopNames existingName:ShopNames.values()){
            if(existingName.getShopName().equalsIgnoreCase(shop)){
                return dealFinderService.getDealsByShop(existingName);
            }
        }

        throw new BadDealRequest("Invalid shop name: " + shop);
    }

    public ShoppingItemDealDTO findDeal(ShoppingItemDTO shoppingItemDTO) {
        if(shoppingItemDTO == null){
            throw new InvalidShoppingItemRequest("Null item request.");
        }
        if (shoppingItemDTO.getItemName() == null || shoppingItemDTO.getItemName().trim().isEmpty()) {
            throw new InvalidShoppingItemRequest("Item must have a name");
        }

        return dealFinderService.getBestDeal(shoppingItemDTO.getItemName());
    }

    public List<ShoppingItemDTO> attachDeals(List<ShoppingItemDTO> shoppingItemDTOS) {
        return shoppingItemDTOS.stream()
                .map(this::attachDeal)
                .collect(Collectors.toList());
    }

    public ShoppingItemDTO attachDeal(ShoppingItemDTO shoppingItemDTO) {
        shoppingItemDTO.setDeal(findDeal(shoppingItemDTO));
        return shoppingItemDTO;
    }
}
