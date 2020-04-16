package lt.galdebar.monmonscraper.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.jsoup.Connection;
import lt.galdebar.monmonscraper.domain.ScrapedShoppingItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Maxima website scraper.
 */
@Service
public class MaximaScraper {
    private final String CONTAINER_NAME = "offers_container";
    private final String ITEM_NAME = "item";
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36";
    private final int ITEMS_PER_PAGE = 45;
    private String sessionID;

    @Getter
    private Document document;


    private boolean isDocumentValid;

    @Autowired
    private MaximaHTMLElementParserHelper elementParser;

    MaximaScraper() {
        try {
            Connection.Response response = Jsoup.connect("https://www.maxima.lt/akcijos#visi-pasiulymai-1").userAgent(USER_AGENT).execute();
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
    Elements getItemsOnOffer() {
//        return getContainer().getElementsByClass(ITEM_NAME);
        int pagesCount = countPages();
        Elements totalElements = new Elements();
        for (int i = 0; i < pagesCount; i++) {
            Elements fetchedPage = fetchItemsWithOffset(i);
            totalElements.addAll(fetchedPage);
        }
//        return fetchItemsWithOffset();
        return totalElements;
    }

    Elements getItemsOnOffer(Document document) {
        return document.getElementsByClass(ITEM_NAME);
    }

    /**
     * Create item scraped shopping item.
     *
     * @param element the element
     * @return the scraped shopping item
     */
    ScrapedShoppingItem createItem(Element element) {
        return elementParser.parseElement(element);
    }


    public int countPages() {
        int pagesCount = 0;
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

    public Elements fetchItemsWithOffset(int pagesCount) {
        String url = "https://www.maxima.lt/ajax/saleloadmore";
        Document fetchedDoc = null;
        int offset = pagesCount * ITEMS_PER_PAGE;

        try {
            Connection.Response response = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .method(Connection.Method.POST)
                    .data("orderBy","discount","offset",Integer.toString(offset))
                    .cookie("SESSIONID", sessionID)
                    .execute();
            String responseString = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseString);
            String htmlString = jsonNode.get("html").asText();
            fetchedDoc = Jsoup.parse(htmlString);
        } catch (IOException e) {
            e.printStackTrace();
            return new Elements();
        }

        Elements items = getItemsOnOffer(fetchedDoc);
        return items;
    }
}
