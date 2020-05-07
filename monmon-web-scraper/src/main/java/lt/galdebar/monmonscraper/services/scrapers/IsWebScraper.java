package lt.galdebar.monmonscraper.services.scrapers;

import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.nodes.Document;

import java.util.List;

public interface IsWebScraper {
    boolean isValid();
    List<ItemOnOffer> getItemsOnOffer();
    List<ItemOnOffer> getItemsOnOffer(Document document);
    boolean updateOffersDB();
    ShopNames getSHOP();
}
