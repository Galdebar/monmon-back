package lt.galdebar.monmonapi.webscraper.services.helpers;

import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.webscraper.scheduledtasks.RunScraper;
import lt.galdebar.monmonapi.webscraper.services.helpers.translators.HackyGoogleItemTranslator;
import lt.galdebar.monmonapi.webscraper.services.helpers.translators.IsItemTranslator;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
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
        assertEquals(itemToTranslate.getPrice(), translatedItem.getPrice(), 0.0);
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
        assertEquals(itemToTranslate.getPrice(), translatedItem.getPrice(), 0.0);
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
        assertEquals(itemToTranslate.getPrice(), translatedItem.getPrice(), 0.0);
    }
}
