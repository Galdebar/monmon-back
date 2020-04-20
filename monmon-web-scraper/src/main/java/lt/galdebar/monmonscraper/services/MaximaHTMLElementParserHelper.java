package lt.galdebar.monmonscraper.services;

import lt.galdebar.monmonscraper.domain.ScrapedShoppingItem;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
class MaximaHTMLElementParserHelper {

    ScrapedShoppingItem parseElement(Element element) {
        String name = getItemName(getTitleElement(element));
        String brand = getItemBrand(getTitleElement(element));
        float price = getItemPrice(element);
        ScrapedShoppingItem itemToReturn;
        if(name.equalsIgnoreCase("")) {
            return new ScrapedShoppingItem(brand, "", price);
        } else return new ScrapedShoppingItem(name,brand,price);
    }

    private String getItemName(Element titleElement) {
        return cleanCommaFromEndOfString(
                getTitleWords(titleElement.text(), false)
        );
    }

    private String getItemBrand(Element titleElement) {
        return cleanCommaFromEndOfString(
                getTitleWords(titleElement.text(), true)
        );
    }

    private float getItemPrice(Element element) {
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

    private String getTitleWords(String fullTitle, boolean shouldFindCapitalized) {
        String finalString = "";
        List<String> usableWords = filterTitleIntoUsableWordsList(fullTitle);
        List<String> filteredWords = filterTitleWords(usableWords, shouldFindCapitalized);

        if (filteredWords.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String word : filteredWords) {
                stringBuilder.append(word).append(" ");
            }
            finalString = stringBuilder.toString().trim();
            return finalString;
        } else return finalString;
    }

    private List<String> filterTitleIntoUsableWordsList(String fullTitle) {
        List<String> sectionsSeparatedByCommas = generateSectionsSeparatedByCommas(fullTitle);

        return sectionsToUsableWords(sectionsSeparatedByCommas);
    }

    private List<String> sectionsToUsableWords(List<String> wordSections) {
        List<String> separatedBySpaces = Arrays.asList(
                wordSections.stream()
                        .collect(Collectors.joining(" "))
                        .split(" ")
        );
        List<String> finalList = separatedBySpaces.stream()
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toList());

        return finalList;
    }

    private List<String> generateSectionsSeparatedByCommas(String fullTitle) {
        List<String> initialList = new ArrayList<>(Arrays.asList(fullTitle.split(",")));
        if (initialList.size() > 1) {
            //Unknown if readding the comma is needed
            String firstSection = initialList.get(0);
            firstSection += ",";
            initialList.set(0, firstSection);
            List<String> usableSubsectionsAfterFirstComma = filterTitleSubsectionsAfterFirstComma(
                    initialList.subList(1, initialList.size())
            );

            initialList = initialList.subList(0, 1);
            for (String subsection : usableSubsectionsAfterFirstComma) {
                initialList.add(subsection + ",");
            }

        }

        return initialList;
    }

    private List<String> filterTitleSubsectionsAfterFirstComma(List<String> subsections) {
        List<String> usableSections = subsections.stream()
                .filter(word -> checkifWordIsOnlyAlphabetic(word))
                .collect(Collectors.toList());
        return usableSections;
    }

    private boolean checkifWordIsOnlyAlphabetic(String word) {
        for (char c : word.toCharArray()) {
            if (!Character.isDigit(c)  && c!='.' || c == ' ') {
                continue;
            }
            return false;
        }
        return true;
    }

    private List<String> filterTitleWords(List<String> unfilteredWords, boolean shouldFindCapitalized) {
        List<String> filteredWords;
        if (shouldFindCapitalized) {
            filteredWords = unfilteredWords.stream()
                    .filter(word -> isWordUpperCase(word))
                    .collect(Collectors.toList());
        } else {
            filteredWords = unfilteredWords.stream()
                    .filter(word -> !isWordUpperCase(word))
                    .collect(Collectors.toList());
        }
        return filteredWords;
    }

    private boolean isWordUpperCase(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (Character.isLowerCase(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String cleanCommaFromEndOfString(String string) {
        if (!string.isEmpty() && string.charAt(string.length() - 1) == ',') {
            return string.substring(0, string.length() - 1);
        } else return string;
    }
}
