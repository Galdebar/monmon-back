package lt.galdebar.monmonscraper.services.helpers;


import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealDTO;

import lt.galdebar.monmonscraper.services.testhelpers.TestContainersConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@TestPropertySource(locations = {"classpath:test.properties"})
@ContextConfiguration(initializers = {TestContainersConfig.Initializer.class})
public class AssignKeywordHelperTests {

    @Autowired
    private AssignKeywordHelper assignKeywordHelper;


    @Test
    public void givenContext_thenLoadCategorizer() {
        assertNotNull(assignKeywordHelper);
    }

    @Test
    public void givenExistingSimpleKeyword_whenAssignKeyword_thenReturnExistingKeyword() {
        ItemOnOffer scrapedItem1 = new ItemOnOffer(
                "water",
                "Alita",
                1,
                "shopName"
        );
        String expectedKeyword1 = "Water";

        ShoppingItemDealDTO deal1 = assignKeywordHelper.assignKeyword(scrapedItem1);

        assertNotNull(deal1);
        assertEquals(expectedKeyword1, deal1.getItemKeyword());
        assertTrue(scrapedItem1.getPrice() == deal1.getPrice());
        assertEquals(scrapedItem1.getShopName(), deal1.getShopTitle());
    }

    @Test
    public void givenPartialKeyword_whenAssignKeyword_thenReturnExistingKeyword() {
        ItemOnOffer scrapedItem1 = new ItemOnOffer(
                "butter",
                "Whatever",
                1,
                "shopName"
        );
        String expectedKeyword1 = "Butter & Margarine";

        ShoppingItemDealDTO deal1 = assignKeywordHelper.assignKeyword(scrapedItem1);

        assertNotNull(deal1);
        assertEquals(expectedKeyword1, deal1.getItemKeyword());
        assertTrue(scrapedItem1.getPrice() == deal1.getPrice());
        assertEquals(scrapedItem1.getShopName(), deal1.getShopTitle());
    }

    @Test
    public void givenInvalidKeyword_whenAssignKeyword_thenReturnSameName() {
        ItemOnOffer scrapedItem1 = new ItemOnOffer(
                "ipouawd",
                "Whatever",
                1,
                "shopName"
        );
        String expectedKeyword1 = "ipouawd";

        ShoppingItemDealDTO deal1 = assignKeywordHelper.assignKeyword(scrapedItem1);

        assertNotNull(deal1);
        assertEquals(expectedKeyword1, deal1.getItemKeyword());
        assertTrue(scrapedItem1.getPrice() == deal1.getPrice());
        assertEquals(scrapedItem1.getShopName(), deal1.getShopTitle());
    }

    @Test
    public void givenBlankKeyword_whenAssignKeyword_thenReturnBlankDeal() {
        ItemOnOffer scrapedItem1 = new ItemOnOffer(
                "",
                "Whatever",
                1,
                "shopName"
        );

        ShoppingItemDealDTO deal1 = assignKeywordHelper.assignKeyword(scrapedItem1);

        assertNotNull(deal1);
        assertEquals("", deal1.getItemKeyword());
        assertEquals(0.0, deal1.getPrice(), 0.0);
        assertEquals("", deal1.getShopTitle());
    }
    @Test
    public void givenEmptyKeyword_whenAssignKeyword_thenReturnBlankDeal() {
        ItemOnOffer scrapedItem1 = new ItemOnOffer(
                "   ",
                "Whatever",
                1,
                "shopName"
        );

        ShoppingItemDealDTO deal1 = assignKeywordHelper.assignKeyword(scrapedItem1);

        assertNotNull(deal1);
        assertEquals("", deal1.getItemKeyword());
        assertEquals(0.0f, deal1.getPrice(), 0.0);
        assertEquals("", deal1.getShopTitle());
    }

    @Test
    public void givenSeveralKeywords_whenAssignKeywords_thenReturnValidList(){
        ItemOnOffer scrapedItem1 = new ItemOnOffer(
                "ipouawd",
                "Whatever",
                1,
                "shopName"
        );
        ItemOnOffer scrapedItem2 = new ItemOnOffer(
                "butter",
                "Whatever",
                1,
                "shopName"
        );
        String expectedKeyword1 = "ipouawd";
        String expectedKeyword2 = "Butter & Margarine";

        List<ItemOnOffer> itemOnOffers = new ArrayList<>();
        itemOnOffers.add(scrapedItem1);
        itemOnOffers.add(scrapedItem2);

        List<ShoppingItemDealDTO> deals = assignKeywordHelper.assignKeywords(itemOnOffers);

        assertNotNull(deals);
        assertEquals(2,deals.size());
        assertEquals(expectedKeyword1, deals.get(0).getItemKeyword());
        assertEquals(expectedKeyword2, deals.get(1).getItemKeyword());
    }

    @Test
    public void givenListWithBlankOrEmptyScrapedItems_whenAssignKeywords_thenReturnOnlyValid(){
        ItemOnOffer scrapedItem1 = new ItemOnOffer(
                "  ",
                "Whatever",
                1,
                "shopName"
        );
        ItemOnOffer scrapedItem2 = new ItemOnOffer(
                "  ",
                "Whatever",
                1,
                "shopName"
        );
        ItemOnOffer scrapedItem3 = new ItemOnOffer(
                "butter",
                "Whatever",
                1,
                "shopName"
        );
        ItemOnOffer scrapedItem4 = new ItemOnOffer(
                "iouhawd",
                "Whatever",
                1,
                "shopName"
        );
        String expectedKeyword2 = "Butter & Margarine";

        List<ItemOnOffer> itemOnOffers = new ArrayList<>();
        itemOnOffers.add(scrapedItem1);
        itemOnOffers.add(scrapedItem2);
        itemOnOffers.add(scrapedItem3);
        itemOnOffers.add(scrapedItem4);

        List<ShoppingItemDealDTO> deals = assignKeywordHelper.assignKeywords(itemOnOffers);

        assertNotNull(deals);
        assertEquals(2,deals.size());
        assertEquals(expectedKeyword2, deals.get(0).getItemKeyword());
        assertEquals(scrapedItem4.getName(), deals.get(1).getItemKeyword());
    }
}
