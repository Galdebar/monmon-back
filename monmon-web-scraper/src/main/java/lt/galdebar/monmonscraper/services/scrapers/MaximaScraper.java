package lt.galdebar.monmonscraper.services.scrapers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lt.galdebar.monmonscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonscraper.services.helpers.AssignKeywordHelper;
import lt.galdebar.monmonscraper.services.helpers.ItemTranslator;
import lt.galdebar.monmonscraper.services.helpers.ShoppingIitemDealAdapter;
import lt.galdebar.monmonscraper.services.scrapers.helpers.MaximaParserHelper;
import org.jsoup.Connection;
import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Maxima website scraper.
 */
@Service
public class MaximaScraper implements IsWebScraper {
    private final String CONTAINER_NAME = "offers_container";
    private final String ITEM_NAME = "item";
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36";
    private final int ITEMS_PER_PAGE = 45;
    private String sessionID = "";

    private final ItemTranslator TRANSLATOR = new ItemTranslator();
    private final ShoppingIitemDealAdapter ADAPTER = new ShoppingIitemDealAdapter();

    @Getter
    private Document document;


    private boolean isDocumentValid;

    @Autowired
    private MaximaParserHelper elementParser;


    @Autowired
    private AssignKeywordHelper assignKeywordHelper;

    @Autowired
    private ShoppingItemDealsRepo dealsRepo;

    public MaximaScraper() {
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

    public boolean updateOffersDB() {
        if (isDocumentValid) {
            List<ItemOnOffer> unprocessedItems = getItemsOnOffer();
            List<ItemOnOffer> translatedItems = TRANSLATOR.translate(unprocessedItems);
            List<ShoppingItemDealDTO> finalDeals= assignKeywordHelper.assignKeywords(translatedItems);
            List<ShoppingItemDealEntity> returnedEntities = dealsRepo.saveAll(ADAPTER.dtoToEntity(finalDeals));
            if(returnedEntities.size()==finalDeals.size()){
                return true;
            } else return false;
        } else return false;
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
//        List<ItemOnOffer> scrapedItems = elementsToScrapedItems(totalElements);
        return totalElements;
    }

    public List<ItemOnOffer> getItemsOnOffer(Document document) {
        Elements elements = document.getElementsByClass(ITEM_NAME);
        return elementsToScrapedItems(elements);
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

    public List<ItemOnOffer> fetchItemsWithOffset(int pagesCount) {
        String url = "https://www.maxima.lt/ajax/saleloadmore";
        Document fetchedDoc = null;
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

        List<ItemOnOffer> items = getItemsOnOffer(fetchedDoc);
        return items;
    }

    private List<ItemOnOffer> elementsToScrapedItems(Elements totalElements) {
        List<ItemOnOffer> scrapedItems = new ArrayList<>();
        for (Element element : totalElements) {
            scrapedItems.add(elementToScrapedShoppingItem(element));
        }
        return scrapedItems;

    }

    /**
     * Create item scraped shopping item.
     *
     * @param element the element
     * @return the scraped shopping item
     */
    ItemOnOffer elementToScrapedShoppingItem(Element element) {
        return elementParser.parseElement(element);
    }
}
