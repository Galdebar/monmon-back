package lt.galdebar.monmonscraper.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.galdebar.monmonscraper.domain.ScrapedShoppingItem;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service
public class ItemTranslator {
    private final String URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=lt&tl=en&dt=t&q=";
    private final String[] wordsToFilter = {"for"};

    public ScrapedShoppingItem translate(ScrapedShoppingItem itemToTranslate) {
        String translatedName = translateString(itemToTranslate.getName());

        return new ScrapedShoppingItem(
                translatedName,
                itemToTranslate.getBrand(),
                itemToTranslate.getPrice()
        );
    }

    private String translateString(String name) {
        String url = URL + name;
        String translatedName = name;
        try {
            Connection.Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .execute();
            String responseString = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseString);
            translatedName = jsonNode.get(0).get(0).get(0).asText();
        } catch (IOException e) {
            e.printStackTrace();
            translatedName = name;
        } finally {
            return filterString(translatedName);
        }
    }

    private String filterString(String string) {
        String[] words = string.split(" ");
        for (String filterWord : wordsToFilter) {
            if (words[0].trim().equalsIgnoreCase(filterWord)) {
                words = Arrays.copyOfRange(words, 1, words.length);
            }
        }

        return String.join(" ", words);
    }
}
