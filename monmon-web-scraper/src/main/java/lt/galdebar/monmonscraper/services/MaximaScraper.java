package lt.galdebar.monmonscraper.services;

import lombok.Getter;
import org.jsoup.Connection;
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
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36";
    @Getter
    private Document document;


    private boolean isDocumentValid;

    @Autowired
    private MaximaHTMLElementParserHelper elementParser;

    MaximaScraper() {
        try {
            document = Jsoup.connect("https://www.maxima.lt/akcijos#visi-pasiulymai-1").userAgent(USER_AGENT).get();
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
        Elements selectedElements = new Elements();
        Elements paginationElements = document.select("." + ITEM_NAME);
        sendRequest();
        return getContainer().getElementsByClass(ITEM_NAME);
    }

    ScrapedShoppingItem createItem(Element element) {
        return elementParser.parseElement(element);
    }

    private void sendRequest(){
        String requestURL = "https://www.maxima.lt/js/front/app/offers.js?v5";
        try {
            Connection.Response response = Jsoup.connect(requestURL).ignoreContentType(true).method(Connection.Method.GET).userAgent(USER_AGENT).execute();
            Document newDoc = response.parse();
            System.out.println(newDoc.getElementById("items_cnt").text());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("SomethingWentWrong");
        }
    }


}
