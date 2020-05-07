package lt.galdebar.monmonscraper.services.scrapers;

import lt.galdebar.monmonscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

import static lt.galdebar.monmonscraper.services.testhelpers.GetItemsCountFromWebsites.getTotalItemsFromMaxima;
import static lt.galdebar.monmonscraper.services.testhelpers.GetItemsCountFromWebsites.getTotalItemsFromRimi;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class RimiScraperTest {

    @org.springframework.boot.test.context.TestConfiguration
    public static class TestConfiguration {
        @Bean
        public RimiScraper fullFileRimiScraper() {
            File localfile = new File("src/test/resources/WebsiteSnapshots/Rimi_Full.html");
            Document doc = null;
            try {
                doc = Jsoup.parse(localfile, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new RimiScraper(doc);
        }

    }

    @Autowired
    private RimiScraper fullFileRimiScraper;

    @Autowired
    private RimiScraper rimiScraper;

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
        assertNotNull(fullFileRimiScraper);
        assertNotNull(rimiScraper);
        assertEquals(ShopNames.MAXIMA, fullFileRimiScraper.getSHOP());
        assertEquals(ShopNames.MAXIMA, rimiScraper.getSHOP());
    }

    @Test
    public void givenContext_whenIsValid_thenTrue() {
        assertTrue(fullFileRimiScraper.isValid());
        assertTrue(rimiScraper.isValid());
    }

    @Test
    public void givenInvalidAddress_whenIsValid_thenFalse() {
        MaximaScraper scraper = new MaximaScraper(new Document("Some random string which should fail"));
        assertFalse(scraper.isValid());
    }

    @Test
    public void givenValidFile_whenGetItemsOnOffer_thenReturnCount() {
        int expectedCount = 152;
        List<ItemOnOffer> actualIterable = fullFileRimiScraper.getItemsOnOffer();

        assertNotNull(actualIterable);
        assertEquals(expectedCount, actualIterable.size());
    }

    @Test
    public void givenActualWebsite_whenGetItemsOnOffer_thenReturnCount() {
        int expectedCount = getTotalItemsFromRimi();
        int actualCount = rimiScraper.getItemsOnOffer().size();

        assertNotEquals(0, expectedCount);
        assertEquals(expectedCount, actualCount);
    }

    @Test
    public void givenValidFile_whenCreateItemFromElement_thenReturnItem() {
        int expectedItemIndex = 3;
        String expectedItemName = "Viščiukų krūtinėlės filė";
        String expectedItemBrand = "RIMI";
        float expectedItemPrice = 3.59f;
        Element elementToParse = fullFileRimiScraper.getDocument()
                .getElementsByClass("container")
                .get(3)
                .getElementsByClass("offer-card")
                .get(expectedItemIndex);

        ItemOnOffer expectedItem = new ItemOnOffer(expectedItemName, expectedItemBrand, expectedItemPrice, "ShopName");
        ItemOnOffer actualItem = fullFileRimiScraper.elementToScrapedShoppingItem(elementToParse);

        assertNotNull(actualItem);
        assertTrue(actualItem.getName().equalsIgnoreCase(expectedItemName));
        assertTrue(actualItem.getBrand().equalsIgnoreCase(expectedItemBrand));
        assertEquals(expectedItemPrice, actualItem.getPrice(), 0.0);
    }

    @Test
    public void givenValidWebsite_whenUpdateOffersDB_thenDBUpdated(){
        boolean isPushSuccessful = rimiScraper.updateOffersDB();
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
