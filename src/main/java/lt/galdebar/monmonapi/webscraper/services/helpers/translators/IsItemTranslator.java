package lt.galdebar.monmonapi.webscraper.services.helpers.translators;

import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;

import java.util.List;

public interface IsItemTranslator {

    List<ShoppingItemDealDTO> translate(List<ShoppingItemDealDTO> itemsToTranslate);
    ShoppingItemDealDTO translate(ShoppingItemDealDTO itemToTranslate);
    String translateString(String string);
}
