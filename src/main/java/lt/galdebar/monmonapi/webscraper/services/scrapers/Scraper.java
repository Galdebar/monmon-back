package lt.galdebar.monmonapi.webscraper.services.scrapers;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.webscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonapi.webscraper.services.helpers.translators.HackyGoogleItemTranslator;
import lt.galdebar.monmonapi.webscraper.services.helpers.translators.IsItemTranslator;
import lt.galdebar.monmonapi.webscraper.services.helpers.translators.exceptions.MaxTranslateAttemptsException;
import lt.galdebar.monmonapi.webscraper.services.helpers.translators.exceptions.TooManyRequestsException;
import lt.galdebar.monmonapi.webscraper.services.scrapers.helpers.IsHTMLElementParserHelper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
public abstract class Scraper implements IsWebScraper {
    private final int STAGGER_DURATION_SINGLE_ITEM_MINUTES = 1;
    private final int STAGGER_DURATION_PAUSE_MINUTES = 60;
    private final int MAX_TRANSLATE_ATTEMPTS = 10;
    protected final String URL;
    protected final String CONTAINER_NAME;
    protected final String ITEM_NAME;
    protected static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36";
    protected String sessionID = "";
    @Getter
    protected final ShopNames SHOP;

    protected final IsItemTranslator TRANSLATOR = new HackyGoogleItemTranslator();
    protected final IsHTMLElementParserHelper elementParser;

    protected boolean isDocumentValid;

    @Getter
    protected Document document;


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

    protected List<ShoppingItemDealDTO> elementsToScrapedItems(Elements totalElements) {
        List<ShoppingItemDealDTO> scrapedItems = new ArrayList<>();
        for (Element element : totalElements) {
            scrapedItems.add(elementToScrapedShoppingItem(element));
        }
        return scrapedItems;

    }

    ShoppingItemDealDTO elementToScrapedShoppingItem(Element element) {
        return elementParser.parseElement(element);
    }

    public boolean updateOffersDB() {

        if (isDocumentValid) {
            List<ShoppingItemDealDTO> unprocessedItems;

            try {
                unprocessedItems = getItemsOnOffer();
            } catch (Exception e) {
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
        } else return false;
    }

    private void staggeredTranslateAndPush(List<ShoppingItemDealDTO> itemsOnOffer) throws InterruptedException {
        log.info(this.SHOP + " Running staggered translate and push for " + itemsOnOffer.size() + " items.");
        itemsOnOffer.stream().forEach(itemOnOffer -> {
            try {
                ShoppingItemDealDTO translatedItem = translateSingleItem(itemOnOffer, 1);
                ShoppingItemDealEntity savedEntity = dealsRepo.save(new ShoppingItemDealEntity(translatedItem));
                log.info("Saved item deal: " + savedEntity.toString());
                log.info("Thread going to sleep for " + STAGGER_DURATION_SINGLE_ITEM_MINUTES + " minutes to prevent Http 429 when translating item");
                Thread.sleep(TimeUnit.MINUTES.toMillis(STAGGER_DURATION_SINGLE_ITEM_MINUTES));
            } catch (MaxTranslateAttemptsException e) {
                log.warn("Max attempts at translating a single item reached. Skipping translation of this item: " + itemOnOffer.toString());
                e.printStackTrace();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        });
    }

    private ShoppingItemDealDTO translateSingleItem(ShoppingItemDealDTO dealDTO, int currentAttempt) throws InterruptedException {
        if (currentAttempt >= MAX_TRANSLATE_ATTEMPTS) {
            throw new MaxTranslateAttemptsException();
        }
        try {
            dealDTO = TRANSLATOR.translate(dealDTO);
        } catch (TooManyRequestsException e) {
            log.warn(this.SHOP + " scraper encounterd Http 429 when attempting to translate item. Thread going to sleep for " + STAGGER_DURATION_PAUSE_MINUTES + " minutes");
            Thread.sleep(TimeUnit.MINUTES.toMillis(STAGGER_DURATION_PAUSE_MINUTES));
            dealDTO = translateSingleItem(dealDTO, currentAttempt++);
        } finally {
            return dealDTO;
        }
    }

}
