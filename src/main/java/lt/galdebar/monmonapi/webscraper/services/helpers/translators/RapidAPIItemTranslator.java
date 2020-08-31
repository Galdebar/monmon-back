package lt.galdebar.monmonapi.webscraper.services.helpers.translators;

import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RapidAPIItemTranslator implements IsItemTranslator {
    private final String URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=lt&tl=en&dt=t&q=";
    private final String[] wordsToFilter = {"for"};
    private final int REQUESTDELAY = 5;

    @Autowired
    private RestTemplate restTemplate;


    private final HttpEntity requestEntity;
    private final String BODY_START="source=lt&q=";
    private final String BODY_END="&target=en";

    public RapidAPIItemTranslator() {
        restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-rapidapi-host", "google-translate1.p.rapidapi.com");
        headers.set("x-rapidapi-key", "c450774140msh9aeccdba42b7606p1b79bfjsn2e70fe87f8f1");
        headers.set("accept-encoding", "application/gzip");
        headers.set("content-type", "application/x-www-form-urlencoded");
        requestEntity = new HttpEntity(headers);
    }

    public List<ShoppingItemDealDTO> translate(List<ShoppingItemDealDTO> itemsToTranslate) {
        List<ShoppingItemDealDTO> translatedItems = new ArrayList<>();
        for(ShoppingItemDealDTO itemToTranslate:itemsToTranslate){
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

        String translatedName = string;

        try {

            String requestBody = BODY_START + string + BODY_END;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("x-rapidapi-host", "google-translate1.p.rapidapi.com");
            headers.set("x-rapidapi-key", "c450774140msh9aeccdba42b7606p1b79bfjsn2e70fe87f8f1");
            headers.set("accept-encoding", "application/gzip");
            headers.set("content-type", "application/x-www-form-urlencoded");

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);


            RapidAPIResponse response = restTemplate.postForObject(
                    "https://google-translate1.p.rapidapi.com/language/translate/v2",
                    request,
                    RapidAPIResponse.class
            );
            translatedName = response.getData().getTranslations()[0].getTranslatedText();
        }catch (Exception e){
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
