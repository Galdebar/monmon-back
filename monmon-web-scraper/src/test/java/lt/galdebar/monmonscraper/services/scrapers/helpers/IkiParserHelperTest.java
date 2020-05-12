package lt.galdebar.monmonscraper.services.scrapers.helpers;

import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
public class IkiParserHelperTest {

    IkiParserHelper parser = new IkiParserHelper();

    @Test
    public void contextLoads() {
        assertNotNull(parser);
    }

    @Test
    public void parseSimpleElementTest() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/IkiHTMLElements/SimpleElement.html");
        String expectedName = "kopūstų salotos";
        String expectedBrand = "PRANO";
        String expectedShopName = "Iki";
        float expectedPrice = 0.99f;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
        assertEquals(expectedShopName, actualItem.getShopName());
    }
    @Test
    public void parseSimpleElementTest2() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/IkiHTMLElements/SimpleElement2.html");
        String expectedName = "Sveriami obuoliai";
        String expectedBrand = "GOLDEN";
        String expectedShopName = "Iki";
        float expectedPrice = 1.49f;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
        assertEquals(expectedShopName, actualItem.getShopName());
    }

    @Test
    public void parseNoBrandElementTest() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/IkiHTMLElements/NoBrandElement.html");
        String expectedName = "Sveriami bananai";
        String expectedBrand = "";
        float expectedPrice = 0.99f;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void parseSeveralCategorieselementTest() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/IkiHTMLElements/SeveralCategoriesElement.html");
        String expectedName = "jogurtams, glaistytiems varškės sūreliams";
        String expectedBrand = "VILKYŠKIŲ";
        float expectedPrice = 0;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement1Test() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/IkiHTMLElements/ComplexElement.html");
        String expectedName = "suris";
        String expectedBrand = "NRT ROQUEFORT";
        float expectedPrice = 2.62f;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement2Test() {
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/IkiHTMLElements/ComplexElement2.html");
        String expectedName = "vytintas kumpis brandintas";
        String expectedBrand = "NRT SAVOIE";
        float expectedPrice = 2.79f;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
    }

    @Test
    public void complexElement3Test(){
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/IkiHTMLElements/ComplexElement3.html");
        String expectedName = "Karštai rūkytas saliamis";
        String expectedBrand = "SAMSONO";
        float expectedPrice = 2.49f;

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