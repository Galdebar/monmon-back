package lt.galdebar.monmonscraper.services.scrapers.helpers;

public class CommonHTMLElementParserMethods {
    boolean isWordUpperCase(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (Character.isLowerCase(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    String cleanCommaFromEndOfString(String string) {
        if (!string.isEmpty() && string.charAt(string.length() - 1) == ',') {
            return string.substring(0, string.length() - 1);
        } else return string;
    }

    boolean checkifWordIsOnlyAlphabetic(String word) {
        for (char c : word.toCharArray()) {
            if (!Character.isDigit(c)  && c!='.' || c == ' ') {
                continue;
            }
            return false;
        }
        return true;
    }
}
