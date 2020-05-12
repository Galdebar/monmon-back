package lt.galdebar.monmonscraper.services.scrapers.helpers;

import lt.galdebar.monmonscraper.services.scrapers.ShopNames;
import org.jsoup.nodes.Element;

public class IkiParserHelper extends HTMLElementParserHelper {
    public IkiParserHelper() {
        super(ShopNames.IKI.getShopName());
    }

    @Override
    public Element getTitleElement(Element element) {
        return element.getElementsByClass("title").get(0).getElementsByTag("h4").get(0);
    }

    @Override
    public float getItemPrice(Element element) {
        float finalPrice;
        if (element.getElementsByClass("price-main").size() == 0
                || element.getElementsByClass("price-cents").size() == 0) {
            finalPrice = 0;
            return finalPrice;
        }

        String euro = element.getElementsByClass("price-main").get(0).text();
        String cents = element.getElementsByClass("price-cents").get(0).text();

        String finalPriceString = new StringBuilder()
                .append(euro)
                .append(".")
                .append(cents)
                .toString();
        try {
            finalPrice = Float.parseFloat(finalPriceString);
        } catch (NumberFormatException e) {
            finalPrice = 0;
        }
        return finalPrice;
    }
}
