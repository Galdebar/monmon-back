package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonscraper.services.ShoppingItemDealFinderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShoppingItemDealsService {

    @Autowired
    private ShoppingItemDealFinderService dealFinder;

    public ShoppingItemDTO findDeal(ShoppingItemDTO itemDTO){
        ShoppingItemDealDTO foundDeal = dealFinder.getBestDeal(itemDTO.getItemName());
        itemDTO.setItemDeal(foundDeal);
        return itemDTO;
    }

    public List<ShoppingItemDTO> findDeal(List<ShoppingItemDTO> itemDTOS){
        itemDTOS.forEach(this::findDeal);
        return itemDTOS;
    }
}
