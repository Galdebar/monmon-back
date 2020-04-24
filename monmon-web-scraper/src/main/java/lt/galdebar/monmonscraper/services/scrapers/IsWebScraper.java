package lt.galdebar.monmonscraper.services.scrapers;

import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.nodes.Document;

import java.util.List;

public interface IsWebScraper {
    List<ItemOnOffer> getItemsOnOffer();
    List<ItemOnOffer> getItemsOnOffer(Document document);
}
