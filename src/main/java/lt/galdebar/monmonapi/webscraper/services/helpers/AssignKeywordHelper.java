package lt.galdebar.monmonapi.webscraper.services.helpers;

import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingKeywordDTO;
import lt.galdebar.monmonapi.categoriesparser.services.CategoriesSearchService;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AssignKeywordHelper {

    @Autowired
    private CategoriesSearchService searchService;

    public ShoppingItemDealDTO assignKeyword(ItemOnOffer scrapedItem) {
        if (scrapedItem.getName().trim().isEmpty()) {
            return new ShoppingItemDealDTO();
        }
        List<ShoppingKeywordDTO> foundKeywords = searchService.findKeywords(
                new ShoppingKeywordDTO("", scrapedItem.getName())
        );
        if (foundKeywords.size() > 0) {
            return new ShoppingItemDealDTO(
                    foundKeywords.get(0).getKeyword(),
                    scrapedItem.getBrand(),
                    scrapedItem.getShopName(),
                    scrapedItem.getPrice()
            );
        } else {
            return new ShoppingItemDealDTO(
                    scrapedItem.getName(),
                    scrapedItem.getBrand(),
                    scrapedItem.getShopName(),
                    scrapedItem.getPrice()
            );
        }
    }

    public List<ShoppingItemDealDTO> assignKeywords(List<ItemOnOffer> itemOnOffers) {
        List<ShoppingItemDealDTO> deals = new ArrayList<>();
        for (ItemOnOffer item : itemOnOffers) {
            ShoppingItemDealDTO deal = assignKeyword(item);
            if (!deal.getItemKeyword().trim().isEmpty()) {
                deals.add(assignKeyword(item));
            }
        }

        return deals;
    }
}
