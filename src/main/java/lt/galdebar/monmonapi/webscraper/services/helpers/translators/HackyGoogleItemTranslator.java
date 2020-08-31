package lt.galdebar.monmonapi.webscraper.services.helpers.translators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.services.helpers.translators.exceptions.TooManyRequestsException;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HackyGoogleItemTranslator implements IsItemTranslator {
    private final String URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=lt&tl=en&dt=t&q=";
    private final String[] wordsToFilter = {"for"};
    private final int REQUESTDELAY = 5;

    public List<ShoppingItemDealDTO> translate(List<ShoppingItemDealDTO> itemsToTranslate) {
        List<ShoppingItemDealDTO> translatedItems = new ArrayList<>();
        for (ShoppingItemDealDTO itemToTranslate : itemsToTranslate) {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(REQUESTDELAY));
                translatedItems.add(translate(itemToTranslate));
            } catch (InterruptedException e) {
                e.printStackTrace();
                return translatedItems;
            }
        }
        return translatedItems;
    }

    public ShoppingItemDealDTO translate(ShoppingItemDealDTO itemToTranslate) {
        String translatedName = translateString(itemToTranslate.getTitle());

        return new ShoppingItemDealDTO(
                translatedName,
                itemToTranslate.getBrand(),
                itemToTranslate.getShopTitle(),
                itemToTranslate.getPrice()
        );
    }

    public String translateString(String string) {
        String url = URL + string;
        String translatedName = string;
        try {
            Connection.Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .execute();
            String responseString = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseString);
            translatedName = jsonNode.get(0).get(0).get(0).asText();
        } catch (HttpStatusException statusException) {
            if (statusException.getStatusCode() == 429) {
                throw new TooManyRequestsException();
            } else {
                statusException.printStackTrace();
                translatedName = string;
            }
        } catch (IOException e) {
            e.printStackTrace();
            translatedName = string;
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
