package lt.galdebar.monmonscraper.services.scrapers.helpers;

import lt.galdebar.monmonscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
public class RimiParserHelperTest {

    private RimiParserHelper parser = new RimiParserHelper();

    @Test
    public void contextLoads(){
        assertNotNull(parser);
    }

    @Test
    public void parseSimpleElementTest(){
        Element testElement = getElementFromFile("src/test/resources/WebsiteSnapshots/RimiHTMLElements/SimpleElement.html");
        String expectedName = "Sūris";
        String expectedBrand = "BRIE PRESIDENT";
        String expectedShopName = "Rimi";
        float expectedPrice = 8.99f;

        ItemOnOffer actualItem = parser.parseElement(testElement);

        assertEquals(expectedName, actualItem.getName());
        assertEquals(expectedBrand, actualItem.getBrand());
        assertEquals(expectedPrice, actualItem.getPrice());
        assertEquals(expectedShopName, actualItem.getShopName());

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
