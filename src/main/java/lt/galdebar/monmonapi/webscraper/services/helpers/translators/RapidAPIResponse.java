package lt.galdebar.monmonapi.webscraper.services.helpers.translators;

import lombok.Data;

@Data
public class RapidAPIResponse {
    private Data data;

    @lombok.Data
    static class Data{
        private Translations[] translations;
    }

    @lombok.Data
    static class Translations{
     private String translatedText;
    }
}
