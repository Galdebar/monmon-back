package lt.galdebar.monmonapi.webscraper.services.scrapers;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.services.scrapers.helpers.RimiParserHelper;
import lt.galdebar.monmonapi.webscraper.services.scrapers.pojos.ItemOnOffer;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Log4j2
public class RimiScraper extends Scraper {

    public RimiScraper() {
        super(
                "https://www.rimi.lt/akcijos",
                "container",
                "offer-card",
                ShopNames.RIMI,
                new RimiParserHelper()
        );
        try {
            Connection.Response response = Jsoup.connect(URL).userAgent(USER_AGENT).maxBodySize(0).execute();
            document = response.parse();
            sessionID = response.cookie("SESSIONID");
            isDocumentValid = true;
        } catch (IOException e) {
            e.printStackTrace();
            isDocumentValid = false;
        }
    }

    public RimiScraper(Document doc){
        super(
                "https://www.rimi.lt/akcijos",
                "container",
                "offer-card",
                ShopNames.RIMI,
                new RimiParserHelper()
        );
        if (doc.childNodes().size() > 0) {
            document = doc;
            isDocumentValid = true;
        } else isDocumentValid = false;
    }



    @Override
    public List<ShoppingItemDealDTO> getItemsOnOffer() {
            Elements elements = document.getElementsByClass(CONTAINER_NAME).get(3)
                    .getElementsByClass(ITEM_NAME);
            return elementsToScrapedItems(elements);
    }

    @Override
    public List<ShoppingItemDealDTO> getItemsOnOffer(Document document) {
        Elements elements = document.getElementsByClass(CONTAINER_NAME).get(3)
                .getElementsByClass(ITEM_NAME);
        return elementsToScrapedItems(elements);
    }
}
