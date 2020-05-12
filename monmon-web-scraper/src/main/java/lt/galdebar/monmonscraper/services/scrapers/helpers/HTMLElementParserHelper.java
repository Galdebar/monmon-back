package lt.galdebar.monmonscraper.services.scrapers.helpers;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class HTMLElementParserHelper implements IsHTMLElementParserHelper {
    protected final String shopName;

    public HTMLElementParserHelper(String shopName) {
        this.shopName = shopName;
    }

    public ItemOnOffer parseElement(Element element) {
        String name = getItemName(getTitleElement(element));
        String brand = getItemBrand(getTitleElement(element));
        float price = getItemPrice(element);
        return new ItemOnOffer(name, brand, price, shopName);
    }

    public abstract Element getTitleElement(Element element);

    public abstract float getItemPrice(Element element);

    public String getItemBrand(Element titleElement) {
        return cleanCommaFromEndOfString(
                getTitleWords(titleElement.text(), true)
        );
    }

    public String getItemName(Element titleElement) {
        return cleanCommaFromEndOfString(
                getTitleWords(titleElement.text(), false)
        );
    }


    String getTitleWords(String fullTitle, boolean shouldFindCapitalized) {
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

    List<String> filterTitleIntoUsableWordsList(String fullTitle) {
        List<String> sectionsSeparatedByCommas = generateSectionsSeparatedByCommas(fullTitle);

        return sectionsToUsableWords(sectionsSeparatedByCommas);
    }

    List<String> sectionsToUsableWords(List<String> wordSections) {
        List<String> separatedBySpaces = Arrays.asList(
                wordSections.stream()
                        .collect(Collectors.joining(" "))
                        .split(" ")
        );
        List<String> finalList = separatedBySpaces.stream()
                .filter(word -> !word.isEmpty())
                .filter(this::checkifWordIsOnlyAlphabetic)
                .collect(Collectors.toList());

        return finalList;
    }

    List<String> generateSectionsSeparatedByCommas(String fullTitle) {
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

    List<String> filterTitleSubsectionsAfterFirstComma(List<String> subsections) {
        List<String> usableSections = subsections.stream()
                .filter(this::checkifWordIsOnlyAlphabetic)
                .collect(Collectors.toList());
        return usableSections;
    }

    List<String> filterTitleWords(List<String> unfilteredWords, boolean shouldFindCapitalized) {
        List<String> filteredWords;
        if (shouldFindCapitalized) {
            filteredWords = unfilteredWords.stream()
                    .filter(this::isWordUpperCase)
                    .collect(Collectors.toList());
        } else {
            filteredWords = unfilteredWords.stream()
                    .filter(word -> !isWordUpperCase(word))
                    .collect(Collectors.toList());
        }
        return filteredWords;
    }

    protected String cleanCommaFromEndOfString(String string) {
        if (!string.isEmpty() && string.charAt(string.length() - 1) == ',') {
            return string.substring(0, string.length() - 1);
        } else return string;
    }

    protected boolean isWordUpperCase(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (Character.isLowerCase(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkifWordIsOnlyAlphabetic(String word) {
        for (char c : word.toCharArray()) {
            if (!Character.isDigit(c) && c != '.' || c == ' ') {
                continue;
            }
            return false;
        }
        return true;
    }
}
