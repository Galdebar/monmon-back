package lt.galdebar.monmonapi.webscraper.services.scrapers;

import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
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
                "https://iki.lt/akcijos/savaites-akcijos",
                "promotions",
                "akcija__container",
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

    //just left this constructor so the tests with the old naming doesn't break
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
    public List<ShoppingItemDealDTO> getItemsOnOffer() {
        if (!document.location().contains(URL)) {

            Elements elements = document.getElementsByClass(ITEM_NAME);
            return elementsToScrapedItems(elements);
        }
        int pagesCount = countPages();
        List<ShoppingItemDealDTO> totalDeals = new ArrayList<>();
        for (int i = 0; i < pagesCount; i++) {
            List<ShoppingItemDealDTO> dealsPerPage = fetchItemsWithOffset(i);
            totalDeals.addAll(dealsPerPage);
        }
        return totalDeals;
    }

    List<ShoppingItemDealDTO> fetchItemsWithOffset(int i) {
        int offset = ITEMS_PER_PAGE * i;
        String url = URL + "/page/" + i + "/";
        Document fetchedDoc;

        try {
            fetchedDoc = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .method(Connection.Method.GET)
//                    .cookie("PHPSESSID", sessionID)
                    .maxBodySize(0)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return getItemsOnOffer(fetchedDoc);
    }

    int countPages() {
        Element paginationElement = document.getElementsByClass("nav-links").get(0);
        Elements pagesList =  paginationElement
                .getElementsByTag("a");
        String lastElementText = pagesList.get(pagesList.size()-2).text();
        return Integer.parseInt(lastElementText);
    }

    @Override
    public List<ShoppingItemDealDTO> getItemsOnOffer(Document document) {
//        Elements elements = document.getElementsByClass(ITEM_NAME);
        Elements elements = document.select("." + ITEM_NAME);
        return elementsToScrapedItems(elements);
    }
}
