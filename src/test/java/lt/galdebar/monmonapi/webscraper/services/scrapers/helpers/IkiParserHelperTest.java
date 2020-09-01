package lt.galdebar.monmonapi.webscraper.services.scrapers.helpers;

import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.scheduledtasks.RunScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = {"classpath:categoriesparser/test.properties"})
@ContextConfiguration(initializers = {ListTestContainersConfig.Initializer.class})
@SpringBootTest
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = RunScraper.class)})
public class IkiParserHelperTest {

    private IkiParserHelper parser = new IkiParserHelper();

    @Test
    public void contextLoads() {
        assertNotNull(parser);
    }

    @Test
    public void parseSimpleElementTest() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/IkiHTMLElements/SimpleElement.html");
        String expectedName = "kopūstų salotos";
        String expectedBrand = "PRANO";
        String expectedShopName = "Iki";
        float expectedPrice = 0.99f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getUntranslatedTitle());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
        assertEquals(expectedShopName, actualItem.getShopTitle());
    }
    @Test
    public void parseSimpleElementTest2() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/IkiHTMLElements/SimpleElement2.html");
        String expectedName = "Sveriami obuoliai";
        String expectedBrand = "GOLDEN";
        String expectedShopName = "Iki";
        float expectedPrice = 1.49f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getUntranslatedTitle());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
        assertEquals(expectedShopName, actualItem.getShopTitle());
    }

    @Test
    public void parseNoBrandElementTest() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/IkiHTMLElements/NoBrandElement.html");
        String expectedName = "Sveriami bananai";
        String expectedBrand = "";
        float expectedPrice = 0.99f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getUntranslatedTitle());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void parseSeveralCategorieselementTest() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/IkiHTMLElements/SeveralCategoriesElement.html");
        String expectedName = "jogurtams, glaistytiems varškės sūreliams";
        String expectedBrand = "VILKYŠKIŲ";
        float expectedPrice = 0;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getUntranslatedTitle());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement1Test() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/IkiHTMLElements/ComplexElement.html");
        String expectedName = "suris";
        String expectedBrand = "NRT ROQUEFORT";
        float expectedPrice = 2.62f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getUntranslatedTitle());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement2Test() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/IkiHTMLElements/ComplexElement2.html");
        String expectedName = "vytintas kumpis brandintas";
        String expectedBrand = "NRT SAVOIE";
        float expectedPrice = 2.79f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getUntranslatedTitle());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement3Test(){
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/IkiHTMLElements/ComplexElement3.html");
        String expectedName = "Karštai rūkytas saliamis";
        String expectedBrand = "SAMSONO";
        float expectedPrice = 2.49f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getUntranslatedTitle());
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