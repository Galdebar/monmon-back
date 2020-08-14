package lt.galdebar.monmonapi.webscraper.services.scrapers.helpers;

import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.nodes.Element;

public interface IsHTMLElementParserHelper {
    ItemOnOffer parseElement(Element element);
    Element getTitleElement(Element element);
    String getItemName(Element titleElement);
    String getItemBrand(Element titleElement);
    float getItemPrice(Element element);
}
