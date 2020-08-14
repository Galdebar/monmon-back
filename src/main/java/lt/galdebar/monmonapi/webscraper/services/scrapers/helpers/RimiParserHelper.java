package lt.galdebar.monmonapi.webscraper.services.scrapers.helpers;

import lt.galdebar.monmonapi.webscraper.services.scrapers.ShopNames;
import org.jsoup.nodes.Element;

public class RimiParserHelper extends HTMLElementParserHelper {

    public RimiParserHelper() {
        super(ShopNames.RIMI.getShopName());
    }

    @Override
    public float getItemPrice(Element element) {
        float finalPrice;

        if (element.getElementsByClass("price-badge__inner").size() == 0) {
            finalPrice = 0;
            return finalPrice;
        }

        Element priceElement = element
                .getElementsByClass("price-badge").get(0)
                .getElementsByClass("price-badge__inner").get(0);

        String value = priceElement.getElementsByClass("euro").get(0).text();
        String cents = priceElement.getElementsByClass("cents").get(0).text();

        String finalPriceString = value +
                "." +
                cents;
        try {
            finalPrice = Float.parseFloat(finalPriceString);
        } catch (NumberFormatException e) {
            finalPrice = 0;
        }
        return finalPrice;
    }

    @Override
    public Element getTitleElement(Element element) {
        return element.getElementsByClass("offer-card__name").get(0);
    }
}
