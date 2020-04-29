package lt.galdebar.monmonscraper.services;

import lt.galdebar.monmonscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonscraper.services.helpers.ShoppingIitemDealAdapter;
import lt.galdebar.monmonscraper.services.scrapers.ShopNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Component
public class ShoppingItemDealFinderService {
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
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return ADAPTER.entityToDTO(dealsRepo.findByItemKeyword(keyword));
    }

    public ShoppingItemDealDTO getBestDeal(String keyword) {
        List<ShoppingItemDealEntity> foundDeals = dealsRepo.findByItemKeyword(keyword);
        if (foundDeals.size() == 0) {
            return new ShoppingItemDealDTO();
        }
        if (foundDeals.size() > 1) {
            ShoppingItemDealEntity bestDeal = foundDeals.stream()
                    .sorted((o1, o2) -> Float.compare(o1.getPrice(), o2.getPrice()))
                    .collect(Collectors.toList())
                    .get(0);
            return ADAPTER.entityToDTO(bestDeal);
        } else return ADAPTER.entityToDTO(foundDeals.get(0));
    }
}
