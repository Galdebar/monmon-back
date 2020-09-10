package lt.galdebar.monmonapi.webscraper.services;

import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.webscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonapi.webscraper.scheduledtasks.RunScraper;
import lt.galdebar.monmonapi.webscraper.services.helpers.translators.HackyGoogleItemTranslator;
import lt.galdebar.monmonapi.webscraper.services.scrapers.MaximaScraper;
import lt.galdebar.monmonapi.webscraper.services.scrapers.ShopNames;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static lt.galdebar.monmonapi.webscraper.services.scrapers.ShopNames.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(initializers = {ListTestContainersConfig.Initializer.class})
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = RunScraper.class)})
public class ShoppingItemDealFinderServiceTests {

    @Autowired
    private ShoppingItemDealFinderService shoppingItemDealFinderService;

    @Autowired
    private ShoppingItemDealsRepo dealsRepo;

    private MaximaScraper maximaScraper = new MaximaScraper();
    private HackyGoogleItemTranslator translator = new HackyGoogleItemTranslator();

    @Before
    public void setUp() {
        addItemsToDB();
    }


    @After
    public void tearDown() {
        dealsRepo.deleteAll();
    }

    @Test
    public void givenContext_thenServiceNotNull() {
        assertNotNull(shoppingItemDealFinderService);
    }

    @Test
    public void whenGetAllDeals_thenReturnList() {
        int expectedSize = dealsRepo.findAll().size();
        List<ShoppingItemDealDTO> foundDeals = shoppingItemDealFinderService.getAllDeals();
        assertNotNull(foundDeals);
        assertEquals(expectedSize, foundDeals.size());
    }

    @Test
    public void whenGetDealsByShopName_thenReturnList() {
        for (ShopNames shop : values()) {
            int expectedNumOfDeals = (int) dealsRepo.findAll().stream()
                    .filter(deal -> deal.getShopTitle().equalsIgnoreCase(shop.getShopName())).count();
            List<ShoppingItemDealDTO> foundDeals = shoppingItemDealFinderService.getDealsByShop(shop);
            assertNotNull(foundDeals);
            assertEquals(expectedNumOfDeals, foundDeals.size());
        }
    }

    @Test
    public void givenValidKeyword_whenGetDealsByKeyword_thenReturnList() {
        String keyword = "Eggs";
        List<ShoppingItemDealDTO> foundDeals = shoppingItemDealFinderService.getDealsByKeyword(keyword);
        int expectedSize = (int) dealsRepo.findAll().stream()
                .filter(deal -> deal.getTitle().toLowerCase().contains(keyword.toLowerCase())).count();

        assertNotNull(foundDeals);
        assertEquals(expectedSize,foundDeals.size());
    }

    @Test
    public void givenInvalidKeyword_whenGetDealsByKeyword_thenReturnEmptyList() {
        String keyword = "liawhjd";
        List<ShoppingItemDealDTO> foundDeals = shoppingItemDealFinderService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0, foundDeals.size());
    }

    @Test
    public void givenEmptyKeyword_whenGetDealsByKeyword_thenReturnEmptyList() {
        String keyword = "";
        List<ShoppingItemDealDTO> foundDeals = shoppingItemDealFinderService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0, foundDeals.size());
    }

    @Test
    public void givenBlankKeyword_whenGetDealsByKeyword_thenReturnEmptyList() {
        String keyword = "   ";
        List<ShoppingItemDealDTO> foundDeals = shoppingItemDealFinderService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0, foundDeals.size());
    }

    @Test
    public void givenNullKeyword_whenGetDealsByKeyword_thenReturnEmptyList() {
        String keyword = null;
        List<ShoppingItemDealDTO> foundDeals = shoppingItemDealFinderService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0, foundDeals.size());
    }

    @Test
    public void givenValidKeyword_whenGetBestDeal_thenReturnSingleItem() {
        String keyword = "Eggs";
        ShoppingItemDealEntity expectedDeal = dealsRepo.findAll().stream()
                .filter(deal->deal.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .sorted((o1, o2) -> Float.compare(o1.getPrice(),o2.getPrice()))
                .collect(Collectors.toList())
                .get(0);
        ShoppingItemDealDTO actualDeal = shoppingItemDealFinderService.getBestDeal(keyword);
        assertEquals(expectedDeal.getPrice(),actualDeal.getPrice(), 0.001);
        assertEquals(expectedDeal.getShopTitle(),actualDeal.getShopTitle());
        assertEquals(expectedDeal.getBrand(),actualDeal.getBrand());
    }

    @Test
    public void givenInvalidKeyword_whenGetBestDeal_thenReturnEmptyItem() {
        String keyword = "liawhjd";
        ShoppingItemDealDTO actualDeal = shoppingItemDealFinderService.getBestDeal(keyword);

        assertNotNull(actualDeal);
        assertEquals("",actualDeal.getTitle());
        assertEquals("",actualDeal.getBrand());
        assertEquals("",actualDeal.getShopTitle());
        assertEquals(0,actualDeal.getPrice(),0.001);

    }

    @Test
    public void givenEmptyKeyword_whenGetBestDeal_thenReturnEmptyItem() {
        String keyword = "";
        ShoppingItemDealDTO actualDeal = shoppingItemDealFinderService.getBestDeal(keyword);

        assertNotNull(actualDeal);
        assertEquals("",actualDeal.getTitle());
        assertEquals("",actualDeal.getBrand());
        assertEquals("",actualDeal.getShopTitle());
        assertEquals(0,actualDeal.getPrice(),0.001);
    }

    @Test
    public void givenBlankKeyword_whenGetBestDeal_thenReturnEmptyItem() {
        String keyword = "   ";
        ShoppingItemDealDTO actualDeal = shoppingItemDealFinderService.getBestDeal(keyword);

        assertNotNull(actualDeal);
        assertEquals("",actualDeal.getTitle());
        assertEquals("",actualDeal.getBrand());
        assertEquals("",actualDeal.getShopTitle());
        assertEquals(0,actualDeal.getPrice(),0.001);
    }

    @Test
    public void givenNullKeyword_whenGetBestDeal_thenReturnEmptyItem() {
        String keyword = null;
        ShoppingItemDealDTO actualDeal = shoppingItemDealFinderService.getBestDeal(keyword);

        assertNotNull(actualDeal);
        assertEquals("",actualDeal.getTitle());
        assertEquals("",actualDeal.getBrand());
        assertEquals("",actualDeal.getShopTitle());
        assertEquals(0,actualDeal.getPrice(),0.001);
    }

    @Test
    public void givenLithuanianKeyword_whenGetBestDeal_thenReturnItemMatchingUntranslatedTitle(){
        String lithuanianKeyword = "kiausiniai";

        ShoppingItemDealEntity expectedDeal = new ShoppingItemDealEntity();
        expectedDeal.setUntranslatedTitle("kaimiški kiaušiniai");
        expectedDeal.setTitle("country eggs");
        expectedDeal.setBrand("RIDO");
        expectedDeal.setShopTitle(RIMI.getShopName());
        expectedDeal.setPrice(0.25f);

        ShoppingItemDealEntity alternativeDeal1 = new ShoppingItemDealEntity();
        alternativeDeal1.setUntranslatedTitle("kiaušiniai");
        alternativeDeal1.setTitle("eggs");
        alternativeDeal1.setBrand("RIDO");
        alternativeDeal1.setShopTitle(MAXIMA.getShopName());
        alternativeDeal1.setPrice(0.69f);

        ShoppingItemDealEntity alternativeDeal2 = new ShoppingItemDealEntity();
        alternativeDeal2.setUntranslatedTitle("kazkas kito");
        alternativeDeal2.setTitle("country eggs");
        alternativeDeal2.setBrand("RIDO");
        alternativeDeal2.setShopTitle(IKI.getShopName());
        alternativeDeal2.setPrice(0.20f);

        dealsRepo.save(expectedDeal);
        dealsRepo.save(alternativeDeal1);
        dealsRepo.save(alternativeDeal2);

        ShoppingItemDealDTO actualDeal = shoppingItemDealFinderService.getBestDeal(lithuanianKeyword);

        assertEquals(expectedDeal.getUntranslatedTitle(), actualDeal.getUntranslatedTitle());
        assertEquals(expectedDeal.getUntranslatedTitle(), actualDeal.getTitle());
        assertEquals(expectedDeal.getBrand(), actualDeal.getBrand());
        assertEquals(expectedDeal.getShopTitle(), actualDeal.getShopTitle());
        assertEquals(expectedDeal.getPrice(), actualDeal.getPrice(),0.001f);

    }
    //prioritize untranslated deal


    private void addItemsToDB() {
        List<ShoppingItemDealEntity> deals = new ArrayList<>();
        deals.add(
                createDeal("Eggs", "", MAXIMA, 1f)
        );
        deals.add(
                createDeal("Eggs", "", IKI, 0.2f)
        );
        deals.add(
                createDeal("Milk", "Rokiskio", MAXIMA, 1.5f)
        );
        dealsRepo.saveAll(deals);
    }

    private ShoppingItemDealEntity createDeal(String keyword, String brand, ShopNames shopName, float price) {
        ShoppingItemDealEntity deal = new ShoppingItemDealEntity();
        deal.setTitle(keyword);
        deal.setBrand(brand);
        deal.setShopTitle(shopName.getShopName());
        deal.setPrice(price);

        return deal;
    }


}
