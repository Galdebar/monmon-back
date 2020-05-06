package lt.galdebar.monmonscraper.services.scrapers.helpers;

import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface IsHTMLElementParserHelper {
    ItemOnOffer parseElement(Element element);
    float getItemPrice(Element element);

    default String getItemBrand(Element titleElement) {
        return cleanCommaFromEndOfString(
                getTitleWords(titleElement.text(), true)
        );
    }

    default String getItemName(Element titleElement) {
        return cleanCommaFromEndOfString(
                getTitleWords(titleElement.text(), false)
        );
    }


    default List<String> sectionsToUsableWords(List<String> wordSections) {
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

    default String getTitleWords(String fullTitle, boolean shouldFindCapitalized) {
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

    default List<String> filterTitleIntoUsableWordsList(String fullTitle) {
        List<String> sectionsSeparatedByCommas = generateSectionsSeparatedByCommas(fullTitle);

        return sectionsToUsableWords(sectionsSeparatedByCommas);
    }

    default List<String> generateSectionsSeparatedByCommas(String fullTitle) {
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

    default List<String> filterTitleSubsectionsAfterFirstComma(List<String> subsections) {
        List<String> usableSections = subsections.stream()
                .filter(this::checkifWordIsOnlyAlphabetic)
                .collect(Collectors.toList());
        return usableSections;
    }

    default List<String> filterTitleWords(List<String> unfilteredWords, boolean shouldFindCapitalized) {
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

    default String cleanCommaFromEndOfString(String string) {
        if (!string.isEmpty() && string.charAt(string.length() - 1) == ',') {
            return string.substring(0, string.length() - 1);
        } else return string;
    }

    default boolean isWordUpperCase(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (Character.isLowerCase(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    default boolean checkifWordIsOnlyAlphabetic(String word) {
        for (char c : word.toCharArray()) {
            if (!Character.isDigit(c)  && c!='.' || c == ' ') {
                continue;
            }
            return false;
        }
        return true;
    }
}
