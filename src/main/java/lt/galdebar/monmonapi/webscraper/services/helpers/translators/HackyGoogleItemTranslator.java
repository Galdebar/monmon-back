package lt.galdebar.monmonapi.webscraper.services.helpers.translators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.galdebar.monmonapi.webscraper.services.helpers.translators.exceptions.TooManyRequestsException;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HackyGoogleItemTranslator implements IsItemTranslator {
    private final String URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=lt&tl=en&dt=t&q=";
    private final String[] wordsToFilter = {"for"};
    private final int REQUESTDELAY = 5;

    public List<ItemOnOffer> translate(List<ItemOnOffer> itemsToTranslate) {
        List<ItemOnOffer> translatedItems = new ArrayList<>();
        for (ItemOnOffer itemToTranslate : itemsToTranslate) {
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

    public ItemOnOffer translate(ItemOnOffer itemToTranslate) {
        String translatedName = translateString(itemToTranslate.getName());

        return new ItemOnOffer(
                translatedName,
                itemToTranslate.getBrand(),
                itemToTranslate.getPrice(),
                itemToTranslate.getShopName()
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
