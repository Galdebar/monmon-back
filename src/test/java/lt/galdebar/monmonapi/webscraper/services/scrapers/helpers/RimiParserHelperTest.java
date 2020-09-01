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

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = {"classpath:categoriesparser/test.properties"})
@ContextConfiguration(initializers = {ListTestContainersConfig.Initializer.class})
@SpringBootTest
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = RunScraper.class)})
public class RimiParserHelperTest {

    private RimiParserHelper parser = new RimiParserHelper();

    @Test
    public void contextLoads(){
        assertNotNull(parser);
    }

    @Test
    public void parseSimpleElementTest(){
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/RimiHTMLElements/SimpleElement.html");
        String expectedName = "Sūris";
        String expectedBrand = "BRIE PRESIDENT";
        String expectedShopName = "Rimi";
        float expectedPrice = 8.99f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getUntranslatedTitle());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
        assertEquals(expectedShopName, actualItem.getShopTitle());

    }

    @Test
    public void parseNoBrandElementTest() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/RimiHTMLElements/NoBrandElement.html");
        String expectedName = "Kalakutų filė";
        String expectedBrand = "";
        float expectedPrice = 6.99f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getUntranslatedTitle());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void parseSeveralCategorieselementTest() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/RimiHTMLElements/SeveralCategoriesElement.html");
        String expectedName = "Filtrams, filtravimo indams ar kasetėms";
        String expectedBrand = "BRITA";
        float expectedPrice = 0;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getUntranslatedTitle());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    private Element getElementFromFile(String filePath) {
        return Jsoup.parse(
                htmlStringFromFile(filePath)
        );
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
}
