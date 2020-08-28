package lt.galdebar.monmonapi.webscraper.services.helpers;

import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingKeywordDTO;
import lt.galdebar.monmonapi.categoriesparser.services.CategoriesSearchService;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;

@Component
public class AssignKeywordHelper {

    @Autowired
    private CategoriesSearchService searchService;

    @Autowired
    private StringMatcherHelper stringMatcher;

    public ShoppingItemDealDTO assignKeyword(ItemOnOffer scrapedItem) {
        if (scrapedItem.getName().trim().isEmpty()) {
            return new ShoppingItemDealDTO();
        }
        List<ShoppingKeywordDTO> foundKeywords = searchService.findKeywords(
                new ShoppingKeywordDTO("", scrapedItem.getName())
        );
        if (foundKeywords.size() > 0) {
            String closestKeyword = stringMatcher.findBestMatch(
                    scrapedItem.getName(),
                    foundKeywords.stream()
                    .map(ShoppingKeywordDTO::getKeyword)
                    .collect(Collectors.toList())
            );
            return new ShoppingItemDealDTO(
                    scrapedItem.getName(),
                    closestKeyword,
                    scrapedItem.getBrand(),
                    scrapedItem.getShopName(),
                    scrapedItem.getPrice()
            );
        } else {
            return new ShoppingItemDealDTO(
                    scrapedItem.getName(),
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
