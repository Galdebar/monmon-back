package lt.galdebar.monmonapi.webscraper.services.testhelpers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class GetItemsCountFromWebsites {

    public static int getTotalItemsFromMaxima() {
        Document doc = null;
        try {
            doc = Jsoup.connect("https://www.maxima.lt/akcijos#visi-pasiulymai-1").userAgent("Mozilla").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements totalElements = doc.getElementsByClass("item");
        return totalElements.size();
    }

    public static int getTotalItemsFromRimi() {
        Document doc = null;
        try {
            doc = Jsoup.connect("https://www.rimi.lt/akcijos").userAgent("Mozilla").maxBodySize(0).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements totalElements = doc.getElementsByClass("container")
                .get(3)
                .getElementsByClass("offer-card");
        return totalElements.size();
    }

    public static int getTotalItemsFromIki() {
        String mainUrl = "https://www.iki.lt/akcijos/";
        int itemsPerPage = 18;
        Document document = null;
        try {
            document = Jsoup.connect(mainUrl).userAgent("Mozilla").maxBodySize(0).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document.getElementsByClass("akcija__container").size();
//
//        Element paginationElement = document.getElementsByClass("row-sales-pagination").get(0);
//        Elements pagesList = paginationElement
//                .getElementsByTag("li");
//        String lastElementText = pagesList.get(pagesList.size() - 1).getElementsByTag("a").get(0).text();
//
//        int pagesCount = Integer.parseInt(lastElementText);
//        int itemsOnLastPage = 0;
//        try {
//            itemsOnLastPage = Jsoup
//                    .connect(mainUrl + "?start=" + (pagesCount - 1) * itemsPerPage)
//                    .maxBodySize(0)
//                    .get()
//                    .select(".sales-item")
//                    .size();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return (pagesCount-1)*itemsPerPage + itemsOnLastPage;
    }
}
