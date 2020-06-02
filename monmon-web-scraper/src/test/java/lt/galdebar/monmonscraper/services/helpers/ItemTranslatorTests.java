package lt.galdebar.monmonscraper.services.helpers;

import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
public class ItemTranslatorTests {
    @Autowired
    private ItemTranslator itemTranslator;

    @org.springframework.boot.test.context.TestConfiguration
    public static class TestConfiguration {

        @Bean
        public ItemTranslator itemTranslator(){
            return new ItemTranslator();
        }
    }

    @AfterEach
    public void afterEach() throws InterruptedException {
//        Thread.sleep(10000); // testing a grace period to not overload the api
        await().atMost(10,TimeUnit.SECONDS);
    }


    @Test
    public void givenContext_thenLoadTranslator(){
        assertNotNull(itemTranslator);
    }

    @Test
    public void givenValidItem_whenTranslate_thenReturnTranslatedItem(){
        ItemOnOffer itemToTranslate = new ItemOnOffer(
                "sviestas",
                "ROKIŠKIO",
                1,
                "shopName"
        );
        String expectedName = "butter";
        ItemOnOffer translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getName(),translatedItem.getName());
        assertEquals(expectedName,translatedItem.getName());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertEquals(itemToTranslate.getPrice(), translatedItem.getPrice(), 0.0);
        assertEquals(itemToTranslate.getShopName(),translatedItem.getShopName());
    }

    @Test
    public void givenComplexItem_whenTranslate_thenReturnTranslatedItem(){
        ItemOnOffer itemToTranslate = new ItemOnOffer(
                "Atšaldytas kiaulienos kumpis be kaulų, be odos, vakuumuotas",
                "ROKIŠKIO",
                1,
                "shopName"
        );
        String expectedName = "Chilled pork ham, boneless, skinless, vacuum";
        ItemOnOffer translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getName(),translatedItem.getName());
        assertEquals(expectedName,translatedItem.getName());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertTrue(itemToTranslate.getPrice() == translatedItem.getPrice());
    }

    @Test
    public void givenItemWithNoName_whenTranslate_returnTranslatedBrand(){
        ItemOnOffer itemToTranslate = new ItemOnOffer(
                "KONSERVUOTOMS DARŽOVĖMS, VAISIAMS IR UOGIENĖMS",
                "",
                1,
                "shopName"
        );
        String expectedName = "PRESERVED VEGETABLES, FRUIT AND JAM";
        ItemOnOffer translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getName(),translatedItem.getName());
        assertEquals(expectedName,translatedItem.getName());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertTrue(itemToTranslate.getPrice() == translatedItem.getPrice());
    }

    @Test
    public void givenItemWithNoName_whenTranslate_returnTranslatedBrandFiltered(){
        ItemOnOffer itemToTranslate = new ItemOnOffer(
                "KOJINĖMS IR PĖDKELNĖMS",
                "",
                1,
                "shopName"
        );
        String expectedName = "SOCKS AND TIGHTS";
        ItemOnOffer translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getName(),translatedItem.getName());
        assertEquals(expectedName,translatedItem.getName());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertTrue(itemToTranslate.getPrice() == translatedItem.getPrice());
    }
}
