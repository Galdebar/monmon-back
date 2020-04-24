package lt.galdebar.monmonscraper.services.scrapers.helpers;

import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.nodes.Element;

public interface IsHTMLElementParserHelper {
    ItemOnOffer parseElement(Element element);
}
