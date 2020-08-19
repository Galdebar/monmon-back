package lt.galdebar.monmonapi.webscraper.services.scrapers;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.webscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonapi.webscraper.services.helpers.AssignKeywordHelper;
import lt.galdebar.monmonapi.webscraper.services.helpers.ItemTranslator;
import lt.galdebar.monmonapi.webscraper.services.helpers.ShoppingIitemDealAdapter;
import lt.galdebar.monmonapi.webscraper.services.scrapers.helpers.IsHTMLElementParserHelper;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
public abstract class Scraper implements IsWebScraper {
    protected final String URL;
    protected final String CONTAINER_NAME;
    protected final String ITEM_NAME;
    protected static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36";
    protected String sessionID = "";
    @Getter
    protected final ShopNames SHOP;

    protected final ItemTranslator TRANSLATOR = new ItemTranslator();
    protected final ShoppingIitemDealAdapter ADAPTER = new ShoppingIitemDealAdapter();
    protected final IsHTMLElementParserHelper elementParser;

    protected boolean isDocumentValid;

    @Getter
    protected Document document;

    @Autowired
    protected AssignKeywordHelper assignKeywordHelper;

    @Autowired
    protected ShoppingItemDealsRepo dealsRepo;

    protected Scraper(String url, String container_name, String item_name, ShopNames shop, IsHTMLElementParserHelper elementParser) {
        this.URL = url;
        CONTAINER_NAME = container_name;
        ITEM_NAME = item_name;
        SHOP = shop;
        this.elementParser = elementParser;
    }

    @Override
    public boolean isValid() {
        return isDocumentValid;
    }

    protected List<ItemOnOffer> elementsToScrapedItems(Elements totalElements) {
        List<ItemOnOffer> scrapedItems = new ArrayList<>();
        for (Element element : totalElements) {
            scrapedItems.add(elementToScrapedShoppingItem(element));
        }
        return scrapedItems;

    }

    ItemOnOffer elementToScrapedShoppingItem(Element element) {
        return elementParser.parseElement(element);
    }

    public boolean updateOffersDB() {

        if(isDocumentValid){
            List<ItemOnOffer> unprocessedItems;

            try {
                unprocessedItems = getItemsOnOffer();
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }

            try {
                staggeredTranslateAndPush(unprocessedItems);
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }else return false;
    }

    //This method is a bad necessity, because I'm not using Google's translate API.
    //So the translator allowance is 100 requests per hour.
    private void staggeredTranslateAndPush(List<ItemOnOffer> itemsOnOffer) throws InterruptedException {
    log.info(this.SHOP + " Running staggered translate and push");
    log.info(itemsOnOffer.toString());
        int maxItemsInBatch = 50;
        int numOfBatches = (itemsOnOffer.size() % maxItemsInBatch == 0) ?
                (itemsOnOffer.size() / maxItemsInBatch) : (itemsOnOffer.size() / maxItemsInBatch + 1);

        for (int i = 0; i < numOfBatches; i++) {
            int startIndex = maxItemsInBatch * i;
            int endIndex = Math.min(itemsOnOffer.size(), (startIndex + maxItemsInBatch)) - 1;
            List<ItemOnOffer> trimmedList = itemsOnOffer.subList(startIndex, endIndex);

            List<ItemOnOffer> translatedItems = TRANSLATOR.translate(trimmedList);
            List<ShoppingItemDealDTO> finalDeals = assignKeywordHelper.assignKeywords(translatedItems);
            List<ShoppingItemDealEntity> entities = dealsRepo.saveAll(ADAPTER.dtoToEntity(finalDeals));
            System.out.println(entities);
            Thread.sleep(TimeUnit.HOURS.toMillis(1));
        }
    }

}
