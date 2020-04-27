package lt.galdebar.monmonscraper.services;

import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonscraper.services.helpers.ItemTranslator;
import lt.galdebar.monmonscraper.services.scrapers.IsWebScraper;
import lt.galdebar.monmonscraper.services.scrapers.MaximaScraper;
import lt.galdebar.monmonscraper.services.scrapers.ShopNames;
import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class DealsServiceTests {

    @Autowired
    private DealsService dealsService;

    @Autowired
    private WebScraperAPI scraperAPI;

    private MaximaScraper maximaScraper = new MaximaScraper();
    private ItemTranslator translator = new ItemTranslator();

    @Before
    public void setUp() throws Exception {
        scraperAPI.runScrapers();
    }

    @Test
    public void givenContext_thenServiceNotNull() {
        assertNotNull(dealsService);
    }

    @Test
    public void whenGetAllDeals_thenReturnList() {
        List<ShoppingItemDealDTO> foundDeals = dealsService.getAllDeals();
        assertNotNull(foundDeals);
        assertTrue(foundDeals.size() > 0);
        int expectedNumberOfDeals = getNumberOfTotalDeals();
    }

    @Test
    public void whenGetDealsByShopName_thenReturnList() {
        for (ShopNames shop : ShopNames.values()) {
            int expectedNumOfDeals = getNumOfDealsByShop(shop);
            List<ShoppingItemDealDTO> foundDeals = dealsService.getDealByShop(shop);
            assertNotNull(foundDeals);
            assertEquals(expectedNumOfDeals, foundDeals.size());
        }
    }

    @Test
    public void givenValidKeyword_whenGetDealsByKeyword_thenReturnList() {
        String keyword = getFirstKeywordFromMaxima();
        List<ShoppingItemDealDTO> foundDeals = dealsService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertTrue(foundDeals.size() > 0);
    }

    @Test
    public void givenInvalidKeyword_whenGetDealsByKeyword_thenReturnEmptyList() {
        String keyword = "liawhjd";
        List<ShoppingItemDealDTO> foundDeals = dealsService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0,foundDeals.size());
    }

    @Test
    public void givenEmptyKeyword_whenGetDealsByKeyword_thenReturnEmptyList(){
        String keyword = "";
        List<ShoppingItemDealDTO> foundDeals = dealsService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0,foundDeals.size());
    }

    @Test
    public void givenBlankKeyword_whenGetDealsByKeyword_thenReturnEmptyList(){
        String keyword = "   ";
        List<ShoppingItemDealDTO> foundDeals = dealsService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0,foundDeals.size());
    }

    @Test
    public void givenNullKeyword_whenGetDealsByKeyword_thenReturnEmptyList(){
        String keyword = null;
        List<ShoppingItemDealDTO> foundDeals = dealsService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0,foundDeals.size());
    }

    private int getNumberOfTotalDeals() {
        int total = 0;
        for (IsWebScraper scraper : scraperAPI.getAvailableScrapers()) {
            total += scraper.getItemsOnOffer().size();
        }

        return total;
    }

    private int getNumOfDealsByShop(ShopNames shopName) {
        for (IsWebScraper scraper : scraperAPI.getAvailableScrapers()) {
            if (scraper.getSHOP().equals(shopName)) {
                return scraper.getItemsOnOffer().size();
            }
        }
        return 0;
    }

    private String getFirstKeywordFromMaxima() {
        ItemOnOffer firstItemOnOffer = maximaScraper.getItemsOnOffer().get(0);
        String keyword = translator.translate(firstItemOnOffer).getName();
        return keyword;
    }


}
