package lt.galdebar.monmonapi.webscraper.services.scrapers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.galdebar.monmonapi.webscraper.services.scrapers.helpers.MaximaParserHelper;
import org.jsoup.Connection;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Maxima website scraper.
 */
//@Component
public class MaximaScraper extends Scraper {
    private final int ITEMS_PER_PAGE = 45;

    public MaximaScraper() {
        super(
                "https://www.maxima.lt/akcijos#visi-pasiulymai-1",
                "offers_container",
                "item",
                ShopNames.MAXIMA,
                new MaximaParserHelper()
        );
        try {
            Connection.Response response = Jsoup.connect(URL).userAgent(USER_AGENT).maxBodySize(0).execute();
            document = response.parse();
            sessionID = response.cookie("SESSIONID");
            isDocumentValid = true;
        } catch (IOException e) {
            e.printStackTrace();
            isDocumentValid = false;
        }
    }

    /**
     * Instantiates a new Maxima scraper.
     *
     * @param doc the doc
     */
    MaximaScraper(Document doc) {
        super(
                "https://www.maxima.lt/akcijos#visi-pasiulymai-1",
                "offers_container",
                "item",
                ShopNames.MAXIMA,
                new MaximaParserHelper()
        );
        if (doc.childNodes().size() > 0) {
            document = doc;
            isDocumentValid = true;
        } else isDocumentValid = false;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    String getTitle() {
        if (isDocumentValid) {
            return document.title();
        } else return "";
    }

    /**
     * Is valid boolean.
     *
     * @return the boolean
     */
    public boolean isValid() {
        return isDocumentValid;
    }

    /**
     * Gets container.
     *
     * @return the container
     */
    Element getContainer() {
        return document.getElementById(CONTAINER_NAME);
    }

    Element getContainer(Document document) {
        return document.getElementById(CONTAINER_NAME);
    }

    /**
     * Gets items on offer.
     *
     * @return the items on offer
     */
    public List<ItemOnOffer> getItemsOnOffer() {
        if (sessionID.trim().isEmpty()) {

            Elements elements = document.getElementsByClass(ITEM_NAME);
            return elementsToScrapedItems(elements);
        }
        int pagesCount = countPages();
        List<ItemOnOffer> totalElements = new ArrayList<>();
        for (int i = 0; i < pagesCount; i++) {
            List<ItemOnOffer> fetchedPage = fetchItemsWithOffset(i);
            totalElements.addAll(fetchedPage);
        }
        return totalElements;
    }

    public List<ItemOnOffer> getItemsOnOffer(Document document) {
        Elements elements = document.getElementsByClass(ITEM_NAME);
        return elementsToScrapedItems(elements);
    }


    int countPages() {
        int pagesCount;
        int totalItemsCount;
        try {
            totalItemsCount = Integer.parseInt(document.getElementById("items_cnt").text());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            totalItemsCount = 0;
        }
        if (totalItemsCount % ITEMS_PER_PAGE > 0) {
            pagesCount = (totalItemsCount / ITEMS_PER_PAGE) + 1;
        } else pagesCount = totalItemsCount / ITEMS_PER_PAGE;

        return pagesCount;
    }

    List<ItemOnOffer> fetchItemsWithOffset(int pagesCount) {
        String url = "https://www.maxima.lt/ajax/saleloadmore";
        Document fetchedDoc;
        int offset = pagesCount * ITEMS_PER_PAGE;

        try {
            Connection.Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .method(Connection.Method.POST)
                    .data("orderBy", "discount", "offset", Integer.toString(offset))
                    .cookie("SESSIONID", sessionID)
                    .execute();
            String responseString = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseString);
            String htmlString = jsonNode.get("html").asText();
            fetchedDoc = Jsoup.parse(htmlString);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return getItemsOnOffer(fetchedDoc);
    }
}
