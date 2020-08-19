package lt.galdebar.monmonapi.webscraper.services.scrapers;

import lt.galdebar.monmonapi.webscraper.services.scrapers.helpers.IkiParserHelper;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class IkiScraper extends Scraper {
    private final int ITEMS_PER_PAGE = 18;

    public IkiScraper(){
        super(
                "https://www.iki.lt/akcijos/",
                "sales-main-wrap",
                "sales-item ",
                ShopNames.IKI,
                new IkiParserHelper()
        );
        try {
            Connection.Response response = Jsoup.connect(URL).userAgent(USER_AGENT).maxBodySize(0).execute();
            document = response.parse();
            sessionID = response.cookie("PHPSESSID");
            isDocumentValid = true;
        } catch (IOException e) {
            e.printStackTrace();
            isDocumentValid = false;
        }
    }

    public IkiScraper(Document doc) {
        super(
                "https://www.iki.lt/akcijos/",
                "sales-main-wrap",
                "sales-item ",
                ShopNames.IKI,
                new IkiParserHelper()
        );
        if (doc.childNodes().size() > 0) {
            document = doc;
            isDocumentValid = true;
        } else isDocumentValid = false;
    }

    @Override
    public List<ItemOnOffer> getItemsOnOffer() {
        if (!document.location().contains(URL)) {

            Elements elements = document.getElementsByClass(ITEM_NAME);
            return elementsToScrapedItems(elements);
        }
        int pagesCount = countPages();
        List<ItemOnOffer> totalElements = new ArrayList<>();
        for (int i = 0; i < pagesCount; i++) {
            List<ItemOnOffer> fetchedPage = fetchItemsWithOffset(i);
            totalElements.addAll(fetchedPage);
        }
        return totalElements;
    }

    List<ItemOnOffer> fetchItemsWithOffset(int i) {
        int offset = ITEMS_PER_PAGE * i;
        String url = URL + "?start=" + offset;
        Document fetchedDoc;

        try {
            fetchedDoc = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .method(Connection.Method.GET)
                    .cookie("PHPSESSID", sessionID)
                    .maxBodySize(0)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return getItemsOnOffer(fetchedDoc);
    }

    int countPages() {
        Element paginationElement = document.getElementsByClass("row-sales-pagination").get(0);
        Elements pagesList =  paginationElement
                .getElementsByTag("li");
        String lastElementText = pagesList.get(pagesList.size()-1).getElementsByTag("a").get(0).text();
        return Integer.parseInt(lastElementText);
    }

    @Override
    public List<ItemOnOffer> getItemsOnOffer(Document document) {
//        Elements elements = document.getElementsByClass(ITEM_NAME);
        Elements elements = document.select("." + ITEM_NAME);
        return elementsToScrapedItems(elements);
    }
}
