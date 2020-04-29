package lt.galdebar.monmonscraper.services;

import lt.galdebar.monmonscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonscraper.services.helpers.ItemTranslator;
import lt.galdebar.monmonscraper.services.scrapers.IsWebScraper;
import lt.galdebar.monmonscraper.services.scrapers.MaximaScraper;
import lt.galdebar.monmonscraper.services.scrapers.ShopNames;
import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
public class DealsServiceTests {

    @Autowired
    private DealsService dealsService;

    @Autowired
    private ShoppingItemDealsRepo dealsRepo;

    private MaximaScraper maximaScraper = new MaximaScraper();
    private ItemTranslator translator = new ItemTranslator();

    @Before
    public void setUp() throws Exception {
        addItemsToDB();
    }


    @After
    public void tearDown() {
        dealsRepo.deleteAll();
    }

    @Test
    public void givenContext_thenServiceNotNull() {
        assertNotNull(dealsService);
    }

    @Test
    public void whenGetAllDeals_thenReturnList() {
        int expectedSize = dealsRepo.findAll().size();
        List<ShoppingItemDealDTO> foundDeals = dealsService.getAllDeals();
        assertNotNull(foundDeals);
        assertEquals(expectedSize, foundDeals.size());
    }

    @Test
    public void whenGetDealsByShopName_thenReturnList() {
        for (ShopNames shop : ShopNames.values()) {
            int expectedNumOfDeals = dealsRepo.findAll().stream()
                    .filter(deal->deal.getShopTitle().equalsIgnoreCase(shop.getShopName()))
                    .collect(Collectors.toList())
                    .size();
            List<ShoppingItemDealDTO> foundDeals = dealsService.getDealByShop(shop);
            assertNotNull(foundDeals);
            assertEquals(expectedNumOfDeals, foundDeals.size());
        }
    }

    @Test
    public void givenValidKeyword_whenGetDealsByKeyword_thenReturnList() {
        String keyword = "Eggs";
        List<ShoppingItemDealDTO> foundDeals = dealsService.getDealsByKeyword(keyword);
        int expectedSize = dealsRepo.findAll().stream()
                .filter(deal->deal.getItemKeyword().equalsIgnoreCase(keyword))
                .collect(Collectors.toList())
                .size();

        assertNotNull(foundDeals);
        assertEquals(expectedSize,foundDeals.size());
    }

    @Test
    public void givenInvalidKeyword_whenGetDealsByKeyword_thenReturnEmptyList() {
        String keyword = "liawhjd";
        List<ShoppingItemDealDTO> foundDeals = dealsService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0, foundDeals.size());
    }

    @Test
    public void givenEmptyKeyword_whenGetDealsByKeyword_thenReturnEmptyList() {
        String keyword = "";
        List<ShoppingItemDealDTO> foundDeals = dealsService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0, foundDeals.size());
    }

    @Test
    public void givenBlankKeyword_whenGetDealsByKeyword_thenReturnEmptyList() {
        String keyword = "   ";
        List<ShoppingItemDealDTO> foundDeals = dealsService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0, foundDeals.size());
    }

    @Test
    public void givenNullKeyword_whenGetDealsByKeyword_thenReturnEmptyList() {
        String keyword = null;
        List<ShoppingItemDealDTO> foundDeals = dealsService.getDealsByKeyword(keyword);

        assertNotNull(foundDeals);
        assertEquals(0, foundDeals.size());
    }

    @Test
    public void givenValidKeyword_whenGetBestDeal_thenReturnSingleItem() {
        String keyword = "Eggs";
        ShoppingItemDealEntity expectedDeal = (ShoppingItemDealEntity) dealsRepo.findAll().stream()
                .filter(deal->deal.getItemKeyword().equalsIgnoreCase(keyword))
                .sorted((o1, o2) -> Float.compare(o1.getPrice(),o2.getPrice()))
                .collect(Collectors.toList())
                .get(0);
        ShoppingItemDealDTO actualDeal = dealsService.getBestDeal(keyword);
        assertEquals(expectedDeal.getPrice(),actualDeal.getPrice(), 0.001);
        assertEquals(expectedDeal.getShopTitle(),actualDeal.getShopTitle());
        assertEquals(expectedDeal.getItemBrand(),actualDeal.getItemBrand());
    }

    @Test
    public void givenInvalidKeyword_whenGetBestDeal_thenReturnEmptyItem() {
        String keyword = "liawhjd";
        ShoppingItemDealDTO actualDeal = dealsService.getBestDeal(keyword);

        assertNotNull(actualDeal);
        assertEquals("",actualDeal.getItemKeyword());
        assertEquals("",actualDeal.getItemBrand());
        assertEquals("",actualDeal.getShopTitle());
        assertEquals(0,actualDeal.getPrice(),0.001);

    }

    @Test
    public void givenEmptyKeyword_whenGetBestDeal_thenReturnEmptyItem() {
        String keyword = "";
        ShoppingItemDealDTO actualDeal = dealsService.getBestDeal(keyword);

        assertNotNull(actualDeal);
        assertEquals("",actualDeal.getItemKeyword());
        assertEquals("",actualDeal.getItemBrand());
        assertEquals("",actualDeal.getShopTitle());
        assertEquals(0,actualDeal.getPrice(),0.001);
    }

    @Test
    public void givenBlankKeyword_whenGetBestDeal_thenReturnEmptyItem() {
        String keyword = "   ";
        ShoppingItemDealDTO actualDeal = dealsService.getBestDeal(keyword);

        assertNotNull(actualDeal);
        assertEquals("",actualDeal.getItemKeyword());
        assertEquals("",actualDeal.getItemBrand());
        assertEquals("",actualDeal.getShopTitle());
        assertEquals(0,actualDeal.getPrice(),0.001);
    }

    @Test
    public void givenNullKeyword_whenGetBestDeal_thenReturnEmptyItem() {
        String keyword = null;
        ShoppingItemDealDTO actualDeal = dealsService.getBestDeal(keyword);

        assertNotNull(actualDeal);
        assertEquals("",actualDeal.getItemKeyword());
        assertEquals("",actualDeal.getItemBrand());
        assertEquals("",actualDeal.getShopTitle());
        assertEquals(0,actualDeal.getPrice(),0.001);
    }


    private void addItemsToDB() {
        List<ShoppingItemDealEntity> deals = new ArrayList<>();
        deals.add(
                createDeal("Eggs", "", ShopNames.MAXIMA, 1f)
        );
        deals.add(
                createDeal("Eggs", "", ShopNames.IKI, 0.2f)
        );
        deals.add(
                createDeal("Milk", "Rokiskio", ShopNames.MAXIMA, 1.5f)
        );
        dealsRepo.saveAll(deals);
    }

    private ShoppingItemDealEntity createDeal(String keyword, String brand, ShopNames shopName, float price) {
        ShoppingItemDealEntity deal = new ShoppingItemDealEntity();
        deal.setItemKeyword(keyword);
        deal.setItemBrand(brand);
        deal.setShopTitle(shopName.getShopName());
        deal.setPrice(price);

        return deal;
    }


}
