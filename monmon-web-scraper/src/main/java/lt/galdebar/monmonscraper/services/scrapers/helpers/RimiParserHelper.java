package lt.galdebar.monmonscraper.services.scrapers.helpers;

import lt.galdebar.monmonscraper.services.scrapers.ShopNames;
import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RimiParserHelper implements IsHTMLElementParserHelper {
    private final String shopName = ShopNames.RIMI.getShopName();

    @Override
    public ItemOnOffer parseElement(Element element) {
        String name = getItemName(getTitleElement(element));
        String brand = getItemBrand(getTitleElement(element));
        float price = getItemPrice(element);
        return new ItemOnOffer(name, brand, price, shopName);
    }

    @Override
    public float getItemPrice(Element element) {
        float finalPrice;

        if (element.getElementsByClass("price-badge").size() == 0) {
            finalPrice = 0;
            return finalPrice;
        }

        Element priceElement = element
                .getElementsByClass("price-badge").get(0)
                .getElementsByClass("price-badge__inner").get(0);

        String value = priceElement.getElementsByClass("euro").get(0).text();
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

//    private String getItemBrand(Element titleElement) {
//        return cleanCommaFromEndOfString(
//                getTitleWords(titleElement.text(), true)
//        );
//    }
//
//    private String getItemName(Element titleElement) {
//        return cleanCommaFromEndOfString(
//                getTitleWords(titleElement.text(), false)
//        );
//    }

//    private String getTitleWords(String fullTitle, boolean shouldFindCapitalized) {
//        String finalString = "";
//        List<String> usableWords = filterTitleIntoUsableWordsList(fullTitle);
//        List<String> filteredWords = filterTitleWords(usableWords, shouldFindCapitalized);
//
//        if (filteredWords.size() > 0) {
//            StringBuilder stringBuilder = new StringBuilder();
//            for (String word : filteredWords) {
//                stringBuilder.append(word).append(" ");
//            }
//            finalString = stringBuilder.toString().trim();
//            return finalString;
//        } else return finalString;
//    }
//
//    private List<String> filterTitleIntoUsableWordsList(String fullTitle) {
//        List<String> sectionsSeparatedByCommas = generateSectionsSeparatedByCommas(fullTitle);
//
//        return sectionsToUsableWords(sectionsSeparatedByCommas);
//    }
//
//    private List<String> generateSectionsSeparatedByCommas(String fullTitle) {
//        List<String> initialList = new ArrayList<>(Arrays.asList(fullTitle.split(",")));
//        if (initialList.size() > 1) {
//            //Unknown if readding the comma is needed
//            String firstSection = initialList.get(0);
//            firstSection += ",";
//            initialList.set(0, firstSection);
//            List<String> usableSubsectionsAfterFirstComma = filterTitleSubsectionsAfterFirstComma(
//                    initialList.subList(1, initialList.size())
//            );
//
//            initialList = initialList.subList(0, 1);
//            for (String subsection : usableSubsectionsAfterFirstComma) {
//                initialList.add(subsection + ",");
//            }
//
//        }
//
//        return initialList;
//    }
//
//    private List<String> filterTitleSubsectionsAfterFirstComma(List<String> subsections) {
//        List<String> usableSections = subsections.stream()
//                .filter(word -> checkifWordIsOnlyAlphabetic(word))
//                .collect(Collectors.toList());
//        return usableSections;
//    }
//
//    private List<String> filterTitleWords(List<String> unfilteredWords, boolean shouldFindCapitalized) {
//        List<String> filteredWords;
//        if (shouldFindCapitalized) {
//            filteredWords = unfilteredWords.stream()
//                    .filter(word -> isWordUpperCase(word))
//                    .collect(Collectors.toList());
//        } else {
//            filteredWords = unfilteredWords.stream()
//                    .filter(word -> !isWordUpperCase(word))
//                    .collect(Collectors.toList());
//        }
//        return filteredWords;
//    }

    private Element getTitleElement(Element element) {
        return element.getElementsByClass("offer-card__name").get(0);
    }
}
