package lt.galdebar.monmonapi.webscraper.services;

import lt.galdebar.monmonapi.webscraper.services.scrapers.IsWebScraper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.Test;

import static lt.galdebar.monmonapi.webscraper.services.testhelpers.GetItemsCountFromWebsites.*;
import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class WebScraperAPITests {

    @Autowired
    private WebScraperAPI scraperAPI;

    @Test
    public void givenContext_ThenLoadScraper(){
        assertNotNull(scraperAPI);
    }

    @Test
    public void givenContext_whenGetAvailableScrapers_thenReturnScraperList(){
        assertNotNull(scraperAPI.getAvailableScrapers());
        assertTrue(scraperAPI.getAvailableScrapers().size()>0);
        assertNotNull(scraperAPI.getAvailableScrapers().get(0));
    }

    @Test
    public void givenContext_whenRunScrapers_thenDBHasEntries(){

    }

    //get all items (fresh parse from websites)
    //get items from specific shop.
    //push items to db ? probably move them to each scraper.
    //search by keyword


    private int getTotalItemsCount(){
        return getTotalItemsFromMaxima();
    }
}
