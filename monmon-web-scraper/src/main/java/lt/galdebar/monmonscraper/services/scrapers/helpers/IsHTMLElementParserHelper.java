package lt.galdebar.monmonscraper.services.scrapers.helpers;

import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface IsHTMLElementParserHelper {
    ItemOnOffer parseElement(Element element);
    Element getTitleElement(Element element);
    String getItemName(Element titleElement);
    String getItemBrand(Element titleElement);
    float getItemPrice(Element element);
}
