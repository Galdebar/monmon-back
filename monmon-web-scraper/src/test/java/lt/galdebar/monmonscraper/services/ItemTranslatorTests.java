package lt.galdebar.monmonscraper.services;

import lt.galdebar.monmonscraper.domain.ScrapedShoppingItem;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.Assert.*;

@SpringJUnitConfig
public class ItemTranslatorTests {
    @Autowired
    private ItemTranslator itemTranslator;

    @Configuration
    public static class TestConfiguration {
        @Bean
        public MaximaScraper maximaScraper() {
            return new MaximaScraper();
        }

        @Bean
        public MaximaHTMLElementParserHelper maximaHTMLElementParserHelper() {
            return new MaximaHTMLElementParserHelper();
        }

        @Bean
        public ItemTranslator itemTranslator(){
            return new ItemTranslator();
        }
    }

    @Autowired
    MaximaScraper maximaScraper;


    @Test
    void givenContext_thenLoadTranslator(){
        assertNotNull(itemTranslator);
    }

    @Test
    void givenValidItem_whenTranslate_thenReturnTranslatedItem(){
        ScrapedShoppingItem itemToTranslate = new ScrapedShoppingItem(
                "sviestas",
                "ROKIŠKIO",
                1
        );
        String expectedName = "butter";
        ScrapedShoppingItem translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getName(),translatedItem.getName());
        assertEquals(expectedName,translatedItem.getName());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertTrue(itemToTranslate.getPrice() == translatedItem.getPrice());
    }

    @Test
    void givenComplexItem_whenTranslate_thenReturnTranslatedItem(){
        ScrapedShoppingItem itemToTranslate = new ScrapedShoppingItem(
                "Atšaldytas kiaulienos kumpis be kaulų, be odos, vakuumuotas",
                "ROKIŠKIO",
                1
        );
        String expectedName = "Chilled pork ham, boneless, skinless, vacuum";
        ScrapedShoppingItem translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getName(),translatedItem.getName());
        assertEquals(expectedName,translatedItem.getName());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertTrue(itemToTranslate.getPrice() == translatedItem.getPrice());
    }

    @Test
    void givenItemWithNoName_whenTranslate_returnTranslatedBrand(){
        ScrapedShoppingItem itemToTranslate = new ScrapedShoppingItem(
                "KONSERVUOTOMS DARŽOVĖMS, VAISIAMS IR UOGIENĖMS",
                "",
                1
        );
        String expectedName = "PRESERVED VEGETABLES, FRUIT AND JAM";
        ScrapedShoppingItem translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getName(),translatedItem.getName());
        assertEquals(expectedName,translatedItem.getName());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertTrue(itemToTranslate.getPrice() == translatedItem.getPrice());
    }

    @Test
    void givenItemWithNoName_whenTranslate_returnTranslatedBrandFiltered(){
        ScrapedShoppingItem itemToTranslate = new ScrapedShoppingItem(
                "KOJINĖMS IR PĖDKELNĖMS",
                "",
                1
        );
        String expectedName = "SOCKS AND TIGHTS";
        ScrapedShoppingItem translatedItem = itemTranslator.translate(itemToTranslate);

        assertNotNull(translatedItem);
        assertNotEquals(itemToTranslate.getName(),translatedItem.getName());
        assertEquals(expectedName,translatedItem.getName());
        assertEquals(itemToTranslate.getBrand(),translatedItem.getBrand());
        assertTrue(itemToTranslate.getPrice() == translatedItem.getPrice());
    }
}
