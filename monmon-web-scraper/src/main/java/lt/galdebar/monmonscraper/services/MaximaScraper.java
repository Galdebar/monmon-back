package lt.galdebar.monmonscraper.services;

import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class MaximaScraper {
    private final String CONTAINER_NAME = "offers_container";
    private final String ITEM_NAME = "item";
    @Getter
    private Document document;


    private boolean isDocumentValid;

    @Autowired
    private MaximaHTMLElementParserHelper elementParser;

    MaximaScraper() {
        try {
            document = Jsoup.connect("https://www.baeldung.com/").get();
            isDocumentValid = true;
        } catch (IOException e) {
            e.printStackTrace();
            isDocumentValid = false;
        }
    }

    MaximaScraper(Document doc) {
        if (doc.childNodes().size() > 0) {
            document = doc;
            isDocumentValid = true;
        } else isDocumentValid = false;
    }

    String getTitle() {
        if (isDocumentValid) {
            return document.title();
        } else return "";
    }

    public boolean isValid() {
        return isDocumentValid;
    }

    Element getContainer() {
        return document.getElementById(CONTAINER_NAME);
    }

    Elements getItemsOnOffer() {
        return getContainer().getElementsByClass(ITEM_NAME);
    }

    ScrapedShoppingItem createItem(Element element) {
        return elementParser.parseElement(element);
    }


}
