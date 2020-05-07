package lt.galdebar.monmonscraper.services.testhelpers;

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
        Element totalItemsElement = doc.getElementById("items_cnt");
        return Integer.parseInt(totalItemsElement.text());
    }

    public static int getTotalItemsFromRimi(){
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
}
