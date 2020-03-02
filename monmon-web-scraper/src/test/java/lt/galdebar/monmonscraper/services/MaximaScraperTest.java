package lt.galdebar.monmonscraper.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.*;

@SpringJUnitConfig
public class MaximaScraperTest {

    @Configuration
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
        public MaximaHTMLElementParserHelper maximaHTMLElementParserHelper() {
            return new MaximaHTMLElementParserHelper();
        }
    }

    @Autowired
    private MaximaScraper maximaScraper;

    @Test
    void loadContext() {
        assertNotNull(maximaScraper);
    }

    @Test
    void testIsValid() {
        assertTrue(maximaScraper.isValid());
    }

    @Test
    void testIsValidFail() {
        MaximaScraper scraper = new MaximaScraper(new Document("Some random string which should fail"));
        assertFalse(scraper.isValid());
    }

    @Test
    void getDocumentTitleTest() {
        assertNotNull(maximaScraper.getTitle());
    }

    @Test
    void getContainerTest() {
        Element container = maximaScraper.getContainer();
        assertNotNull(container);
    }

    @Test
    void getItemsOnOfferTest() {
        int expectedCount = 328;
        Collection<Element> actualIterable = maximaScraper.getItemsOnOffer();

        assertNotNull(actualIterable);
        assertEquals(actualIterable.size(), expectedCount);
    }

    @Test
    void getItemsOnOfferPaginationTest() {
        MaximaScraper scraper = new MaximaScraper();
        int expectedItemCount = getTotalItemsFromActualSite();
        int actualItemCount = scraper.getItemsOnOffer().size();

        assertEquals(expectedItemCount, actualItemCount);

    }

    @Test
    void createItemFromElementTest1() {
        int expectedItemIndex = 2;
        String expectedItemName = "sviestas";
        String expectedItemBrand = "ROKIŠKIO";
        float expectedItemPrice = 1.09f;
        ScrapedShoppingItem expectedItem = new ScrapedShoppingItem(expectedItemName, expectedItemBrand, expectedItemPrice);
        ScrapedShoppingItem actualItem = maximaScraper.createItem(
                maximaScraper.getItemsOnOffer().get(expectedItemIndex)
        );

        assertNotNull(actualItem);
        assertTrue(actualItem.getName().equalsIgnoreCase(expectedItemName));
        assertTrue(actualItem.getBrand().equalsIgnoreCase(expectedItemBrand));
        assertEquals(expectedItemPrice, actualItem.getPrice(), 0.0);
    }

    private int getTotalItemsFromActualSite() {
        Document doc = null;
        try {
            doc = Jsoup.connect("https://www.maxima.lt/akcijos#visi-pasiulymai-1").userAgent("Mozilla").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element totalItemsElement = doc.getElementById("items_cnt");
        return Integer.parseInt(totalItemsElement.text());
    }
}
