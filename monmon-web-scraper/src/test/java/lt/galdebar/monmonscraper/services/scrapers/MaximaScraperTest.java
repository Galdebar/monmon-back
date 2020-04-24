package lt.galdebar.monmonscraper.services.scrapers;

import lt.galdebar.monmonscraper.services.scrapers.helpers.MaximaParserHelper;
import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static lt.galdebar.monmonscraper.services.testhelpers.GetItemsCountFromWebsites.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class MaximaScraperTest {

    @org.springframework.boot.test.context.TestConfiguration
    public static class TestConfiguration {
        @Bean
        public MaximaScraper maximaScraper() {
            File localfile = new File("src/test/resources/WebsiteSnapshots/Maxima_Full.html");
            Document doc = null;
            try {
                doc = Jsoup.parse(localfile, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new MaximaScraper(doc);
        }

        @Bean
        public MaximaScraper actualMaximaScraper(){
            return new MaximaScraper();
        }

        @Bean
        public MaximaParserHelper maximaHTMLElementParserHelper() {
            return new MaximaParserHelper();
        }

    }

    @Autowired
    private MaximaScraper maximaScraper;

    @Autowired
    private MaximaScraper actualMaximaScraper;

    @Test
    public void givenContext_thenLoadContext() {
        assertNotNull(maximaScraper);
    }

    @Test
    public void givenContext_whenIsValid_thenTrue() {
        assertTrue(maximaScraper.isValid());
    }

    @Test
    public void givenInvalidAddress_whenIsValid_thenFalse() {
        MaximaScraper scraper = new MaximaScraper(new Document("Some random string which should fail"));
        assertFalse(scraper.isValid());
    }

    @Test
    public void givenValidContext_whenGetDocumentTitle_thenNotNull() {
        assertNotNull(maximaScraper.getTitle());
    }


    @Test
    public void givenValidContext_whenGetContainer_thenNotNull() {
        Element container = maximaScraper.getContainer();
        assertNotNull(container);
    }

    @Test
    public void givenValidFile_whenGetNumberOfRequiredRequests_thenReturnCorrectCount() {
        int singlePageItemsCount = 45;
        int totalItems = 328;
        int expectedCount = 8;
        int actualCount = maximaScraper.countPages();

        assertEquals(expectedCount, actualCount);
    }

    @Test
    public void givenActualWebsite_whenGetNumberOfRequiredRequests_thenReturnCorrectCount() {
        MaximaScraper scraper = new MaximaScraper(); // default constructor has hardwired url.
        int singlePageItemsCount = 45;
        int totalItemsCount = getTotalItemsFromMaxima();
        int expectedPagesCount = (totalItemsCount % singlePageItemsCount == 0) ? totalItemsCount / singlePageItemsCount : totalItemsCount / singlePageItemsCount + 1;
        int actualPagesCount = scraper.countPages();

        assertNotEquals(0, expectedPagesCount);
        assertEquals(expectedPagesCount, actualPagesCount);
    }

    @Test
    public void givenValidFile_whenFetchItemsWithOffset_thenNotNull() {
        MaximaScraper scraper = new MaximaScraper();
        List<ItemOnOffer> fetchedElements = actualMaximaScraper.fetchItemsWithOffset(0);

        assertNotNull(fetchedElements);
    }

    @Test
    public void givenValidFile_whenGetItemsOnOffer_thenReturnCount() {
        int expectedCount = 328;
        List<ItemOnOffer> actualIterable = maximaScraper.getItemsOnOffer();

        assertNotNull(actualIterable);
        assertEquals(actualIterable.size(), expectedCount);
    }

    @Test
    public void givenActualWebsite_whenGetItemsOnOffer_thenReturnCount() {
        int expectedCount = getTotalItemsFromMaxima();
        int actualCount = actualMaximaScraper.getItemsOnOffer().size();

        assertNotEquals(0, expectedCount);
        assertEquals(expectedCount, actualCount);
    }

    @Test
    public void givenValidFile_whenCreateItemFromElement_thenReturnItem() {
        int expectedItemIndex = 2;
        String expectedItemName = "sviestas";
        String expectedItemBrand = "ROKIŠKIO";
        float expectedItemPrice = 1.09f;
        ItemOnOffer expectedItem = new ItemOnOffer(expectedItemName, expectedItemBrand, expectedItemPrice, "ShopName");
        ItemOnOffer actualItem = maximaScraper.elementToScrapedShoppingItem(
                maximaScraper.getDocument().getElementsByClass("item").get(expectedItemIndex)
        );

        assertNotNull(actualItem);
        assertTrue(actualItem.getName().equalsIgnoreCase(expectedItemName));
        assertTrue(actualItem.getBrand().equalsIgnoreCase(expectedItemBrand));
        assertEquals(expectedItemPrice, actualItem.getPrice(), 0.0);
    }

//    @Test
//    void givenValidWebsite_whenUpdateOffersDB_thenDBUpdated(){
//        MaximaScraper scraper = new MaximaScraper();
//        List<ShoppingItemDealEntity> foundDeals = dealsRepo.findAll();
//        assertNotNull(foundDeals);
//        assertEquals(
//                getTotalItemsFromMaxima(),
//                foundDeals.size()
//        );
//    }

    // push to db with valid website
    //push to db with invalid website
}
