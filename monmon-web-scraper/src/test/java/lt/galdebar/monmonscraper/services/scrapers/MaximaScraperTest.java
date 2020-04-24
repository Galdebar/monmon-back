package lt.galdebar.monmonscraper.services.scrapers;

import lt.galdebar.monmonscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonscraper.services.scrapers.helpers.MaximaParserHelper;
import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static lt.galdebar.monmonscraper.services.testhelpers.GetItemsCountFromWebsites.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
//@ActiveProfiles("test")
public class MaximaScraperTest {

    @org.springframework.boot.test.context.TestConfiguration
    public static class TestConfiguration {
        @Bean
        public MaximaScraper fullFileMaximaScraper() {
            File localfile = new File("src/test/resources/WebsiteSnapshots/Maxima_Full.html");
            Document doc = null;
            try {
                doc = Jsoup.parse(localfile, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new MaximaScraper(doc);
        }

    }

    @Autowired
    private MaximaScraper fullFileMaximaScraper;

    @Autowired
    private MaximaScraper maximaScraper;

    @Autowired
    private ShoppingItemDealsRepo dealsRepo;

    @Before
    public void setUp() throws Exception {
        dealsRepo.deleteAll();
    }

    @AfterEach
    public void tearDown() throws Exception {
        dealsRepo.deleteAll();
    }

    @Test
    public void givenContext_thenLoadContext() {
        assertNotNull(fullFileMaximaScraper);
    }

    @Test
    public void givenContext_whenIsValid_thenTrue() {
        assertTrue(fullFileMaximaScraper.isValid());
    }

    @Test
    public void givenInvalidAddress_whenIsValid_thenFalse() {
        MaximaScraper scraper = new MaximaScraper(new Document("Some random string which should fail"));
        assertFalse(scraper.isValid());
    }

    @Test
    public void givenValidContext_whenGetDocumentTitle_thenNotNull() {
        assertNotNull(fullFileMaximaScraper.getTitle());
    }


    @Test
    public void givenValidContext_whenGetContainer_thenNotNull() {
        Element container = fullFileMaximaScraper.getContainer();
        assertNotNull(container);
    }

    @Test
    public void givenValidFile_whenGetNumberOfRequiredRequests_thenReturnCorrectCount() {
        int singlePageItemsCount = 45;
        int totalItems = 328;
        int expectedCount = 8;
        int actualCount = fullFileMaximaScraper.countPages();

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
        List<ItemOnOffer> fetchedElements = maximaScraper.fetchItemsWithOffset(0);

        assertNotNull(fetchedElements);
    }

    @Test
    public void givenValidFile_whenGetItemsOnOffer_thenReturnCount() {
        int expectedCount = 328;
        List<ItemOnOffer> actualIterable = fullFileMaximaScraper.getItemsOnOffer();

        assertNotNull(actualIterable);
        assertEquals(actualIterable.size(), expectedCount);
    }

    @Test
    public void givenActualWebsite_whenGetItemsOnOffer_thenReturnCount() {
        int expectedCount = getTotalItemsFromMaxima();
        int actualCount = maximaScraper.getItemsOnOffer().size();

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
        ItemOnOffer actualItem = fullFileMaximaScraper.elementToScrapedShoppingItem(
                fullFileMaximaScraper.getDocument().getElementsByClass("item").get(expectedItemIndex)
        );

        assertNotNull(actualItem);
        assertTrue(actualItem.getName().equalsIgnoreCase(expectedItemName));
        assertTrue(actualItem.getBrand().equalsIgnoreCase(expectedItemBrand));
        assertEquals(expectedItemPrice, actualItem.getPrice(), 0.0);
    }

    @Test
    public void givenValidWebsite_whenUpdateOffersDB_thenDBUpdated(){
        boolean isPushSuccessful = maximaScraper.updateOffersDB();
        List<ShoppingItemDealEntity> foundDeals = dealsRepo.findAll();

        assertTrue(isPushSuccessful);
        assertNotNull(foundDeals);
        assertEquals(
                getTotalItemsFromMaxima(),
                foundDeals.size()
        );
    }

    // push to db with valid website
    //push to db with invalid website
}
