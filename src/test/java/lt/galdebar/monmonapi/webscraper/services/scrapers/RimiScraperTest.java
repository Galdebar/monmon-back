package lt.galdebar.monmonapi.webscraper.services.scrapers;

import lt.galdebar.monmonapi.webscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;
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

import static lt.galdebar.monmonapi.webscraper.services.testhelpers.GetItemsCountFromWebsites.getTotalItemsFromMaxima;
import static lt.galdebar.monmonapi.webscraper.services.testhelpers.GetItemsCountFromWebsites.getTotalItemsFromRimi;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class RimiScraperTest {

    @org.springframework.boot.test.context.TestConfiguration
    public static class TestConfiguration {
        @Bean
        public RimiScraper fullFileRimiScraper() {
            File localfile = new File("src/test/resources/webscraper/WebsiteSnapshots/Rimi_Full.html");
            Document doc = null;
            try {
                doc = Jsoup.parse(localfile, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert doc != null;
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
    public void setUp() {
        dealsRepo.deleteAll();
    }

    @AfterEach
    public void tearDown() {
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
        List<ShoppingItemDealDTO> actualIterable = fullFileRimiScraper.getItemsOnOffer();

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

        ShoppingItemDealDTO expectedItem = new ShoppingItemDealDTO(expectedItemName, expectedItemBrand, "ShopName", expectedItemPrice);
        ShoppingItemDealDTO actualItem = fullFileRimiScraper.elementToScrapedShoppingItem(elementToParse);

        assertNotNull(actualItem);
        assertTrue(actualItem.getTitle().equalsIgnoreCase(expectedItemName));
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
