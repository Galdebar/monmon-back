package lt.galdebar.monmonscraper.services;

import lt.galdebar.monmonscraper.domain.ScrapedShoppingItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
class MaximaHTMLElementParserHelperTest {

    @Configuration
    public static class TestConfiguration {
        @Bean
        public MaximaHTMLElementParserHelper maximaHTMLElementParserHelper() {
            return new MaximaHTMLElementParserHelper();
        }
    }

    @Autowired
    MaximaHTMLElementParserHelper parser;

    @Test
    void contextLoads() {
        assertNotNull(parser);
    }

    @Test
    void parseSimpleElementTest() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/SimpleElement.html");
        String expectedName = "sviestas";
        String expectedBrand = "ROKIŠKIO";
        float expectedPrice = 1.09f;

        ScrapedShoppingItem actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    void parseNoBrandElementTest() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/NoBrandElement.html");
        String expectedName = "Šviežios viščiukų broilerių blauzdelės";
        String expectedBrand = "";
        float expectedPrice = 1.19f;

        ScrapedShoppingItem actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    void parseSingleCategoryElementTest() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/SingleCategoryElement.html");
        String expectedName = "KOJINĖMS IR PĖDKELNĖMS";
        String expectedBrand = "";
        float expectedPrice = 0;

        ScrapedShoppingItem actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    void parseSeveralCategorieselementTest() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/SeveralCategoriesElement.html");
        String expectedName = "KONSERVUOTOMS DARŽOVĖMS, VAISIAMS IR UOGIENĖMS";
        String expectedBrand = "";
        float expectedPrice = 0;

        ScrapedShoppingItem actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    void complexElement1Test() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/ComplexElement.html");
        String expectedName = "Rauginti kopūstai fasuoti";
        String expectedBrand = "LINKĖJIMAI IŠ KAIMO";
        float expectedPrice = 1.09f;

        ScrapedShoppingItem actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    void complexElement2Test() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/ComplexElement2.html");
        String expectedName = "Atšaldytas kiaulienos kumpis be kaulų, be odos, vakuumuotas";
        String expectedBrand = "LAUKUVA";
        float expectedPrice = 2.79f;

        ScrapedShoppingItem actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    void complexElement3Test(){
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/ComplexElement3.html");
        String expectedName = "Virta dešra";
        String expectedBrand = "SAMSONO DAKTARIŠKA";
        float expectedPrice = 1.74f;

        ScrapedShoppingItem actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }


    private String htmlStringFromFile(String filePath) {
        File elementFile = new File(filePath);
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(elementFile));
            String string;
            while ((string = bufferedReader.readLine()) != null) {
                stringBuilder.append(string);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    private Element getElementFromFile(String filePath) {
        return Jsoup.parse(
                htmlStringFromFile(filePath)
        );
    }
}