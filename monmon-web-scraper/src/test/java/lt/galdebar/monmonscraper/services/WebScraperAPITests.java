package lt.galdebar.monmonscraper.services;

import lt.galdebar.monmonscraper.services.scrapers.IsWebScraper;
import lt.galdebar.monmonscraper.services.testhelpers.GetItemsCountFromWebsites;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.Test;

import java.io.IOException;

import static lt.galdebar.monmonscraper.services.testhelpers.GetItemsCountFromWebsites.*;
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
        assertTrue( scraperAPI.getAvailableScrapers().get(0) instanceof IsWebScraper);
    }

    //get all items (fresh parse from websites)
    //get items from specific shop.
    //push items to db ? probably move them to each scraper.
    //search by keyword


    private int getTotalItemsCount(){
        int maximaCount = getTotalItemsFromMaxima();
        return maximaCount;
    }
}
