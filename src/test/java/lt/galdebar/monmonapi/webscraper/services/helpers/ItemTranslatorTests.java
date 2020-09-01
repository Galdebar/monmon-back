package lt.galdebar.monmonapi.webscraper.services.helpers;

import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.scheduledtasks.RunScraper;
import lt.galdebar.monmonapi.webscraper.services.helpers.translators.HackyGoogleItemTranslator;
import lt.galdebar.monmonapi.webscraper.services.helpers.translators.IsItemTranslator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = {"classpath:categoriesparser/test.properties"})
@ContextConfiguration(initializers = {ListTestContainersConfig.Initializer.class})
@SpringBootTest
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = RunScraper.class)})
public class ItemTranslatorTests {
    @Autowired
    private HackyGoogleItemTranslator itemTranslator;

    @org.springframework.boot.test.context.TestConfiguration
    public static class TestConfiguration {

        @Bean
        public IsItemTranslator itemTranslator(){
            return new HackyGoogleItemTranslator();
        }
    }


    @Test
    public void givenContext_thenLoadTranslator(){
        assertNotNull(itemTranslator);
    }

    @Test
    public void givenValidItem_whenTranslate_thenReturnTranslatedItem(){
        ShoppingItemDealDTO itemToTranslate = new ShoppingItemDealDTO(
                "sviestas",
                "",
                "ROKIŠKIO",
                "shopName",
                1
        );
        String expectedName = "butter";
        ShoppingItemDealDTO translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getTitle(),translatedItem.getTitle());
        assertEquals(expectedName,translatedItem.getTitle());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertEquals(itemToTranslate.getPrice(), translatedItem.getPrice(), 0.0);
        assertEquals(itemToTranslate.getShopTitle(),translatedItem.getShopTitle());
    }

    @Test
    public void givenComplexItem_whenTranslate_thenReturnTranslatedItem(){
        ShoppingItemDealDTO itemToTranslate = new ShoppingItemDealDTO(
                "Atšaldytas kiaulienos kumpis be kaulų, be odos, vakuumuotas",
                "",
                "ROKIŠKIO",
                "shopName",
                1
        );
        String expectedName = "Chilled pork ham, boneless, skinless, vacuum";
        ShoppingItemDealDTO translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getTitle(),translatedItem.getTitle());
        assertEquals(expectedName,translatedItem.getTitle());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertEquals(itemToTranslate.getPrice(), translatedItem.getPrice(), 0.0);
    }

    @Test
    public void givenItemWithNoName_whenTranslate_returnTranslatedBrand(){
        ShoppingItemDealDTO itemToTranslate = new ShoppingItemDealDTO(
                "KONSERVUOTOMS DARŽOVĖMS, VAISIAMS IR UOGIENĖMS",
                "",
                "",
                "shopName",
                1
        );
        String expectedName = "PRESERVED VEGETABLES, FRUIT AND JAM";
        ShoppingItemDealDTO translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getTitle(),translatedItem.getTitle());
        assertEquals(expectedName,translatedItem.getTitle());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertEquals(itemToTranslate.getPrice(), translatedItem.getPrice(), 0.0);
    }

    @Test
    public void givenItemWithNoName_whenTranslate_returnTranslatedBrandFiltered(){
        ShoppingItemDealDTO itemToTranslate = new ShoppingItemDealDTO(
                "KOJINĖMS IR PĖDKELNĖMS",
                "",
                "",
                "shopName",
                1
        );
        String expectedName = "SOCKS AND TIGHTS";
        ShoppingItemDealDTO translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getTitle(),translatedItem.getTitle());
        assertEquals(expectedName,translatedItem.getTitle());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertEquals(itemToTranslate.getPrice(), translatedItem.getPrice(), 0.0);
    }
}
