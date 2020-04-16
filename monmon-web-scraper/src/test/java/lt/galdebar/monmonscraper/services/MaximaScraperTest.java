package lt.galdebar.monmonscraper.services;

import lt.galdebar.monmonscraper.domain.ScrapedShoppingItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
    void givenContext_thenLoadContext() {
        assertNotNull(maximaScraper);
    }

    @Test
    void givenContext_whenIsValid_thenTrue() {
        assertTrue(maximaScraper.isValid());
    }

    @Test
    void givenInvalidAddress_whenIsValid_thenFalse() {
        MaximaScraper scraper = new MaximaScraper(new Document("Some random string which should fail"));
        assertFalse(scraper.isValid());
    }

    @Test
    void givenValidContext_whenGetDocumentTitle_thenNotNull() {
        assertNotNull(maximaScraper.getTitle());
    }


    @Test
    void givenValidContext_whenGetContainer_thenNotNull() {
        Element container = maximaScraper.getContainer();
        assertNotNull(container);
    }

    @Test
    void givenValidFile_whenGetNumberOfRequiredRequests_thenReturnCorrectCount() {
        int singlePageItemsCount = 45;
        int totalItems = 328;
        int expectedCount = 8;
        int actualCount = maximaScraper.countPages();

        assertEquals(expectedCount, actualCount);
    }

    @Test
    void givenActualWebsite_whenGetNumberOfRequiredRequests_thenReturnCorrectCount() {
        MaximaScraper scraper = new MaximaScraper(); // default constructor has hardwired url.
        int singlePageItemsCount = 45;
        int totalItemsCount = getTotalItemsFromActualSite();
        int expectedPagesCount = (totalItemsCount % singlePageItemsCount == 0) ? totalItemsCount / singlePageItemsCount : totalItemsCount / singlePageItemsCount + 1;
        int actualPagesCount = scraper.countPages();

        assertNotEquals(0, expectedPagesCount);
        assertEquals(expectedPagesCount, actualPagesCount);
    }

    @Test
    void givenValidFile_whenFetchItemsWithOffset_thenNotNull() {
        MaximaScraper scraper = new MaximaScraper();
        Elements fetchedElements = scraper.fetchItemsWithOffset(0);

        assertNotNull(fetchedElements);
    }

    @Test
    void givenValidFile_whenGetItemsOnOffer_thenReturnCount() {
        int expectedCount = 328;
        Collection<Element> actualIterable = maximaScraper.getItemsOnOffer();

        assertNotNull(actualIterable);
        assertEquals(actualIterable.size(), expectedCount);
    }

    @Test
    void givenActualWebsite_whenGetItemsOnOffer_thenReturnCount() {
        MaximaScraper scraper = new MaximaScraper();// default constructor has hardwired url.
        int expectedCount = getTotalItemsFromActualSite();
        int actualCount = scraper.getItemsOnOffer().size();

        assertNotEquals(0, expectedCount);
        assertEquals(expectedCount, actualCount);
    }

    @Test
    void givenValidFile_whenCreateItemFromElement_thenReturnItem() {
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

    // account for timeouts and bad pages- just return empty arrays.

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
