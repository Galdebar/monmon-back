package lt.galdebar.monmonapi.webscraper.services.scrapers.helpers;

import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
import lt.galdebar.monmonapi.webscraper.services.scrapers.ShopNames;
import org.jsoup.nodes.Element;


public class MaximaParserHelper extends HTMLElementParserHelper {

    public MaximaParserHelper() {
        super(ShopNames.MAXIMA.getShopName());
    }
//
//    @Override
//    public ShoppingItemDealDTO parseElement(Element element) {
//        String untranslatedTitle = getItemName(getTitleElement(element));
//        String brand = getItemBrand(getTitleElement(element));
//        float price = getItemPrice(element);
//        if(untranslatedTitle.equalsIgnoreCase("")) {
//            return new ShoppingItemDealDTO("",brand, "",shopName, price);
//        } else return new ShoppingItemDealDTO(untranslatedTitle,"",brand,shopName,price);
//    }

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

    public Element getTitleElement(Element itemElement) {
        return itemElement.getElementsByClass("title").get(0);
    }

}
