package lt.galdebar.monmonscraper.services;

import lombok.Getter;
import lt.galdebar.monmonscraper.domain.ScrapedShoppingItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Maxima website scraper.
 */
@Service
public class MaximaScraper {
    private final String CONTAINER_NAME = "offers_container";
    private final String ITEM_NAME = "item";
    @Getter
    private Document document;


    private boolean isDocumentValid;

    @Autowired
    private MaximaHTMLElementParserHelper elementParser;


    /**
     * Instantiates a new Maxima scraper.
     *
     * @param doc the doc
     */
    MaximaScraper(Document doc) {
        if (doc.childNodes().size() > 0) {
            document = doc;
            isDocumentValid = true;
        } else isDocumentValid = false;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    String getTitle() {
        if (isDocumentValid) {
            return document.title();
        } else return "";
    }

    /**
     * Is valid boolean.
     *
     * @return the boolean
     */
    public boolean isValid() {
        return isDocumentValid;
    }

    /**
     * Gets container.
     *
     * @return the container
     */
    Element getContainer() {
        return document.getElementById(CONTAINER_NAME);
    }

    /**
     * Gets items on offer.
     *
     * @return the items on offer
     */
    Elements getItemsOnOffer() {
        return getContainer().getElementsByClass(ITEM_NAME);
    }

    /**
     * Create item scraped shopping item.
     *
     * @param element the element
     * @return the scraped shopping item
     */
    ScrapedShoppingItem createItem(Element element) {
        return elementParser.parseElement(element);
    }


}
