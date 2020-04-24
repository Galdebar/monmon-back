package lt.galdebar.monmonscraper.services.scrapers.helpers;

import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
public class MaximaParserHelperTest {

    @org.springframework.boot.test.context.TestConfiguration
    public static class TestConfiguration {
        @Bean
        public MaximaParserHelper maximaHTMLElementParserHelper() {
            return new MaximaParserHelper();
        }
    }

    @Autowired
    MaximaParserHelper parser;

    @Test
    public void contextLoads() {
        assertNotNull(parser);
    }

    @Test
    public void parseSimpleElementTest() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/SimpleElement.html");
        String expectedName = "sviestas";
        String expectedBrand = "ROKIŠKIO";
        String expectedShopName = "Maxima";
        float expectedPrice = 1.09f;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
        assertEquals(expectedShopName, actualItem.getShopName());
    }

    @Test
    public void parseNoBrandElementTest() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/NoBrandElement.html");
        String expectedName = "Šviežios viščiukų broilerių blauzdelės";
        String expectedBrand = "";
        float expectedPrice = 1.19f;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void parseSingleCategoryElementTest() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/SingleCategoryElement.html");
        String expectedName = "KOJINĖMS IR PĖDKELNĖMS";
        String expectedBrand = "";
        float expectedPrice = 0;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void parseSeveralCategorieselementTest() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/SeveralCategoriesElement.html");
        String expectedName = "KONSERVUOTOMS DARŽOVĖMS, VAISIAMS IR UOGIENĖMS";
        String expectedBrand = "";
        float expectedPrice = 0;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement1Test() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/ComplexElement.html");
        String expectedName = "Rauginti kopūstai fasuoti";
        String expectedBrand = "LINKĖJIMAI IŠ KAIMO";
        float expectedPrice = 1.09f;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement2Test() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/ComplexElement2.html");
        String expectedName = "Atšaldytas kiaulienos kumpis be kaulų, be odos, vakuumuotas";
        String expectedBrand = "LAUKUVA";
        float expectedPrice = 2.79f;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement3Test(){
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/MaximaHTMLElements/ComplexElement3.html");
        String expectedName = "Virta dešra";
        String expectedBrand = "SAMSONO DAKTARIŠKA";
        float expectedPrice = 1.74f;

        ItemOnOffer actualItem = parser.parseElement(testElement);

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