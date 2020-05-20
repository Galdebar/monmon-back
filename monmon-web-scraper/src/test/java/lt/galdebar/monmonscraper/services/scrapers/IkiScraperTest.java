package lt.galdebar.monmonscraper.services.scrapers;

import lt.galdebar.monmonscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static lt.galdebar.monmonscraper.services.testhelpers.GetItemsCountFromWebsites.getTotalItemsFromIki;
import static lt.galdebar.monmonscraper.services.testhelpers.GetItemsCountFromWebsites.getTotalItemsFromMaxima;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class IkiScraperTest {

    @org.springframework.boot.test.context.TestConfiguration
    public static class TestConfiguration {
        @Bean
        public IkiScraper fullFileIkiScraper() {
            File localfile = new File("src/test/resources/WebsiteSnapshots/IKI_Full.html");
            Document doc = null;
            try {
                doc = Jsoup.parse(localfile, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new IkiScraper(doc);
        }

    }

    @Autowired
    private IkiScraper fullFileIkiScraper;

    private IkiScraper ikiScraper = new IkiScraper();

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
        assertNotNull(fullFileIkiScraper);
        assertNotNull(ikiScraper);
        assertEquals(ShopNames.IKI, fullFileIkiScraper.getSHOP());
        assertEquals(ShopNames.IKI, ikiScraper.getSHOP());
    }

    @Test
    public void givenContext_whenIsValid_thenTrue() {
        assertTrue(fullFileIkiScraper.isValid());
        assertTrue(ikiScraper.isValid());
    }

    @Test
    public void givenInvalidAddress_whenIsValid_thenFalse() {
        MaximaScraper scraper = new MaximaScraper(new Document("Some random string which should fail"));
        assertFalse(scraper.isValid());
    }

    @Test
    public void givenValidFile_whenGetNumberOfRequiredRequests_thenReturnCorrectCount() {
        int expectedCount = 11;
        int actualCount = fullFileIkiScraper.countPages();

        assertEquals(expectedCount, actualCount);
    }

    @Test
    public void givenActualWebsite_whenGetNumberOfRequiredRequests_thenReturnCorrectCount() {
        int singlePageItemsCount = 18;
        int totalItemsCount = getTotalItemsFromIki();
        int expectedPagesCount = (totalItemsCount % singlePageItemsCount == 0) ? totalItemsCount / singlePageItemsCount : totalItemsCount / singlePageItemsCount + 1;
        int actualPagesCount = ikiScraper.countPages();

        assertNotEquals(0, expectedPagesCount);
        assertEquals(expectedPagesCount, actualPagesCount);
    }

    @Test
    public void givenActualSite_whenFetchItemsWithOffset_thenNotNull() {
        List<ItemOnOffer> fetchedElements = ikiScraper.fetchItemsWithOffset(0);

        assertNotNull(fetchedElements);
    }

    @Test
    public void givenValidFile_whenGetItemsOnOffer_thenReturnCount() {
        int expectedCount = 18;
        List<ItemOnOffer> actualIterable = fullFileIkiScraper.getItemsOnOffer();

        assertNotNull(actualIterable);
        assertEquals( expectedCount, actualIterable.size());
    }

    @Test
    public void givenActualWebsite_whenGetItemsOnOffer_thenReturnCount() {
        int expectedCount = getTotalItemsFromIki();
        int actualCount = ikiScraper.getItemsOnOffer().size();

        assertNotEquals(0, expectedCount);
        assertEquals(expectedCount, actualCount);
    }

    @Test
    public void givenValidFile_whenCreateItemFromElement_thenReturnItem() {
        int expectedItemIndex = 2;
        String expectedItemName = "Didieji raudonieji greipfrutai";
        String expectedItemBrand = "";
        float expectedItemPrice = 1.49f;
        ItemOnOffer expectedItem = new ItemOnOffer(expectedItemName, expectedItemBrand, expectedItemPrice, "ShopName");
        ItemOnOffer actualItem = fullFileIkiScraper.elementToScrapedShoppingItem(
                fullFileIkiScraper.getDocument().getElementsByClass("sales-item ").get(expectedItemIndex)
        );

        assertNotNull(actualItem);
        assertTrue(actualItem.getName().equalsIgnoreCase(expectedItemName));
        assertTrue(actualItem.getBrand().equalsIgnoreCase(expectedItemBrand));
        assertEquals(expectedItemPrice, actualItem.getPrice(), 0.0);
    }

//    @Test
//    public void givenValidWebsite_whenUpdateOffersDB_thenDBUpdated(){
//        boolean isPushSuccessful = ikiScraper.updateOffersDB();
//        List<ShoppingItemDealEntity> foundDeals = dealsRepo.findAll();
//
//        assertTrue(isPushSuccessful);
//        assertNotNull(foundDeals);
//        assertEquals(
//                getTotalItemsFromMaxima(),
//                foundDeals.size()
//        );
//    }

    // push to db with valid website
    //push to db with invalid website
}
