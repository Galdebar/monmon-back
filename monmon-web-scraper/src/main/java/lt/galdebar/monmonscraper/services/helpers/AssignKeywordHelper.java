package lt.galdebar.monmonscraper.services.helpers;

import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordDTO;
import lt.galdebar.monmon.categoriesparser.services.CategoriesSearchService;
import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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
        List<KeywordDTO> foundKeywords = searchService.findKeywords(
                new KeywordDTO("", scrapedItem.getName())
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
