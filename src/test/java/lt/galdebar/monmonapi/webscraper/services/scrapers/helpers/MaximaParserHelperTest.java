package lt.galdebar.monmonapi.webscraper.services.scrapers.helpers;

import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.scheduledtasks.RunScraper;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
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
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = {"classpath:categoriesparser/test.properties"})
@ContextConfiguration(initializers = {ListTestContainersConfig.Initializer.class})
@SpringBootTest
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = RunScraper.class)})
public class MaximaParserHelperTest {

    private MaximaParserHelper parser = new MaximaParserHelper();

    @Test
    public void contextLoads() {
        assertNotNull(parser);
    }

    @Test
    public void parseSimpleElementTest() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/MaximaHTMLElements/SimpleElement.html");
        String expectedName = "sviestas";
        String expectedBrand = "ROKIŠKIO";
        String expectedShopName = "Maxima";
        float expectedPrice = 1.09f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getTitle());
        assertEquals(expectedBrand, actualItem.getItemBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
        assertEquals(expectedShopName, actualItem.getShopTitle());
    }

    @Test
    public void parseNoBrandElementTest() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/MaximaHTMLElements/NoBrandElement.html");
        String expectedName = "Šviežios viščiukų broilerių blauzdelės";
        String expectedBrand = "";
        float expectedPrice = 1.19f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getTitle());
        assertEquals(expectedBrand, actualItem.getItemBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void parseSingleCategoryElementTest() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/MaximaHTMLElements/SingleCategoryElement.html");
        String expectedName = "KOJINĖMS IR PĖDKELNĖMS";
        String expectedBrand = "";
        float expectedPrice = 0;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getTitle());
        assertEquals(expectedBrand, actualItem.getItemBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void parseSeveralCategorieselementTest() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/MaximaHTMLElements/SeveralCategoriesElement.html");
        String expectedName = "KONSERVUOTOMS DARŽOVĖMS, VAISIAMS IR UOGIENĖMS";
        String expectedBrand = "";
        float expectedPrice = 0;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getTitle());
        assertEquals(expectedBrand, actualItem.getItemBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement1Test() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/MaximaHTMLElements/ComplexElement.html");
        String expectedName = "Rauginti kopūstai fasuoti";
        String expectedBrand = "LINKĖJIMAI IŠ KAIMO";
        float expectedPrice = 1.09f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getTitle());
        assertEquals(expectedBrand, actualItem.getItemBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement2Test() {
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/MaximaHTMLElements/ComplexElement2.html");
        String expectedName = "Atšaldytas kiaulienos kumpis be kaulų, be odos, vakuumuotas";
        String expectedBrand = "LAUKUVA";
        float expectedPrice = 2.79f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getTitle());
        assertEquals(expectedBrand, actualItem.getItemBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement3Test(){
        Element testElement = getElementFromFile("src/test/resources/webscraper/WebsiteSnapshots/MaximaHTMLElements/ComplexElement3.html");
        String expectedName = "Virta dešra";
        String expectedBrand = "SAMSONO DAKTARIŠKA";
        float expectedPrice = 1.74f;

        ShoppingItemDealDTO actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getTitle());
        assertEquals(expectedBrand, actualItem.getItemBrand());
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