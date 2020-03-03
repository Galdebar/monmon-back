package lt.galdebar.monmonscraper.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
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
    private final int ITEMS_PER_PAGE = 45;
    private String sessionID;

    @Getter
    private Document document;


    private boolean isDocumentValid;

    @Autowired
    private MaximaHTMLElementParserHelper elementParser;

    MaximaScraper() {
        try {
            Connection.Response response = Jsoup.connect("https://www.maxima.lt/akcijos#visi-pasiulymai-1").userAgent(USER_AGENT).execute();
            document = response.parse();
            sessionID = response.cookie("SESSIONID");
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
    Element getContainer(Document document){
        return document.getElementById(CONTAINER_NAME);
    }

    Elements getItemsOnOffer() {
        return getContainer().getElementsByClass(ITEM_NAME);
    }

    Elements getItemsOnOffer(Document document){
        return document.getElementsByClass(ITEM_NAME);
    }

    ScrapedShoppingItem createItem(Element element) {
        return elementParser.parseElement(element);
    }



    public int countNumOfRequiredRequests() {
        int itemsCount = 0;
        int totalItemsCount;
        try {
            totalItemsCount = Integer.parseInt(document.getElementById("items_cnt").text());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            totalItemsCount = 0;
        }
        if (totalItemsCount % ITEMS_PER_PAGE > 0) {
            itemsCount = (totalItemsCount / ITEMS_PER_PAGE) + 1;
        } else itemsCount = totalItemsCount / ITEMS_PER_PAGE;

        return itemsCount;
    }

    public Elements fetchItemsWithOffset() {
        String url = "https://www.maxima.lt/ajax/saleloadmore";
        Document fetchedDoc = null;
        MaximaRequestObject requestObj = new MaximaRequestObject();
        requestObj.addToMap("orderBy", "discount");
        requestObj.addToMap("offset", "0");
        String test = requestObj.toString();

        try {
            Connection.Response response= Jsoup.connect(url)
//                    .data("orderBy", "discount")
//                    .data("offset", "0")
                    .ignoreContentType(true)
                    .method(Connection.Method.POST)
//                    .header("Accept", "application/json")
//                    .header("Content-Type", "application/json")
                    .requestBody(requestObj.toString())
//                    .userAgent(USER_AGENT)
                    .cookie("SESSIONID", sessionID)
                    .execute();
            String responseString = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseString);
            String htmlString = jsonNode.get("html").asText();
            fetchedDoc = Jsoup.parse(htmlString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Elements items = getItemsOnOffer(fetchedDoc);
        return items;
    }
}
