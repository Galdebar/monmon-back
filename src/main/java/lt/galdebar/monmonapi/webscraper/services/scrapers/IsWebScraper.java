package lt.galdebar.monmonapi.webscraper.services.scrapers;

import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.nodes.Document;

import java.util.List;

public interface IsWebScraper {
    boolean isValid();
    List<ShoppingItemDealDTO> getItemsOnOffer();
    List<ShoppingItemDealDTO> getItemsOnOffer(Document document);
    boolean updateOffersDB();
    ShopNames getSHOP();
}
