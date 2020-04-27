package lt.galdebar.monmonscraper.services;

import lt.galdebar.monmonscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonscraper.services.helpers.ShoppingIitemDealAdapter;
import lt.galdebar.monmonscraper.services.scrapers.ShopNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DealsService {
    private final ShoppingIitemDealAdapter ADAPTER = new ShoppingIitemDealAdapter();

    @Autowired
    private ShoppingItemDealsRepo dealsRepo;


    public List<ShoppingItemDealDTO> getAllDeals() {
        return ADAPTER.entityToDTO(dealsRepo.findAll());
    }

    public List<ShoppingItemDealDTO> getDealByShop(ShopNames shop) {
        return ADAPTER.entityToDTO(dealsRepo.findByShopTitle(shop.getShopName()));
    }

    public List<ShoppingItemDealDTO> getDealsByKeyword(String keyword) {
        if(keyword == null || keyword.trim().isEmpty()){
            return new ArrayList<>();
        }
        return ADAPTER.entityToDTO(dealsRepo.findByItemKeyword(keyword));
    }
}
