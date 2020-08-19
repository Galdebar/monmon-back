package lt.galdebar.monmonapi.webscraper.services.helpers.translators;

import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;

import java.util.List;

public interface IsItemTranslator {

    List<ItemOnOffer> translate(List<ItemOnOffer> itemsToTranslate);
    ItemOnOffer translate(ItemOnOffer itemToTranslate);
    String translateString(String string);
}
