package lt.galdebar.monmonscraper.services.scrapers.helpers;

import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import lt.galdebar.monmonscraper.services.scrapers.ShopNames;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class MaximaParserHelper implements IsHTMLElementParserHelper {
    private final String shopName = ShopNames.MAXIMA.getShopName();

    public ItemOnOffer parseElement(Element element) {
        String name = getItemName(getTitleElement(element));
        String brand = getItemBrand(getTitleElement(element));
        float price = getItemPrice(element);
        if(name.equalsIgnoreCase("")) {
            return new ItemOnOffer(brand, "", price,shopName);
        } else return new ItemOnOffer(name,brand,price,shopName);
    }

    @Override
    public float getItemPrice(Element element) {
        float finalPrice;

        if (element.getElementsByClass("t1").size() == 0) {
            finalPrice = 0;
            return finalPrice;
        }

        Element priceElement = element
                .getElementsByClass("price").get(0)
                .getElementsByClass("t1").get(0);

        String value = priceElement.getElementsByClass("value").get(0).text();
        String cents = priceElement.getElementsByClass("cents").get(0).text();

        String finalPriceString = new StringBuilder()
                .append(value)
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

    private Element getTitleElement(Element itemElement) {
        return itemElement.getElementsByClass("title").get(0);
    }

}
